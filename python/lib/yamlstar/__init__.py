# Copyright 2024 yaml.org
# MIT License

"""
Python binding/API for the libyamlstar shared library.

This module provides a Python interface to YAMLStar, a pure YAML 1.2 loader.
The YAMLStar class has methods for loading YAML documents and converting
them to Python objects.
"""

# Version matching the yamlstar shared library
yamlstar_version = '0.1.3'

import os
import sys
import ctypes
import json

# Require Python 3.6 or greater:
assert sys.version_info >= (3, 6), \
  "Python 3.6 or greater required for 'yamlstar'."

def find_libyamlstar():
  """Find libyamlstar shared library. Returns (path, backend)."""
  if sys.platform == 'linux':
    so = 'so'
  elif sys.platform == 'darwin':
    so = 'dylib'
  elif sys.platform == 'win32':
    so = 'dll'
  else:
    raise Exception(
      "Unsupported platform '%s' for yamlstar." % sys.platform)

  # Use LD_LIBRARY_PATH to find libyamlstar shared library, or default to
  # '/usr/local/lib' (where it is installed by default):
  ld_library_path = os.environ.get('LD_LIBRARY_PATH')
  ld_library_paths = ld_library_path.split(':') if ld_library_path else []
  ld_library_paths.append('/usr/local/lib')
  ld_library_paths.append(os.environ.get('HOME') + '/.local/lib')

  # Also check relative to this file (for development)
  lib_path = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
    '..', 'libyamlstar', 'lib')
  ld_library_paths.insert(0, os.path.abspath(lib_path))

  if os.environ.get('YAMLSTAR_GLOJURE'):
    lib_name, backend = 'libyamlstarglj', 'gloat'
  else:
    lib_name, backend = 'libyamlstar', 'graalvm'

  filename = "%s.%s" % (lib_name, so)
  for path in ld_library_paths:
    full_path = os.path.join(path, filename)
    if os.path.isfile(full_path):
      return full_path, backend

  raise Exception(
    """\
Shared library file '%s' not found
Search paths: %s
Build with: cd libyamlstar && make build
""" % (filename, ':'.join(ld_library_paths)))

# Load libyamlstar shared library and detect backend:
_libyamlstar_path, _backend = find_libyamlstar()
libyamlstar = ctypes.CDLL(_libyamlstar_path)

# Create bindings to library functions (signatures differ by backend):
if _backend == 'gloat':
  # Gloat: functions take (yaml_bytes, opts_bytes) -> json_bytes
  yamlstar_load_fn = libyamlstar.yamlstar_load
  yamlstar_load_fn.argtypes = [ctypes.c_char_p, ctypes.c_char_p]
  yamlstar_load_fn.restype = ctypes.c_char_p

  yamlstar_load_all_fn = libyamlstar.yamlstar_load_all
  yamlstar_load_all_fn.argtypes = [ctypes.c_char_p, ctypes.c_char_p]
  yamlstar_load_all_fn.restype = ctypes.c_char_p

  yamlstar_version_fn = libyamlstar.yamlstar_version
  yamlstar_version_fn.argtypes = []
  yamlstar_version_fn.restype = ctypes.c_char_p

else:
  # GraalVM: functions take (isolatethread, yaml_bytes) -> json_bytes
  yamlstar_load_fn = libyamlstar.yamlstar_load
  yamlstar_load_fn.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
  yamlstar_load_fn.restype = ctypes.c_char_p

  yamlstar_load_all_fn = libyamlstar.yamlstar_load_all
  yamlstar_load_all_fn.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
  yamlstar_load_all_fn.restype = ctypes.c_char_p

  yamlstar_version_fn = libyamlstar.yamlstar_version
  yamlstar_version_fn.argtypes = [ctypes.c_void_p]
  yamlstar_version_fn.restype = ctypes.c_char_p


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
  """

  def __init__(self):
    if _backend == 'graalvm':
      # Create a new GraalVM isolate thread for the life of this instance:
      self._isolatethread = ctypes.c_void_p()
      rc = libyamlstar.graal_create_isolate(
        None,
        None,
        ctypes.byref(self._isolatethread),
      )
      if rc != 0:
        raise Exception("Failed to create GraalVM isolate")

  # Load a single YAML document and return the result:
  def load(self, yaml_input):
    """
    Load a single YAML document.

    Args:
      yaml_input: String containing YAML content

    Returns:
      Python object representing the YAML document

    Raises:
      Exception if the YAML is malformed
    """
    self.error = None
    yaml_bytes = ctypes.c_char_p(bytes(yaml_input, "utf8"))

    if _backend == 'gloat':
      data_json = yamlstar_load_fn(yaml_bytes, ctypes.c_char_p(b"{}")).decode()
    else:
      data_json = yamlstar_load_fn(self._isolatethread, yaml_bytes).decode()

    resp = json.loads(data_json)
    self.error = resp.get('error')
    if self.error:
      raise Exception(self.error['cause'])
    if 'data' not in resp:
      raise Exception("Unexpected response from 'libyamlstar'")
    return resp.get('data')

  # Load all YAML documents and return the results:
  def load_all(self, yaml_input):
    """
    Load all YAML documents from a multi-document string.

    Args:
      yaml_input: String containing one or more YAML documents

    Returns:
      List of Python objects, one per YAML document

    Raises:
      Exception if the YAML is malformed
    """
    self.error = None
    yaml_bytes = ctypes.c_char_p(bytes(yaml_input, "utf8"))

    if _backend == 'gloat':
      data_json = \
        yamlstar_load_all_fn(yaml_bytes, ctypes.c_char_p(b"{}")).decode()
    else:
      data_json = \
        yamlstar_load_all_fn(self._isolatethread, yaml_bytes).decode()

    resp = json.loads(data_json)
    self.error = resp.get('error')
    if self.error:
      raise Exception(self.error['cause'])
    if 'data' not in resp:
      raise Exception("Unexpected response from 'libyamlstar'")
    return resp.get('data')

  # Get the YAMLStar version:
  def version(self):
    """
    Get the YAMLStar version string.

    Returns:
      Version string
    """
    if _backend == 'gloat':
      return yamlstar_version_fn().decode()
    else:
      return yamlstar_version_fn(self._isolatethread).decode()

  def __del__(self):
    if _backend == 'graalvm' and hasattr(self, '_isolatethread'):
      rc = libyamlstar.graal_tear_down_isolate(self._isolatethread)
      if rc != 0:
        raise Exception("Failed to tear down GraalVM isolate")
