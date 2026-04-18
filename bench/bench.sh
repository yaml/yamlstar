#!/usr/bin/env bash

# Build a native Go benchmark binary from Clojure sources via gloat,
# then run it.
#
# Prerequisites: run `make ext` to clone ext/glojure and ext/gloat.
# Accepts GO and GLOAT_EXT env vars (set by `make bench`).

set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)

GLOJURE=$ROOT/ext/glojure
GLOAT_EXT=${GLOAT_EXT:-$ROOT/ext/gloat}
CORE=$ROOT/core/src/yamlstar

# Verify ext/ repos exist
for d in "$GLOJURE" "$GLOAT_EXT"; do
  if [[ ! -d "$d" ]]; then
    echo "ERROR: $d not found. Run 'make ext' first." >&2
    exit 1
  fi
done

# Use GO from env, or discover via gloat's makes system
if [[ -z "${GO:-}" ]]; then
  GO=$(cd "$GLOAT_EXT" && make shell cmd='which go' 2>/dev/null | tail -1)
fi
if [[ -z "$GO" || ! -x "$GO" ]]; then
  echo "ERROR: Cannot find Go. Use 'make bench' or set GO=." >&2
  exit 1
fi

# Set glj and gloat binary paths
GLJ=$GLOAT_EXT/bin/glj
GLOAT=$GLOAT_EXT/bin/gloat

cd "$ROOT"

# Use /tmp for Go caches to avoid permission issues with read-only cache files
export GOPATH=/tmp/yamlstar-gopath
export GOCACHE=/tmp/yamlstar-gocache
export GOMODCACHE=/tmp/yamlstar-gomodcache
mkdir -p "$GOPATH" "$GOCACHE" "$GOMODCACHE"
chmod -R u+w "$GOPATH" "$GOCACHE" "$GOMODCACHE" 2>/dev/null || true

BUILD_DIR=/tmp/yamlstar-bench
rm -rf "$BUILD_DIR"
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
MAX_LOAD=${MAX_LOAD:-1.0}
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
