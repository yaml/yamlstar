# Copyright 2023-2026 Ingy dot Net
# This code is licensed under MIT license (See License for details)

# Test the yamlstar Nim binding.

import std/json

import yamlstar

var fails = 0

proc check(cond: bool, label: string) =
  if cond:
    echo "ok - ", label
  else:
    echo "not ok - ", label
    inc fails

let yaml = newYAMLStar()

# Load YAML mapping:
var data = yaml.load("test: 42")
check(data["test"].getInt == 42, "load mapping")

# Load plain YAML:
data = yaml.load("foo: bar")
check(data["foo"].getStr == "bar", "load plain yaml")

# Load invalid input raises and sets error:
var threw = false
try:
  discard yaml.load("key: \"unclosed")
except YAMLStarError:
  threw = true
check(threw, "load error raises")
check(yaml.error != nil, "error object is set")

# Load multiple times on one instance:
data = yaml.load("test: 42")
check(data["test"].getInt == 42, "load multiple times")

yaml.close()

if fails > 0:
  echo fails, " test(s) failed"
  quit(1)
