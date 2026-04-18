#!/usr/bin/env python3

"""Benchmark yamlstar load times with detailed per-phase timing."""

import ctypes
import json
import os
import sys
import time

# --------------- direct ctypes binding (bypass yamlstar module) ---------------

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
so_path = os.path.join(root, 'libyamlstar', 'lib', 'libyamlstar.so')
if not os.path.exists(so_path):
    sys.exit("libyamlstar.so not found at: " + so_path)
print("Library: %s  (%d MB)" % (so_path, os.path.getsize(so_path) // (1024*1024)))

t0 = time.perf_counter()
lib = ctypes.CDLL(so_path)
t_dlopen = time.perf_counter() - t0

yamlstar_load = lib.yamlstar_load
yamlstar_load.argtypes = [ctypes.c_char_p, ctypes.c_char_p]
yamlstar_load.restype = ctypes.c_char_p

# --------------- test inputs --------------------------------------------------

INPUTS = {
    "scalar":    "hello",
    "mapping":   "foo: 42",
    "nested":    """\
root:
  child1:
    key: value
  child2:
  - item1
  - item2
  - item3""",
    "types":     """\
string: hello
integer: 42
float: 3.14
bool: true
null_val: null""",
}

OPTS = ctypes.c_char_p(b"{}")

# --------------- helpers ------------------------------------------------------

def call_load(yaml_bytes):
    """Raw FFI call + JSON decode. Returns (result, ffi_ms, json_ms)."""
    t1 = time.perf_counter()
    raw = yamlstar_load(ctypes.c_char_p(yaml_bytes), OPTS)
    t2 = time.perf_counter()
    resp = json.loads(raw.decode())
    t3 = time.perf_counter()
    err = resp.get('error')
    if err:
        raise Exception(err['cause'])
    return resp['data'], (t2 - t1) * 1000, (t3 - t2) * 1000

def fmt(ms):
    if ms >= 1000:
        return "%8.2f s" % (ms / 1000)
    if ms < 1:
        return "%7.3f ms" % ms
    return "%7.1f ms" % ms

# --------------- benchmark ----------------------------------------------------

print()
print("dlopen:  %s" % fmt(t_dlopen * 1000))

# Cold call (first ever call initializes Go runtime / Clojure namespaces)
yaml_bytes = b"warmup: true"
t0 = time.perf_counter()
raw = yamlstar_load(ctypes.c_char_p(yaml_bytes), OPTS)
t_cold = (time.perf_counter() - t0) * 1000
_ = json.loads(raw.decode())
print("cold:    %s  (first call, includes Go/ns init)" % fmt(t_cold))

# Single call per input
limit_ms = int(os.environ['LIMIT']) if 'LIMIT' in os.environ else None

print()
print("%-12s %12s" % ("input", "time"))
print("-" * 26)

for name, yaml_str in INPUTS.items():
    yaml_bytes = yaml_str.encode('utf-8')
    _, ffi_ms, _ = call_load(yaml_bytes)
    print("%-12s %s" % (name, fmt(ffi_ms)))
    if name == "scalar" and limit_ms is not None and ffi_ms > limit_ms:
        print("(skipping remaining — scalar exceeded %d ms limit)" % limit_ms)
        break

# Bulk test — pick iteration count based on speed of first call
yaml_bytes = b"foo: 42"
_, probe_ms, _ = call_load(yaml_bytes)

if probe_ms < 10:
    reps = 100
elif probe_ms < 500:
    reps = 10
else:
    reps = 3

print()
t0 = time.perf_counter()
for _ in range(reps):
    yamlstar_load(ctypes.c_char_p(yaml_bytes), OPTS)
t_total = (time.perf_counter() - t0) * 1000
print("%d x 'foo: 42':  %s total,  %s/call" % (
    reps, fmt(t_total), fmt(t_total / reps)))
