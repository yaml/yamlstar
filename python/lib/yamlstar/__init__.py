# Copyright 2024 yaml.org
# MIT License

"""
Python binding/API for the libyamlstar shared library.

This module provides a Python interface to YAMLStar, a pure YAML 1.2 loader.
The YAMLStar class has methods for loading YAML documents and converting
them to Python objects.
"""

# Version matching the yamlstar shared library
yamlstar_version = '0.1.18'

import os
import sys
import ctypes
import json

# Require Python 3.6 or greater:
assert sys.version_info >= (3, 6), \
  "Python 3.6 or greater required for 'yamlstar'."

def find_libyamlstar():
  """Find the libyamlstar shared library."""
  if sys.platform == 'linux' or sys.platform.startswith('freebsd'):
    so = 'so'
  elif sys.platform == 'darwin':
    so = 'dylib'
  elif sys.platform == 'win32':
    so = 'dll'
  else:
    raise Exception(
      "Unsupported platform '%s' for yamlstar." % sys.platform)

  # Use the platform library path, plus package and common install locations.
  if sys.platform == 'win32':
    library_path = os.environ.get('PATH')
    library_paths = library_path.split(';') if library_path else []
  else:
    library_path = os.environ.get('LD_LIBRARY_PATH')
    library_paths = library_path.split(':') if library_path else []
  library_paths.append(
    os.path.join(os.path.dirname(__file__), 'libyamlstar'))
  if sys.platform != 'win32':
    library_paths.append('/usr/local/lib')
  home = os.environ.get('HOME') or os.path.expanduser('~')
  if home:
    library_paths.append(os.path.join(home, '.local', 'lib'))

  # Also check relative to this file (for development)
  lib_path = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
    '..', 'libyamlstar', 'lib')
  library_paths.insert(0, os.path.abspath(lib_path))

  lib_name = 'libyamlstar'
  filename = "%s.%s" % (lib_name, so)
  for path in library_paths:
    full_path = os.path.join(path, filename)
    if os.path.isfile(full_path):
      return full_path

  raise Exception(
    """\
Shared library file '%s' not found
Search paths: %s
Build with: cd libyamlstar && make build
""" % (filename, os.pathsep.join(library_paths)))

# Load libyamlstar shared library:
_libyamlstar_path = find_libyamlstar()
libyamlstar = ctypes.CDLL(_libyamlstar_path)

