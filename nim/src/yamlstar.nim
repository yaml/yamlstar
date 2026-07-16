# Copyright 2023-2026 Ingy dot Net
# This code is licensed under MIT license (See License for details)

## Nim binding/API for the libyamlstar shared library.
##
## This module is a Nim port of the Python 'yamlstar' module, which
## is the reference implementation for YAMLStar FFI bindings to
## libyamlstar.
##
## The current user facing API consists of a single type,
## `YAMLStar`, which has a single method: `load(string)`.
## The load() method takes a YAML string as input and returns
## the JsonNode that YAMLStar loads.

import std/[dynlib, json, os, strutils]

# This value is automatically updated by 'make bump'.
# The version number is used to find the correct shared library file.
# We currently only support binding to an exact version of libyamlstar.
const yamlstarVersion* = "0.1.16"

# We currently only support platforms that GraalVM supports.
# Windows uses an unversioned file name, matching the Python binding:
when defined(linux):
  const libyamlstarName = "libyamlstar.so." & yamlstarVersion
elif defined(macosx):
  const libyamlstarName = "libyamlstar.dylib." & yamlstarVersion
elif defined(windows):
  const libyamlstarName = "libyamlstar.dll"
else:
  {.error: "Unsupported platform for yamlstar.".}

type
  ## Error raised by the YAMLStar loader:
  YAMLStarError* = object of CatchableError

  # FFI signatures for the 3 libyamlstar functions used by this binding:
  CreateIsolateFn = proc (
    params: pointer, isolate: ptr pointer, thread: ptr pointer,
  ): cint {.cdecl.}
  TearDownIsolateFn = proc (thread: pointer): cint {.cdecl.}
  LoadYamlstarFn = proc (
    thread: pointer, input: cstring,
  ): cstring {.cdecl.}

  ## The YAMLStar type is the main user facing API for this module.
  YAMLStar* = ref object
    lib: LibHandle
    isolateThread: pointer
    loadYamlstar: LoadYamlstarFn
    tearDownIsolate: TearDownIsolateFn
    ## The error object from the last load() call, if any:
    error*: JsonNode

# Find the libyamlstar shared library file path.
# Search the platform library path entries, then common install
# locations:
proc findLibyamlstarPath(): string =
  when defined(windows):
    const envName = "PATH"
    const sep = ';'
  else:
    const envName = "LD_LIBRARY_PATH"
    const sep = ':'

  var paths: seq[string]
  let libraryPath = getEnv(envName)
  if libraryPath.len > 0:
    for dir in libraryPath.split(sep):
      if dir.len > 0:
        paths.add(dir)
  when not defined(windows):
    paths.add("/usr/local/lib")
  var home = getEnv("HOME")
  if home.len == 0:
    home = getEnv("USERPROFILE")
  if home.len > 0:
    paths.add(home / ".local" / "lib")

  for dir in paths:
    let path = dir / libyamlstarName
    if fileExists(path):
      return path

  raise newException(YAMLStarError, """
Shared library file '$1' not found
Try: curl https://yamlstar.org/install | VERSION=$2 LIB=1 bash
See: https://github.com/yaml/yamlstar/wiki/Installing-YAMLStar
""" % [libyamlstarName, yamlstarVersion])

# Load a symbol from libyamlstar or fail:
proc symbol(lib: LibHandle, name: string): pointer =
  result = lib.symAddr(name.cstring)
  if result == nil:
    raise newException(
      YAMLStarError, "Symbol '" & name & "' not found in libyamlstar")

## Load libyamlstar and create a GraalVM isolate for the life of the
## YAMLStar instance.
proc newYAMLStar*(): YAMLStar =
  let lib = loadLib(findLibyamlstarPath())
  if lib == nil:
    raise newException(
      YAMLStarError, "Failed to load shared library '" &
      libyamlstarName & "'")

  result = YAMLStar(lib: lib)
  let createIsolate =
    cast[CreateIsolateFn](lib.symbol("graal_create_isolate"))
  result.tearDownIsolate =
    cast[TearDownIsolateFn](lib.symbol("graal_tear_down_isolate"))
  result.loadYamlstar =
    cast[LoadYamlstarFn](lib.symbol("yamlstar_load"))

  # Create a new GraalVM isolatethread for the instance:
  if createIsolate(nil, nil, addr result.isolateThread) != 0:
    raise newException(YAMLStarError, "Failed to create isolate")

## Load a YAML string and return the result.
proc load*(ys: YAMLStar, input: string): JsonNode =
  # Reset any previous error:
  ys.error = nil

  # Call 'yamlstar_load' function in libyamlstar shared library:
  let respPtr = ys.loadYamlstar(ys.isolateThread, input.cstring)
  if respPtr == nil:
    raise newException(YAMLStarError, "Null response from 'libyamlstar'")

  # Decode the JSON response:
  let resp = parseJson($respPtr)

  # Check for libyamlstar error in JSON response:
  if resp.hasKey("error"):
    ys.error = resp["error"]
    raise newException(YAMLStarError, ys.error["cause"].getStr)

  # Get the response object from loading the YAML string:
  if not resp.hasKey("data"):
    raise newException(
      YAMLStarError, "Unexpected response from 'libyamlstar'")

  # Return the response object:
  resp["data"]

## Tear down the GraalVM isolate and close libyamlstar.
proc close*(ys: YAMLStar) =
  if ys.lib != nil:
    if ys.tearDownIsolate(ys.isolateThread) != 0:
      raise newException(
        YAMLStarError, "Failed to tear down isolate")
    unloadLib(ys.lib)
    ys.lib = nil
