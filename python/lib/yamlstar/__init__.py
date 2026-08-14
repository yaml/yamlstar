# Copyright 2024 yaml.org
# MIT License

"""
Python binding/API for the libyamlstar shared library.
"""

import ctypes
import json
import os
import sys

yamlstar_version = '0.1.18'

assert sys.version_info >= (3, 6), \
  "Python 3.6 or greater required for 'yamlstar'."


def _lib_extension():
  if sys.platform == 'linux' or sys.platform.startswith('freebsd'):
    return 'so'
  if sys.platform == 'darwin':
    return 'dylib'
  if sys.platform == 'win32':
    return 'dll'
  raise Exception("Unsupported platform '%s' for yamlstar." % sys.platform)


def _library_paths():
  if sys.platform == 'win32':
    library_path = os.environ.get('PATH')
    paths = library_path.split(';') if library_path else []
  else:
    paths = []
    for env in ('LD_LIBRARY_PATH', 'DYLD_LIBRARY_PATH'):
      library_path = os.environ.get(env)
      if library_path:
        paths.extend(library_path.split(':'))

  dev_path = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
    '..', 'libyamlstar', 'lib')
  paths.insert(0, os.path.abspath(dev_path))
  paths.append(os.path.join(os.path.dirname(__file__), 'libyamlstar'))
  if sys.platform != 'win32':
    paths.append('/usr/local/lib')
  home = os.environ.get('HOME') or os.path.expanduser('~')
  if home:
    paths.append(os.path.join(home, '.local', 'lib'))
  return paths


def _candidate_filenames(so):
  ext = _lib_extension()
  base = so or 'libyamlstar'
  if os.path.sep in base or (os.path.altsep and os.path.altsep in base):
    return [base]
  if base.endswith('.' + ext):
    return [base]
  return ['%s.%s' % (base, ext)]


def find_libyamlstar(so='libyamlstar'):
  """Find a YAMLStar shared library by basename or path."""
  candidates = _candidate_filenames(so)
  for filename in candidates:
    if os.path.isabs(filename) and os.path.isfile(filename):
      return filename
  for path in _library_paths():
    for filename in candidates:
      full_path = os.path.join(path, filename)
      if os.path.isfile(full_path):
        return full_path
  raise Exception(
    "Shared library file '%s' not found\nSearch paths: %s" %
    (candidates[0], os.pathsep.join(_library_paths())))


class Options:
  """YAMLStar options builder."""

  def __init__(self, options=None):
    self._options = dict(options or {})

  def add(self, options):
    self._options.update(options or {})
    return self

  def plugin(self, plugin_options):
    plugin = dict(self._options.get('plugin') or {})
    plugin.update(plugin_options or {})
    self._options['plugin'] = plugin
    return self

  def to_dict(self):
    return dict(self._options)


def parser(name):
  """Return a parser plugin option fragment."""
  return {'parser': {'name': name}}


def _options_dict(options):
  if options is None:
    return {}
  if isinstance(options, Options):
    return options.to_dict()
  return dict(options)


def _bytes(text):
  return ctypes.c_char_p(bytes(text, 'utf8'))


class YAMLStar():
  """Interface with a YAMLStar shared library."""

  def __init__(self, options=None, so='libyamlstar'):
    self._options = _options_dict(options)
    self._libyamlstar_path = find_libyamlstar(so)
    self._libyamlstar = ctypes.CDLL(self._libyamlstar_path)
    self._configure_functions()

    self._isolatethread = ctypes.c_void_p()
    rc = self._libyamlstar.graal_create_isolate(
      None,
      None,
      ctypes.byref(self._isolatethread),
    )
    if rc != 0:
      raise Exception("Failed to initialize libyamlstar")

  def _configure_functions(self):
    self._load = self._libyamlstar.yamlstar_load
    self._load.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
    self._load.restype = ctypes.c_char_p

    self._load_all = self._libyamlstar.yamlstar_load_all
    self._load_all.argtypes = [
      ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
    self._load_all.restype = ctypes.c_char_p

    self._dump = self._libyamlstar.yamlstar_dump
    self._dump.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
    self._dump.restype = ctypes.c_char_p

    self._dump_all = self._libyamlstar.yamlstar_dump_all
    self._dump_all.argtypes = [
      ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
    self._dump_all.restype = ctypes.c_char_p

    self._version = self._libyamlstar.yamlstar_version
    self._version.argtypes = [ctypes.c_void_p]
    self._version.restype = ctypes.c_char_p

  def _opts_bytes(self):
    return _bytes(json.dumps(self._options))

  def _call(self, function, input_text):
    self.error = None
    data_json = function(
      self._isolatethread,
      _bytes(input_text),
      self._opts_bytes()).decode()

    resp = json.loads(data_json)
    self.error = resp.get('error')
    if self.error:
      raise Exception(self.error['cause'])
    if 'data' not in resp:
      raise Exception("Unexpected response from 'libyamlstar'")
    return resp.get('data')

  def load(self, yaml_input):
    """Load a single YAML document."""
    return self._call(self._load, yaml_input)

  def load_all(self, yaml_input):
    """Load all YAML documents from a multi-document string."""
    return self._call(self._load_all, yaml_input)

  def dump(self, value):
    """Dump a Python JSON-compatible value to YAML."""
    return self._call(self._dump, json.dumps(value))

  def dump_all(self, values):
    """Dump Python JSON-compatible values to a multi-document YAML stream."""
    return self._call(self._dump_all, json.dumps(values))

  def version(self):
    """Get the YAMLStar version string."""
    return self._version(self._isolatethread).decode()

  def __del__(self):
    if hasattr(self, '_libyamlstar') and hasattr(self, '_isolatethread'):
      self._libyamlstar.graal_tear_down_isolate(self._isolatethread)
