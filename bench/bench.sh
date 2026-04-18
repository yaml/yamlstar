#!/usr/bin/env bash

# Build a native Go benchmark binary from Clojure sources via gloat,
# then run it.
#
# Prerequisites: run `make ext` to clone ext/glojure and ext/gloat.
# Uses the makes system for Go and gloat tooling.

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

BUILD_DIR=/tmp/yamlstar-bench
BINARY=$BUILD_DIR/bench

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
  $ROOT/bench/bench.clj
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

echo
echo "=== 3. Build binary ==="
# Patch main.go to call bench2/-main
sed -i 's|Var("[^"]*", "-main")|Var("bench2", "-main")|' "$BUILD_DIR/main.go"
sed -i 's|FindOrCreateNamespace(lang.NewSymbol("[^"]*"))|FindOrCreateNamespace(lang.NewSymbol("bench2"))|' "$BUILD_DIR/main.go"
time (cd "$BUILD_DIR" && $GO mod tidy && $GO build -o "$BINARY" .)

echo
echo "=== 4. Benchmark ==="
MAX_LOAD=${MAX_LOAD:-2.0}
while true; do
  load=$(awk '{print $1}' /proc/loadavg)
  if awk "BEGIN{exit !($load < $MAX_LOAD)}"; then
    break
  fi
  printf "Load average %.2f >= %.2f, waiting...\r" "$load" "$MAX_LOAD"
  sleep 5
done
echo "Load average $(awk '{print $1}' /proc/loadavg) — starting benchmark"
"$BINARY"
echo "Load average $(awk '{print $1}' /proc/loadavg) — benchmark complete"
