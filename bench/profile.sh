#!/usr/bin/env bash

# Build a profiling binary and collect CPU + memory profiles.
# Usage: ./profile.sh [glojure-commit]
#   REPS=100 ./profile.sh   # more iterations for better data
#
# Output goes to bench/profiles/:
#   cpu.prof, mem.prof      — raw pprof data
#   cpu-top.txt             — top CPU consumers
#   mem-alloc-space.txt     — top memory consumers (bytes)
#   mem-alloc-objects.txt   — top allocation sites (count)
#
# Share the .txt files for analysis, or explore interactively:
#   go tool pprof -http=:8080 bench/profiles/cpu.prof

set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
GLOJURE=$ROOT/../glojure
COMMIT=${1:-gloat}

echo "=== 0. Clone glojure @ $COMMIT ==="
GLOJURE_BUILD=/tmp/glojure/$COMMIT
rm -fr "$GLOJURE_BUILD"
git clone -q "$GLOJURE/.git" "$GLOJURE_BUILD"
git -C "$GLOJURE_BUILD" checkout -q "$COMMIT"

# Auto-detect local root and gloat installation
GLOAT_HOME=$HOME/.local/share/gloat
if [[ -d /tmp/yamlstar-local/go-1.26.0 ]]; then
  LOCAL=/tmp/yamlstar-local
  GLOAT_DIR=$LOCAL/cache/gloat-main
elif [[ -d $ROOT/.cache/.local/go-1.26.0 ]]; then
  LOCAL=$ROOT/.cache/.local
  GLOAT_DIR=$LOCAL/cache/gloat-main
elif [[ -d $GLOAT_HOME/.cache/.local/go-1.26.0 ]]; then
  LOCAL=$GLOAT_HOME/.cache/.local
  GLOAT_DIR=$GLOAT_HOME
else
  echo "ERROR: Cannot find Go installation" >&2
  exit 1
fi

GO=$LOCAL/go-1.26.0/bin/go
GLOAT=$GLOAT_DIR/bin/gloat
GLJ=$LOCAL/bin/glj

# Find gloat's own glj path (may differ from $GLJ).
# Always query from GLOAT_HOME which has the Makefile with gloat-vars.
GLOAT_GLJ=$(cd "$GLOAT_HOME" && make --quiet --no-print-directory gloat-vars 2>/dev/null \
  | grep ':GLJ ' | sed 's/.*:GLJ "\(.*\)"/\1/')

echo "[diag] LOCAL=$LOCAL"
echo "[diag] GLOAT_DIR=$GLOAT_DIR"
echo "[diag] GLOAT_HOME=$GLOAT_HOME"
echo "[diag] GLJ=$GLJ"
echo "[diag] GLOAT_GLJ=$GLOAT_GLJ"
echo "[diag] GLOAT=$GLOAT"

BUILD_DIR=/tmp/yamlstar-profile
BINARY=$BUILD_DIR/profile
PROFILE_DIR=$ROOT/bench/profiles
mkdir -p "$PROFILE_DIR"

SRCS=(
  $ROOT/core/src/yamlstar/parser/prelude.clj
  $ROOT/core/src/yamlstar/parser/parser.clj
  $ROOT/core/src/yamlstar/parser/receiver.clj
  $ROOT/core/src/yamlstar/parser/grammar.clj
  $ROOT/core/src/yamlstar/parser.clj
  $ROOT/core/src/yamlstar/composer.clj
  $ROOT/core/src/yamlstar/resolver.clj
  $ROOT/core/src/yamlstar/constructor.clj
  $ROOT/core/src/yamlstar/core.clj
  $ROOT/bench/profile.clj
)

