#!/usr/bin/env bash

# Compare benchmark results before and after glojure perf changes.
#
# Usage:
#   ./bench/compare.sh                    # compare HEAD~1 vs HEAD
#   ./bench/compare.sh gloat perf         # compare specific refs
#
# Prerequisites: run `make ext` first.

set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
GLOJURE=$ROOT/ext/glojure
BENCH=$ROOT/bench/bench.sh
RESULTS=$ROOT/bench/results

BEFORE_REF=${1:-perf~2}
AFTER_REF=${2:-perf}

mkdir -p "$RESULTS"

run_bench() {
  local label=$1 ref=$2 outfile=$3

  echo
  echo "============================================"
  echo "  Benchmarking: $label ($ref)"
  echo "============================================"

  git -C "$GLOJURE" checkout -q "$ref"
  echo "glojure @ $(git -C "$GLOJURE" log --oneline -1)"
  echo

  "$BENCH" 2>&1 | tee "$outfile"
}

run_bench "BEFORE" "$BEFORE_REF" "$RESULTS/before.txt"
run_bench "AFTER"  "$AFTER_REF"  "$RESULTS/after.txt"

# Restore ext/glojure to perf branch
git -C "$GLOJURE" checkout -q perf

# Extract timing lines and compare
echo
echo "============================================"
echo "  Comparison"
echo "============================================"
echo
printf "%-14s %14s %14s %10s\n" "input" "before" "after" "speedup"
printf "%s\n" "------------------------------------------------------"

# Parse timing from bench output (only from the Benchmark section).
# Expects lines like:  "cold:      719.0 ms  (...)"  or  "mapping        658.0 ms"
extract_times() {
  local file=$1
  # Only look at lines after "=== 4. Benchmark ==="
  sed -n '/=== 4\. Benchmark ===/,$p' "$file" | \
    grep -E '^\S+\s+[0-9]+\.[0-9]' | while read -r label time unit rest; do
    time=$(echo "$time" | tr -d ' ')
    case "$unit" in
      s|s\ *) time=$(awk "BEGIN{printf \"%.3f\", $time * 1000}") ;;
    esac
    printf "%s %s\n" "$label" "$time"
  done
}

# Build associative arrays
declare -A BEFORE AFTER

while read -r label time; do
  BEFORE[$label]=$time
done < <(extract_times "$RESULTS/before.txt")

while read -r label time; do
  AFTER[$label]=$time
done < <(extract_times "$RESULTS/after.txt")

# Print comparison for each label found in BEFORE
for label in "${!BEFORE[@]}"; do
  b=${BEFORE[$label]}
  a=${AFTER[$label]:-""}
  if [[ -n "$a" && "$a" != "0" ]]; then
    speedup=$(awk "BEGIN{printf \"%.1fx\", $b / $a}")
    printf "%-14s %12s ms %12s ms %10s\n" "$label" "$b" "$a" "$speedup"
  else
    printf "%-14s %12s ms %12s ms %10s\n" "$label" "$b" "${a:-n/a}" "—"
  fi
done

# Show bulk test comparison
echo
for f in before after; do
  line=$(grep -E "x 'foo: 42'" "$RESULTS/$f.txt" 2>/dev/null || true)
  if [[ -n "$line" ]]; then
    echo "$f: $line"
  fi
done

echo
echo "Results saved to $RESULTS/"
