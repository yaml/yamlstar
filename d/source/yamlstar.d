// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

/++
D binding/API for the libyamlstar shared library.

This module is a D port of the Python 'yamlstar' module, which is
the reference implementation for YAMLStar FFI bindings to libyamlstar.

The current user facing API consists of a single class, `YAMLStar`,
which has a single method: `.load(string)`.
The load() method takes a YAML string as input and returns the
JSONValue that YAMLStar loads.
+/
module yamlstar;

import std.conv : to;
import std.file : exists;
import std.json;
import std.process : environment;
import std.string : fromStringz, split, toStringz;

// This value is automatically updated by 'make bump'.
// The version number is used to find the correct shared library file.
// We currently only support binding to an exact version of libyamlstar.
enum yamlstarVersion = "0.1.16";

// We currently only support platforms that GraalVM supports.
// Windows uses an unversioned file name, matching the Python binding:
version (linux)
  enum libyamlstarName = "libyamlstar.so." ~ yamlstarVersion;
else version (OSX)
  enum libyamlstarName = "libyamlstar.dylib." ~ yamlstarVersion;
else version (Windows)
  enum libyamlstarName = "libyamlstar.dll";
else
  static assert(0, "Unsupported platform for yamlstar.");

version (Posix)
{
  import core.sys.posix.dlfcn : dlopen, dlsym, dlclose, RTLD_NOW;
}
version (Windows)
{
  import core.sys.windows.winbase :
    LoadLibraryA, GetProcAddress, FreeLibrary;
}

// FFI signatures for the 3 libyamlstar functions used by this binding:
extern (C)
{
  alias CreateIsolateFn = int function(void*, void**, void**);
  alias TearDownIsolateFn = int function(void*);
  alias LoadYamlstarFn = char* function(void*, const(char)*);
}

/// Exception thrown by the YAMLStar loader.
class YAMLStarException : Exception
{
  this(string msg, string file = __FILE__, size_t line = __LINE__)
  {
    super(msg, file, line);
  }
}

// Find the libyamlstar shared library file path.
// Search the platform library path entries, then common install
// locations:
private string findLibyamlstarPath()
{
  version (Windows)
  {
    enum envName = "PATH";
    enum sep = ";";
    enum dirSep = "\\";
  }
  else
  {
    enum envName = "LD_LIBRARY_PATH";
    enum sep = ":";
    enum dirSep = "/";
  }

  string[] paths;
  const libraryPath = environment.get(envName);
  if (libraryPath !is null)
  {
    foreach (dir; libraryPath.split(sep))
      if (dir.length > 0)
        paths ~= dir;
  }
  version (Posix)
    paths ~= "/usr/local/lib";
  auto home = environment.get("HOME");
  if (home is null)
    home = environment.get("USERPROFILE");
  if (home !is null)
    paths ~= home ~ dirSep ~ ".local" ~ dirSep ~ "lib";

  foreach (dir; paths)
  {
    const path = dir ~ dirSep ~ libyamlstarName;
    if (path.exists)
      return path;
  }

  throw new YAMLStarException(
    "Shared library file '" ~ libyamlstarName ~ "' not found\n" ~
    "Try: curl https://yamlstar.org/install | " ~
    "VERSION=" ~ yamlstarVersion ~ " LIB=1 bash\n" ~
    "See: https://github.com/yaml/yamlstar/wiki/" ~
    "Installing-YAMLStar\n");
}

/++
The YAMLStar class is the main user facing API for this module.

Usage:
---
import yamlstar;
auto yaml = new YAMLStar();
auto data = yaml.load(readText("file.ys"));
---
+/
class YAMLStar
{
  private void* lib;
  private void* isolateThread;
  private LoadYamlstarFn loadYamlstar;
  private TearDownIsolateFn tearDownIsolate;

  /// The error object from the last load() call, if any.
  JSONValue error;

  // Load libyamlstar and create a GraalVM isolate for the life of the
  // YAMLStar instance:
  this()
  {
    const path = findLibyamlstarPath();
    version (Posix)
      lib = dlopen(path.toStringz, RTLD_NOW);
    version (Windows)
      lib = LoadLibraryA(path.toStringz);
    if (lib is null)
      throw new YAMLStarException(
        "Failed to load shared library '" ~ path ~ "'");

    auto createIsolate =
      cast(CreateIsolateFn) symbol("graal_create_isolate");
    tearDownIsolate =
      cast(TearDownIsolateFn) symbol("graal_tear_down_isolate");
    loadYamlstar =
      cast(LoadYamlstarFn) symbol("yamlstar_load");

    // Create a new GraalVM isolatethread for the instance:
    if (createIsolate(null, null, &isolateThread) != 0)
      throw new YAMLStarException("Failed to create isolate");
  }

  private void* symbol(string name)
  {
    version (Posix)
      auto sym = dlsym(lib, name.toStringz);
    version (Windows)
      auto sym = GetProcAddress(lib, name.toStringz);
    if (sym is null)
      throw new YAMLStarException(
        "Symbol '" ~ name ~ "' not found in libyamlstar");
    return cast(void*) sym;
  }

  /// Load a YAML string and return the result.
  JSONValue load(string input)
  {
    // Reset any previous error:
    error = JSONValue.init;

    // Call 'yamlstar_load' function in libyamlstar shared library:
    auto respPtr = loadYamlstar(isolateThread, input.toStringz);
    if (respPtr is null)
      throw new YAMLStarException("Null response from 'libyamlstar'");

    // Decode the JSON response:
    const resp = respPtr.fromStringz.to!string.parseJSON;

    // Check for libyamlstar error in JSON response:
    if ("error" in resp)
    {
      error = resp["error"];
      throw new YAMLStarException(error["cause"].str);
    }

    // Get the response object from loading the YAML string:
    if ("data" !in resp)
      throw new YAMLStarException("Unexpected response from 'libyamlstar'");

    // Return the response object:
    return resp["data"];
  }

  /// Tear down the GraalVM isolate and close libyamlstar:
  void close()
  {
    if (lib is null)
      return;
    if (tearDownIsolate(isolateThread) != 0)
      throw new YAMLStarException("Failed to tear down isolate");
    version (Posix)
      dlclose(lib);
    version (Windows)
      FreeLibrary(lib);
    lib = null;
  }
}

unittest
{
  auto yaml = new YAMLStar();
  scope (exit)
    yaml.close();

  // Load YAML mapping:
  auto data = yaml.load("test: 42");
  assert(data["test"].integer == 42);

  // Load plain YAML:
  data = yaml.load("foo: bar");
  assert(data["foo"].str == "bar");

  // Load invalid input throws and sets error:
  bool threw = false;
  try
    yaml.load(`key: "unclosed`);
  catch (YAMLStarException)
    threw = true;
  assert(threw);
  assert(!yaml.error.isNull);

  // Load multiple times on one instance:
  data = yaml.load("test: 42");
  assert(data["test"].integer == 42);
}
