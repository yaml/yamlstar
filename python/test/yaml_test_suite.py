#!/usr/bin/env python3

import argparse
import json
import os
from pathlib import Path
import re
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "lib"))

import yamlstar


ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SUITE_DIR = ROOT / "yaml-test-suite"
EXPECTED_FAILS_DIR = ROOT / "core" / "test-suite" / "expected-fails"


def read_expected_failures(name):
    if os.environ.get("YAMLSTAR_TEST_SUITE_SHOW_FAILURES"):
        return {}
    path = EXPECTED_FAILS_DIR / name
    flavor = os.environ.get("YAMLSTAR_TEST_SUITE_EXPECTED_FAILS_FLAVOR")
    if flavor:
        flavored = path.with_name(f"{path.stem}-{flavor}{path.suffix}")
        if flavored.exists():
            path = flavored
    failures = {}
    if not path.exists():
        return failures
    for raw in path.read_text().splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        key, _, value = line.partition(":")
        failures[key.strip().strip("'\"")] = value.strip()
    return failures


def suite_root():
    return Path(os.environ.get("YAMLSTAR_TEST_SUITE_DIR", DEFAULT_SUITE_DIR))


def selected_case_ids():
    raw = os.environ.get("YAMLSTAR_TEST_SUITE_CASES")
    if not raw:
        return None
    return {part for part in re.split(r"[,\s]+", raw) if part}


def selected(case_id, selected_ids):
    return (
        selected_ids is None
        or case_id in selected_ids
        or case_id.split("/", 1)[0] in selected_ids
    )


def case_dirs(root):
    for id_dir in sorted(root.iterdir(), key=lambda p: p.name):
        if not id_dir.is_dir() or not re.fullmatch(r"[A-Z0-9]{4}", id_dir.name):
            continue
        if (id_dir / "in.yaml").exists():
            yield id_dir
            continue
        for subtest in sorted(id_dir.iterdir(), key=lambda p: p.name):
            if subtest.is_dir() and re.fullmatch(r"[0-9]{2}", subtest.name):
                yield subtest


def relative_case_id(root, case_dir):
    return case_dir.relative_to(root).as_posix()


def json_documents(content):
    decoder = json.JSONDecoder()
    docs = []
    pos = 0
    while True:
        while pos < len(content) and content[pos].isspace():
            pos += 1
        if pos >= len(content):
            return docs
        doc, pos = decoder.raw_decode(content, pos)
        docs.append(doc)


def document_count(case_dir):
    event_file = case_dir / "test.event"
    if event_file.exists():
        return len(re.findall(r"(?m)^\+DOC$", event_file.read_text()))
    return 1


def expected_result(case_dir):
    json_file = case_dir / "in.json"
    error_file = case_dir / "error"
    if json_file.exists():
        content = json_file.read_text()
        if content.strip():
            return {"kind": "ok", "documents": json_documents(content)}
        return {"kind": "skip", "reason": "blank-json-expectation"}
    if error_file.exists():
        return {"kind": "error"}
    return {"kind": "skip", "reason": "no-loader-expectation"}


def test_name(case_dir):
    name_file = case_dir / "==="
    if name_file.exists():
        return name_file.read_text().strip()
    return None


def load_documents(loader, input_text, count):
    if count > 1:
        return loader.load_all(input_text)
    return [loader.load(input_text)]


def dump_documents(loader, documents):
    if len(documents) > 1:
        return loader.dump_all(documents)
    return loader.dump(documents[0])


def run_loader_case(loader, case_dir, expected):
    if expected["kind"] == "skip":
        return {"status": "skip", "reason": expected["reason"]}
    input_text = (case_dir / "in.yaml").read_text()
    count = document_count(case_dir)
    try:
        actual = load_documents(loader, input_text, count)
        if expected["kind"] == "ok":
            if actual == expected["documents"]:
                return {"status": "pass"}
            return {
                "status": "fail",
                "reason": "mismatch",
                "actual": actual,
                "expected": expected["documents"],
            }
        return {"status": "fail", "reason": "accepted-invalid", "actual": actual}
    except Exception as exc:
        if expected["kind"] == "error":
            return {"status": "pass"}
        return {"status": "fail", "reason": "rejected-valid", "message": str(exc)}


def run_roundtrip_case(loader, case_dir, expected):
    if expected["kind"] != "ok":
        return {"status": "skip", "reason": expected.get("reason", "not-json-compatible")}
    input_text = (case_dir / "in.yaml").read_text()
    count = document_count(case_dir)
    try:
        documents = load_documents(loader, input_text, count)
        dumped = dump_documents(loader, documents)
        reloaded = load_documents(loader, dumped, len(documents))
        if documents != expected["documents"]:
            return {"status": "fail", "reason": "loader-mismatch"}
        if reloaded == expected["documents"]:
            return {"status": "pass"}
        return {
            "status": "fail",
            "reason": "roundtrip-mismatch",
            "actual": reloaded,
            "expected": expected["documents"],
        }
    except Exception as exc:
        return {"status": "fail", "reason": "roundtrip-error", "message": str(exc)}


