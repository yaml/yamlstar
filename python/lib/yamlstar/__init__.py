# Copyright 2024 yaml.org
# MIT License

"""
Python binding/API for the libyamlstar shared library.

This module provides a Python interface to YAMLStar, a pure YAML 1.2 loader.
The YAMLStar class has methods for loading YAML documents and converting
them to Python objects.
"""

# Version matching the yamlstar shared library
yamlstar_version = '0.1.0-SNAPSHOT'

import os
import sys
import ctypes
import json

# Require Python 3.6 or greater:
assert sys.version_info >= (3, 6), \
  "Python 3.6 or greater required for 'yamlstar'."

# Find the libyamlstar shared library file path:
def find_libyamlstar_path():
  # Confirm platform and determine file extension:
  if sys.platform == 'linux':
    so = 'so'
  elif sys.platform == 'darwin':
    so = 'dylib'
  else:
    raise Exception(
      "Unsupported platform '%s' for yamlstar." % sys.platform)

  # We currently bind to an exact version of libyamlstar.
  # eg 'libyamlstar.so.0.1.0-SNAPSHOT'
  libyamlstar_name = \
    "libyamlstar.%s.%s" % (so, yamlstar_version)

  # Use LD_LIBRARY_PATH to find libyamlstar shared library, or default to
  # '/usr/local/lib' (where it is installed by default):
  ld_library_path = os.environ.get('LD_LIBRARY_PATH')
  ld_library_paths = ld_library_path.split(':') if ld_library_path else []
  ld_library_paths.append('/usr/local/lib')
  ld_library_paths.append(os.environ.get('HOME') + '/.local/lib')

  # Also check relative to this file (for development)
  lib_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), '..', 'libyamlstar', 'lib')
  lib_path = os.path.abspath(lib_path)
  ld_library_paths.insert(0, lib_path)

  libyamlstar_path = None
  for path in ld_library_paths:
    full_path = path + '/' + libyamlstar_name
    if os.path.isfile(full_path):
      libyamlstar_path = full_path
      break

  if not libyamlstar_path:
    raise Exception(
      """\
Shared library file '%s' not found
Search paths: %s
Build with: cd libyamlstar && make native
""" % (libyamlstar_name, ':'.join(ld_library_paths)))

  return libyamlstar_path

# Load libyamlstar shared library:
libyamlstar = ctypes.CDLL(find_libyamlstar_path())

# Create bindings to library functions:
yamlstar_load = libyamlstar.yamlstar_load
yamlstar_load.restype = ctypes.c_char_p

yamlstar_load_all = libyamlstar.yamlstar_load_all
yamlstar_load_all.restype = ctypes.c_char_p

yamlstar_version_fn = libyamlstar.yamlstar_version
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

  # YAMLStar instance constructor:
  def __init__(self):
    # Create a new GraalVM isolatethread for life of the YAMLStar instance:
    self.isolatethread = ctypes.c_void_p()

    # Create a new GraalVM isolate:
    rc = libyamlstar.graal_create_isolate(
      None,
      None,
      ctypes.byref(self.isolatethread),
    )

    if rc != 0:
      raise Exception("Failed to create isolate")

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
    # Reset any previous error:
    self.error = None

    # Call 'yamlstar_load' function in libyamlstar shared library:
    data_json = yamlstar_load(
      self.isolatethread,
      ctypes.c_char_p(bytes(yaml_input, "utf8")),
    ).decode()

    # Decode the JSON response:
    resp = json.loads(data_json)

    # Check for libyamlstar error in JSON response:
    self.error = resp.get('error')
    if self.error:
      raise Exception(self.error['cause'])

    # Get the response object from loading the YAML string:
    if 'data' not in resp:
      raise Exception("Unexpected response from 'libyamlstar'")
    data = resp.get('data')

    # Return the response object:
    return data

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
    # Reset any previous error:
    self.error = None

    # Call 'yamlstar_load_all' function in libyamlstar shared library:
    data_json = yamlstar_load_all(
      self.isolatethread,
      ctypes.c_char_p(bytes(yaml_input, "utf8")),
    ).decode()

    # Decode the JSON response:
    resp = json.loads(data_json)

    # Check for libyamlstar error in JSON response:
    self.error = resp.get('error')
    if self.error:
      raise Exception(self.error['cause'])

    # Get the response object from loading the YAML string:
    if 'data' not in resp:
      raise Exception("Unexpected response from 'libyamlstar'")
    data = resp.get('data')

    # Return the response object:
    return data

  # Get the YAMLStar version:
  def version(self):
    """
    Get the YAMLStar version string.

    Returns:
      Version string
    """
    return yamlstar_version_fn(self.isolatethread).decode()

  # YAMLStar instance destructor:
  def __del__(self):
    # Tear down the isolate thread to free resources:
    if hasattr(self, 'isolatethread'):
      rc = libyamlstar.graal_tear_down_isolate(self.isolatethread)
      if rc != 0:
        raise Exception("Failed to tear down isolate")
