import argparse
import json
import sys
import time

import yamlstar


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("parser")
    parser.add_argument("yaml_file")
    args = parser.parse_args()

    with open(args.yaml_file, "r", encoding="utf-8") as f:
        yaml_text = f.read()

    opts = yamlstar.Options().plugin(yamlstar.parser(args.parser))
    so = "libyamlstar-graalvm" if args.parser == "snakeyaml" else "libyamlstar"
    ys = yamlstar.YAMLStar(opts, so=so)

    start = time.perf_counter()
    data = ys.load(yaml_text)
    elapsed = time.perf_counter() - start

    print(json.dumps(data, indent=2))
    print(f"{args.parser} load took {elapsed:.6f}s", file=sys.stderr)


if __name__ == "__main__":
    main()