# Patch gloat's template go.mod to use the cloned glojure.
# The template may not have a replace line, so append one if missing.
for gomod in "$GLOAT_DIR/template/go.mod" "$GLOAT_DIR/ys/pkg/go.mod"; do
  if grep -q 'replace github.com/gloathub/glojure =>' "$gomod"; then
    sed -i "s|replace github.com/gloathub/glojure => .*|replace github.com/gloathub/glojure => $GLOJURE_BUILD|" "$gomod"
  else
    echo "replace github.com/gloathub/glojure => $GLOJURE_BUILD" >> "$gomod"
  fi
done

echo
echo "=== 1. Build glj from $COMMIT ==="
time (cd "$GLOJURE_BUILD" && $GO build -o "$GLJ" ./cmd/glj)

# Verify the built glj has ApplyN codegen
APPLY_N=$(strings "$GLJ" | grep -c 'lang\.Apply[0-4]' || true)
echo "[diag] glj at $GLJ has $APPLY_N ApplyN references"
echo "[diag] glj timestamp: $(ls -la "$GLJ")"

# Copy to gloat's glj path if different, so gloat uses our build
if [[ -n "$GLOAT_GLJ" && "$GLOAT_GLJ" != "$GLJ" ]]; then
  echo "[diag] Copying glj to gloat path: $GLOAT_GLJ"
  cp "$GLJ" "$GLOAT_GLJ"
  touch "$GLOAT_GLJ"
  echo "[diag] gloat glj timestamp: $(ls -la "$GLOAT_GLJ")"
else
  echo "[diag] No copy needed (GLOAT_GLJ='$GLOAT_GLJ', GLJ='$GLJ')"
fi
touch "$GLJ"

echo
echo "=== 2. Compile to Go project ==="
time (</dev/null env -u GOROOT "$GLOAT" "${SRCS[@]}" -o "$BUILD_DIR/" --force)

# Check if gloat overwrote our glj
APPLY_N_AFTER=$(strings "$GLJ" | grep -c 'lang\.Apply[0-4]' || true)
echo "[diag] glj at $GLJ has $APPLY_N_AFTER ApplyN references AFTER gloat ran"
echo "[diag] glj timestamp after gloat: $(ls -la "$GLJ")"
if [[ -n "$GLOAT_GLJ" && "$GLOAT_GLJ" != "$GLJ" ]]; then
  GLOAT_APPLY_N_AFTER=$(strings "$GLOAT_GLJ" | grep -c 'lang\.Apply[0-4]' || true)
  echo "[diag] gloat glj at $GLOAT_GLJ has $GLOAT_APPLY_N_AFTER ApplyN references AFTER gloat ran"
  echo "[diag] gloat glj timestamp after gloat: $(ls -la "$GLOAT_GLJ")"
fi

