# Using Python With Local libyamlstar Builds

This branch can run the Python `yamlstar` package against either shared
library backend:

- `libyamlstar.so`: the existing GraalVM native-image backend.
- `libyamlstarglj.so`: the experimental Gloat/Glojure backend.

Both libraries are loaded by the same Python package. The backend is selected
with the `YAMLSTAR_GLOJURE` environment variable at Python import time.

The Gloat/Glojure backend is currently much slower at runtime. Use it for
development and performance work, not as a production replacement yet.

## Repository Layout

Start from the YAMLStar checkout:

```sh
cd /path/to/yamlstar
```

For the Glojure backend, you will usually also need local `gloat` and
`glojure` checkouts. This branch supports `make ext`, which clones them under
`ext/`:

```sh
make ext
```

If you already keep the repositories next to this YAMLStar branch, either
symlink them into `ext/`:

```sh
mkdir -p ext
ln -s ../../gloat ext/gloat
ln -s ../../glojure ext/glojure
```

or pass their paths to the build commands with `GLOAT-DIR` and
`TEST-WITH-GLOJURE`.

## Build the GraalVM Library

The default build is GraalVM:

```sh
make -C libyamlstar build
```

This creates:

```text
libyamlstar/lib/libyamlstar.so
```

Run the Python tests against this backend:

```sh
make -C python test
```

Or try it manually:

```sh
LD_LIBRARY_PATH="$PWD/libyamlstar/lib:$LD_LIBRARY_PATH" \
python3 - <<'PY'
import sys
sys.path.insert(0, "python/lib")
import yamlstar

ys = yamlstar.YAMLStar()
print(ys.version())
print(ys.load("name: YAMLStar\nitems:\n- one\n- two\n"))
PY
```

## Build the Gloat/Glojure Library

The experimental backend is selected with `YAMLSTAR_GLOJURE=1`:

```sh
make ext
YAMLSTAR_GLOJURE=1 make -C libyamlstar build
```

This creates:

```text
libyamlstar/lib/libyamlstarglj.so
```

If you want the build to use sibling checkouts instead of the cached/default
Gloat and Glojure versions, pass them explicitly:

```sh
YAMLSTAR_GLOJURE=1 \
GLOAT-DIR="$PWD/ext/gloat" \
TEST-WITH-GLOJURE="$PWD/ext/glojure" \
make -C libyamlstar build
```

With sibling repos, that usually looks like:

```sh
YAMLSTAR_GLOJURE=1 \
GLOAT-DIR="$PWD/../gloat" \
TEST-WITH-GLOJURE="$PWD/../glojure" \
make -C libyamlstar build
```

### Current Branch Workaround

The Gloat build expects a `.clj` grammar file. If the build fails with:

```text
No rule to make target '../core/src/yamlstar/parser/grammar.clj'
```

create a symlink to the existing `.cljc` grammar:

```sh
ln -sf grammar.cljc core/src/yamlstar/parser/grammar.clj
YAMLSTAR_GLOJURE=1 make -C libyamlstar build
```

## Run Python With the Glojure Backend

Set `YAMLSTAR_GLOJURE=1` when running Python. The Python package will then
look for `libyamlstarglj.so` instead of `libyamlstar.so`.

```sh
YAMLSTAR_GLOJURE=1 \
LD_LIBRARY_PATH="$PWD/libyamlstar/lib:$LD_LIBRARY_PATH" \
python3 - <<'PY'
import sys
sys.path.insert(0, "python/lib")
import yamlstar

ys = yamlstar.YAMLStar()
print(ys.version())
print(ys.load("name: YAMLStar\nitems:\n- one\n- two\n"))
PY
```

Run the Python test suite against the Glojure backend:

```sh
YAMLSTAR_GLOJURE=1 make -C python test
```

## Switch Between Backends

The switch is only the environment variable:

```sh
# GraalVM backend:
unset YAMLSTAR_GLOJURE
make -C python test

# Gloat/Glojure backend:
YAMLSTAR_GLOJURE=1 make -C python test
```

Because the backend is chosen when `yamlstar` is imported, start a new Python
process after changing `YAMLSTAR_GLOJURE`.

## Quick Runtime Comparison

Build both libraries first:

```sh
make -C libyamlstar build
YAMLSTAR_GLOJURE=1 make -C libyamlstar build
```

Then run this small timing script against both backends:

```sh
cat > /tmp/yamlstar-python-bench.py <<'PY'
import os
import sys
import time

sys.path.insert(0, "python/lib")
import yamlstar

sample = "\n".join(
    f"key_{i}: [{i}, value_{i}, true, null]"
    for i in range(200)
)

ys = yamlstar.YAMLStar()

# Warm up import/backend startup effects.
for _ in range(5):
    ys.load(sample)

loops = int(os.environ.get("LOOPS", "100"))
start = time.perf_counter()
for _ in range(loops):
    ys.load(sample)
elapsed = time.perf_counter() - start

backend = "gloat/glojure" if os.environ.get("YAMLSTAR_GLOJURE") else "graalvm"
print(f"{backend}: {elapsed:.3f}s total, {elapsed / loops * 1000:.2f} ms/load")
PY

LD_LIBRARY_PATH="$PWD/libyamlstar/lib:$LD_LIBRARY_PATH" \
LOOPS=100 \
python3 /tmp/yamlstar-python-bench.py

YAMLSTAR_GLOJURE=1 \
LD_LIBRARY_PATH="$PWD/libyamlstar/lib:$LD_LIBRARY_PATH" \
LOOPS=100 \
python3 /tmp/yamlstar-python-bench.py
```

Expect the `gloat/glojure` run to be much slower on this branch. The exact
ratio depends on the Glojure checkout and the input shape.

## Existing Benchmark Scripts

For lower-level Glojure runtime work, this branch also has benchmark helpers:

```sh
make ext
make bench
```

The benchmark path builds a native Go benchmark through Gloat and compares two
Glojure refs. It is useful when optimizing `ext/glojure`, while the Python
timing script above is useful for confirming the end-user Python behavior.
