#!/usr/bin/env bash

# Build a native Go benchmark binary from Clojure sources via gloat,
# then run it. Usage: ./bench2.sh [glojure-commit]
# Assumes deps already bootstrapped (run make build once).

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

BUILD_DIR=/tmp/yamlstar-bench
BINARY=$BUILD_DIR/bench

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
  $ROOT/bench/bench2.clj
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
# Copy to gloat's glj path if different, so gloat uses our build
if [[ -n "$GLOAT_GLJ" && "$GLOAT_GLJ" != "$GLJ" ]]; then
  echo "Copying glj to gloat path: $GLOAT_GLJ"
  cp "$GLJ" "$GLOAT_GLJ"
  touch "$GLOAT_GLJ"
fi
touch "$GLJ"

echo
echo "=== 2. Compile to Go project ==="
time (</dev/null env -u GOROOT "$GLOAT" "${SRCS[@]}" -o "$BUILD_DIR/" --force)

# Patch the generated go.mod to use the cloned glojure.
if grep -q 'replace github.com/gloathub/glojure =>' "$BUILD_DIR/go.mod"; then
  sed -i "s|replace github.com/gloathub/glojure => .*|replace github.com/gloathub/glojure => $GLOJURE_BUILD|" \
    "$BUILD_DIR/go.mod"
else
  echo "replace github.com/gloathub/glojure => $GLOJURE_BUILD" >> "$BUILD_DIR/go.mod"
fi

echo
echo "=== 3. Build binary ==="
# Patch main.go to call bench2/-main instead of whatever gloat picked
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