# Check generated code for ApplyN vs Apply
echo "[diag] Generated code analysis:"
for f in "$BUILD_DIR"/pkg/yamlstar/*/loader.go; do
  name=$(basename "$(dirname "$f")")
  apply_n=$(grep -c 'lang\.Apply[0-4](' "$f" || true)
  apply=$(grep -c 'lang\.Apply(' "$f" || true)
  fnfunc_n=$(grep -c 'lang\.FnFunc[0-4](' "$f" || true)
  newfnfunc=$(grep -c 'lang\.NewFnFunc(' "$f" || true)
  echo "[diag]   $name: ApplyN=$apply_n Apply=$apply FnFuncN=$fnfunc_n NewFnFunc=$newfnfunc"
done

# Patch the generated go.mod to use the cloned glojure.
if grep -q 'replace github.com/gloathub/glojure =>' "$BUILD_DIR/go.mod"; then
  sed -i "s|replace github.com/gloathub/glojure => .*|replace github.com/gloathub/glojure => $GLOJURE_BUILD|" \
    "$BUILD_DIR/go.mod"
else
  echo "replace github.com/gloathub/glojure => $GLOJURE_BUILD" >> "$BUILD_DIR/go.mod"
fi

# Patch main.go to call profile/-main
sed -i 's|Var("[^"]*", "-main")|Var("profile", "-main")|' "$BUILD_DIR/main.go"
sed -i 's|FindOrCreateNamespace(lang.NewSymbol("[^"]*"))|FindOrCreateNamespace(lang.NewSymbol("profile"))|' "$BUILD_DIR/main.go"

# Add profiling wrapper — a separate Go file that instruments main
cat > "$BUILD_DIR/profiling.go" << 'GOEOF'
package main

import (
	"fmt"
	"os"
	"runtime"
	"runtime/pprof"
)

var cpuFile *os.File
var profileDir string

func startProfiling(dir string) {
	profileDir = dir
	var err error
	cpuFile, err = os.Create(dir + "/cpu.prof")
	if err != nil {
		fmt.Fprintf(os.Stderr, "cpu profile: %v\n", err)
		return
	}
	pprof.StartCPUProfile(cpuFile)
}

func stopProfiling() {
	pprof.StopCPUProfile()
	cpuFile.Close()

	runtime.GC()
	memFile, err := os.Create(profileDir + "/mem.prof")
	if err != nil {
		fmt.Fprintf(os.Stderr, "mem profile: %v\n", err)
		return
	}
	pprof.WriteHeapProfile(memFile)
	memFile.Close()
}
GOEOF

# Patch main.go to call startProfiling/stopProfiling around the workload.
# The Clojure -main does warmup first, THEN the profiled iterations.
# We inject profiling around the whole -main; the warmup is quick relative
# to 50+ profiled iterations so it won't skew results much.
PROFILE_DIR_ESCAPED=$(printf '%s' "$PROFILE_DIR" | sed 's|/|\\/|g')
sed -i "s|func main() {|func main() {\n\tstartProfiling(\"${PROFILE_DIR_ESCAPED}\")\n\tdefer stopProfiling()|" \
  "$BUILD_DIR/main.go"

echo
echo "=== 3. Build binary ==="
time (cd "$BUILD_DIR" && $GO mod tidy && $GO build -o "$BINARY" .)

echo
echo "=== 4. Profile ==="
MAX_LOAD=${MAX_LOAD:-2.0}
while true; do
  load=$(awk '{print $1}' /proc/loadavg)
  if awk "BEGIN{exit !($load < $MAX_LOAD)}"; then
    break
  fi
  printf "Load average %.2f >= %.2f, waiting...\r" "$load" "$MAX_LOAD"
  sleep 5
done
echo "Load average $(awk '{print $1}' /proc/loadavg) — starting profile"
REPS=${REPS:-5} "$BINARY"
echo "Load average $(awk '{print $1}' /proc/loadavg) — profile complete"

echo
echo "=== 5. Generate reports ==="

$GO tool pprof -text -nodecount=40 "$BINARY" "$PROFILE_DIR/cpu.prof" \
  > "$PROFILE_DIR/cpu-top.txt" 2>&1

$GO tool pprof -text -nodecount=40 -alloc_space "$BINARY" "$PROFILE_DIR/mem.prof" \
  > "$PROFILE_DIR/mem-alloc-space.txt" 2>&1

$GO tool pprof -text -nodecount=40 -alloc_objects "$BINARY" "$PROFILE_DIR/mem.prof" \
  > "$PROFILE_DIR/mem-alloc-objects.txt" 2>&1

echo
echo "Profiles written to $PROFILE_DIR/"
echo
echo "Reports:"
ls -lh "$PROFILE_DIR"/*.txt "$PROFILE_DIR"/*.prof
echo
echo "=== CPU top 20 ==="
head -30 "$PROFILE_DIR/cpu-top.txt"
echo
echo "=== Memory (alloc space) top 20 ==="
head -30 "$PROFILE_DIR/mem-alloc-space.txt"
echo
echo "To explore interactively:"
echo "  $GO tool pprof -http=:8080 $BINARY $PROFILE_DIR/cpu.prof"
