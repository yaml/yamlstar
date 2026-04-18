#!/usr/bin/env bash

# Build a profiling binary and collect CPU + memory profiles.
# Usage: ./profile.sh
#   REPS=100 ./profile.sh   # more iterations for better data
#
# Prerequisites: run `make ext` to clone ext/glojure and ext/gloat.
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

GLOJURE=$ROOT/ext/glojure
GLOAT_EXT=$ROOT/ext/gloat
CORE=$ROOT/core/src/yamlstar

# Verify ext/ repos exist
for d in "$GLOJURE" "$GLOAT_EXT"; do
  if [[ ! -d "$d" ]]; then
    echo "ERROR: $d not found. Run 'make ext' first." >&2
    exit 1
  fi
done

# Get tool paths from gloat's makes system
cd "$GLOAT_EXT"
make --quiet path-deps >/dev/null 2>&1 || true
GLOAT_BIN=$GLOAT_EXT/bin
GLOAT=$GLOAT_BIN/gloat

# Find Go via makes cache
GO=$(cd "$GLOAT_EXT" && make shell cmd='which go' 2>/dev/null | tail -1)
if [[ -z "$GO" || ! -x "$GO" ]]; then
  echo "ERROR: Cannot find Go. Run 'make -C ext/gloat path-deps'." >&2
  exit 1
fi

# Find glj (gloat installs it)
GLJ=$(cd "$GLOAT_EXT" && make shell cmd='which glj' 2>/dev/null | tail -1)

cd "$ROOT"

BUILD_DIR=/tmp/yamlstar-profile
BINARY=$BUILD_DIR/profile
PROFILE_DIR=$ROOT/bench/profiles
mkdir -p "$PROFILE_DIR"

# Gloat requires .clj extensions; create .clj symlinks for .cljc files.
LINK_DIR=$ROOT/bench/cache/gloat-srcs
rm -rf "$LINK_DIR"
mkdir -p "$LINK_DIR/parser"
for f in prelude parser receiver grammar; do
  ln -sf "$CORE/parser/$f.cljc" "$LINK_DIR/parser/$f.clj"
done
ln -sf "$CORE/constructor.cljc" "$LINK_DIR/constructor.clj"

SRCS=(
  $LINK_DIR/parser/prelude.clj
  $LINK_DIR/parser/parser.clj
  $LINK_DIR/parser/receiver.clj
  $LINK_DIR/parser/grammar.clj
  $CORE/parser.clj
  $CORE/composer.clj
  $CORE/resolver.clj
  $LINK_DIR/constructor.clj
  $CORE/core.clj
  $ROOT/bench/profile.clj
)

echo "=== 1. Build glj from ext/glojure ==="
time (cd "$GLOJURE" && $GO build -o "$GLJ" ./cmd/glj)

# Patch gloat's template go.mod to use ext/glojure.
for gomod in "$GLOAT_EXT/template/go.mod" "$GLOAT_EXT/ys/pkg/go.mod"; do
  [[ -f "$gomod" ]] || continue
  if grep -q 'replace github.com/gloathub/glojure =>' "$gomod"; then
    sed -i "s|replace github.com/gloathub/glojure => .*|replace github.com/gloathub/glojure => $GLOJURE|" "$gomod"
  else
    echo "replace github.com/gloathub/glojure => $GLOJURE" >> "$gomod"
  fi
done

echo
echo "=== 2. Compile to Go project ==="
time (</dev/null env -u GOROOT "$GLOAT" "${SRCS[@]}" -o "$BUILD_DIR/" --force)

# Patch the generated go.mod to use ext/glojure.
if grep -q 'replace github.com/gloathub/glojure =>' "$BUILD_DIR/go.mod"; then
  sed -i "s|replace github.com/gloathub/glojure => .*|replace github.com/gloathub/glojure => $GLOJURE|" \
    "$BUILD_DIR/go.mod"
else
  echo "replace github.com/gloathub/glojure => $GLOJURE" >> "$BUILD_DIR/go.mod"
fi

# Patch main.go to call profile/-main
sed -i 's|Var("[^"]*", "-main")|Var("profile", "-main")|' "$BUILD_DIR/main.go"
sed -i 's|FindOrCreateNamespace(lang.NewSymbol("[^"]*"))|FindOrCreateNamespace(lang.NewSymbol("profile"))|' "$BUILD_DIR/main.go"

# Add profiling wrapper
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

# Inject profiling around main
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