# Public functions use the stable thread-first libyamlstar ABI. Glojure does
# not need a runtime isolate, so its compatibility implementation accepts a
# null thread handle.
yamlstar_load_fn = libyamlstar.yamlstar_load
yamlstar_load_fn.argtypes = \
  [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
yamlstar_load_fn.restype = ctypes.c_char_p

yamlstar_load_all_fn = libyamlstar.yamlstar_load_all
yamlstar_load_all_fn.argtypes = \
  [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
yamlstar_load_all_fn.restype = ctypes.c_char_p

yamlstar_dump_fn = libyamlstar.yamlstar_dump
yamlstar_dump_fn.argtypes = \
  [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
yamlstar_dump_fn.restype = ctypes.c_char_p

yamlstar_dump_all_fn = libyamlstar.yamlstar_dump_all
yamlstar_dump_all_fn.argtypes = \
  [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
yamlstar_dump_all_fn.restype = ctypes.c_char_p

yamlstar_version_fn = libyamlstar.yamlstar_version
yamlstar_version_fn.argtypes = [ctypes.c_void_p]
yamlstar_version_fn.restype = ctypes.c_char_p


def _opts_bytes(options=None, parser=None):
  """Build the options JSON bytes for an FFI call.

  The parser argument is shorthand that expands to
  {"plugin": {"parser": {"use": parser}}} without mutating the
  caller's options dict.
  """
  opts = dict(options) if options else {}
  if parser is not None:
    plugin = dict(opts.get('plugin') or {})
    parser_cfg = dict(plugin.get('parser') or {})
    parser_cfg['use'] = parser
    plugin['parser'] = parser_cfg
    opts['plugin'] = plugin
  return ctypes.c_char_p(bytes(json.dumps(opts), "utf8"))


# The YAMLStar class is the main user facing API for this module.
class YAMLStar():
  """
  Interface with the libyamlstar shared library.

  Usage:
    import yamlstar
    ys = yamlstar.YAMLStar()
    data = ys.load("key: value")
    # Returns: {'key': 'value'}

    docs = ys.load_all("---\\ndoc1\\n---\\ndoc2")
    # Returns: ['doc1', 'doc2']

    # Select a parser plugin:
    data = ys.load("key: value", parser='snakeyaml')
    data = ys.load("key: value",
                   options={'plugin': {'parser': {'use': 'snakeyaml'}}})
  """

  def __init__(self):
    # GraalVM fills this handle; the Glojure compatibility implementation
    # leaves it null because its Go runtime is process-wide.
    self._isolatethread = ctypes.c_void_p()
    rc = libyamlstar.graal_create_isolate(
      None,
      None,
      ctypes.byref(self._isolatethread),
    )
    if rc != 0:
      raise Exception("Failed to initialize libyamlstar")

  def _call(self, function, input_bytes, opts_bytes):
    """Call a library function and unwrap the JSON response envelope."""
    self.error = None
    data_json = function(self._isolatethread, input_bytes, opts_bytes).decode()

    resp = json.loads(data_json)
    self.error = resp.get('error')
    if self.error:
      raise Exception(self.error['cause'])
    if 'data' not in resp:
      raise Exception("Unexpected response from 'libyamlstar'")
    return resp.get('data')

  # Load a single YAML document and return the result:
  def load(self, yaml_input, options=None, parser=None):
    """
    Load a single YAML document.

    Args:
      yaml_input: String containing YAML content
      options: Optional dict of load options, e.g.
        {'plugin': {'parser': {'use': 'snakeyaml'}}}
      parser: Optional parser plugin name (shorthand for the above)

    Returns:
      Python object representing the YAML document

    Raises:
      Exception if the YAML is malformed
    """
    return self._call(
      yamlstar_load_fn,
      ctypes.c_char_p(bytes(yaml_input, "utf8")),
      _opts_bytes(options, parser))

  # Load all YAML documents and return the results:
  def load_all(self, yaml_input, options=None, parser=None):
    """
    Load all YAML documents from a multi-document string.

    Args:
      yaml_input: String containing one or more YAML documents
      options: Optional dict of load options
      parser: Optional parser plugin name

    Returns:
      List of Python objects, one per YAML document

    Raises:
      Exception if the YAML is malformed
    """
    return self._call(
      yamlstar_load_all_fn,
      ctypes.c_char_p(bytes(yaml_input, "utf8")),
      _opts_bytes(options, parser))

  def dump(self, value, options=None):
    """
    Dump a Python JSON-compatible value to YAML.

    The options argument is reserved for future dump options.
    """
    return self._call(
      yamlstar_dump_fn,
      ctypes.c_char_p(bytes(json.dumps(value), "utf8")),
      _opts_bytes(options))

  def dump_all(self, values, options=None):
    """
    Dump Python JSON-compatible values to a multi-document YAML stream.

    The options argument is reserved for future dump options.
    """
    return self._call(
      yamlstar_dump_all_fn,
      ctypes.c_char_p(bytes(json.dumps(values), "utf8")),
      _opts_bytes(options))

  # Get the YAMLStar version:
  def version(self):
    """
    Get the YAMLStar version string.

    Returns:
      Version string
    """
    return yamlstar_version_fn(self._isolatethread).decode()

  def __del__(self):
    if hasattr(self, '_isolatethread'):
      rc = libyamlstar.graal_tear_down_isolate(self._isolatethread)
      if rc != 0:
        raise Exception("Failed to tear down libyamlstar")