def emit_fixture_file(case_dir):
    for name in ("emit.yaml", "out.yaml"):
        path = case_dir / name
        if path.exists():
            return path
    return None


def normalize_newlines(text):
    return re.sub(r"\r\n?", "\n", text)


def run_emit_case(loader, case_dir, expected):
    fixture = emit_fixture_file(case_dir)
    if fixture is None:
        return {"status": "skip", "reason": "no-emit-fixture"}
    if expected["kind"] != "ok":
        return {"status": "skip", "reason": expected.get("reason", "not-json-compatible")}
    try:
        actual = normalize_newlines(dump_documents(loader, expected["documents"]))
        expected_yaml = normalize_newlines(fixture.read_text())
        if actual == expected_yaml:
            return {"status": "pass"}
        return {
            "status": "fail",
            "reason": "emit-mismatch",
            "actual": actual,
            "expected": expected_yaml,
        }
    except Exception as exc:
        return {"status": "fail", "reason": "emit-error", "message": str(exc)}


def format_result(result):
    name = f" ({result['name']})" if result.get("name") else ""
    message = f" - {result['message']}" if result.get("message") else ""
    return f"{result['id']}{name}: {result.get('reason')}{message}"


def summarize(results, expected_failures):
    unexpected = [
        r for r in results
        if r["status"] == "fail" and r["id"] not in expected_failures
    ]
    expected_seen = [
        r for r in results
        if r["status"] == "fail" and r["id"] in expected_failures
    ]
    stale = [
        r for r in results
        if r["status"] == "pass" and r["id"] in expected_failures
    ]
    skipped = [r for r in results if r["status"] == "skip"]
    return {
        "total": len(results),
        "passed": len([r for r in results if r["status"] == "pass"]),
        "skipped": len(skipped),
        "expected_failures": len(expected_seen),
        "unexpected": unexpected,
        "stale": stale,
    }


def run_suite(label, expected_file, run_case):
    root = suite_root()
    if not root.is_dir():
        raise SystemExit(f"Missing yaml-test-suite fixture at {root}")

    loader = yamlstar.YAMLStar()
    selected_ids = selected_case_ids()
    expected_failures = read_expected_failures(expected_file)
    results = []
    for case_dir in case_dirs(root):
        case_id = relative_case_id(root, case_dir)
        if not selected(case_id, selected_ids):
            continue
        expected = expected_result(case_dir)
        result = {
            "id": case_id,
            "name": test_name(case_dir),
        }
        result.update(run_case(loader, case_dir, expected))
        results.append(result)

    if not results:
        raise SystemExit(f"No yaml-test-suite cases matched {selected_ids or 'the suite'}")

    if os.environ.get("YAMLSTAR_TEST_SUITE_VERBOSE"):
        for result in results:
            status = result["status"]
            if status == "skip":
                text = f"skip {result.get('reason')}"
            elif status == "fail" and result["id"] in expected_failures:
                text = f"expected-fail {result.get('reason')}"
            elif status == "fail":
                text = f"fail {result.get('reason')}"
            else:
                text = "pass"
            print(f"yaml-test-suite {label} {result['id']}: {text}")

    summary = summarize(results, expected_failures)
    print(
        f"yaml-test-suite {label}: "
        f"{summary['total']} cases, "
        f"{summary['passed']} passed, "
        f"{summary['expected_failures']} expected failures, "
        f"{summary['skipped']} skipped"
    )

    errors = []
    if summary["unexpected"]:
        errors.append(
            "Unexpected yaml-test-suite "
            + label
            + " failures:\n"
            + "\n".join(format_result(r) for r in summary["unexpected"])
        )
    if summary["stale"]:
        errors.append(
            "These cases now pass; remove them from "
            + expected_file
            + ":\n"
            + "\n".join(format_result(r) for r in summary["stale"])
        )
    if errors:
        raise SystemExit("\n\n".join(errors))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "suite",
        nargs="?",
        default="all",
        choices=["all", "load", "roundtrip", "emit"],
    )
    args = parser.parse_args()

    suites = {
        "load": ("loader", "load.yaml", run_loader_case),
        "roundtrip": ("roundtrip", "roundtrip.yaml", run_roundtrip_case),
        "emit": ("emit", "emit.yaml", run_emit_case),
    }
    selected_suites = suites.values() if args.suite == "all" else [suites[args.suite]]
    for suite in selected_suites:
        run_suite(*suite)


if __name__ == "__main__":
    main()

