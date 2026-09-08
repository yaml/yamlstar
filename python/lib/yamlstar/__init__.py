# Copyright 2024 yaml.org
# MIT License

"""
Python binding/API for the libyamlstar shared library.
"""

import ctypes
import json
import os
import sys
import threading

try:
  from importlib import metadata as importlib_metadata
except ImportError:
  import importlib_metadata

yamlstar_version = '0.1.21'

_plugin_environment_lock = threading.Lock()

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


def _plugin_entry_points():
  entry_points = importlib_metadata.entry_points()
  if hasattr(entry_points, 'select'):
    return list(entry_points.select(group='yamlstar.plugins'))
  if hasattr(entry_points, 'get'):
    return list(entry_points.get('yamlstar.plugins', ()))
  return [
    entry_point for entry_point in entry_points
    if entry_point.group == 'yamlstar.plugins'
  ]


def _requested_plugin_names(options):
  plugins = options.get('plugin') if isinstance(options, dict) else None
  if not isinstance(plugins, dict):
    return []
  names = []
  for api, config in plugins.items():
    if not isinstance(config, dict):
      continue
    name = config.get('name', api)
    if isinstance(name, str) and name not in names:
      names.append(name)
  return names


def _installed_plugin_dirs(options):
  directories = []
  names = _requested_plugin_names(options)
  entry_points = _plugin_entry_points() if names else []
  for name in names:
    matches = [
      entry_point for entry_point in entry_points
      if entry_point.name == name
    ]
    if len(matches) > 1:
      raise Exception(
        "Multiple installed YAMLStar plugins are named '%s'" % name)
    if not matches:
      continue
    entry_point = matches[0]
    try:
      directory = os.path.abspath(os.fspath(entry_point.load()()))
    except Exception as error:
      raise Exception(
        "Failed to load installed YAMLStar plugin '%s': %s" %
        (name, error))
    if not os.path.isdir(directory):
      raise Exception(
        "Installed YAMLStar plugin '%s' returned a missing directory: %s" %
        (name, directory))
    if directory not in directories:
      directories.append(directory)
  return directories


def _binding_plugin_dirs(options):
  if 'YAMLSTAR_LIBRARY_PATH' in os.environ:
    return []
  return _installed_plugin_dirs(options)


def _default_plugin_dirs(libyamlstar_path):
  directories = [os.path.dirname(os.path.abspath(libyamlstar_path))]
  executable_lib = os.path.abspath(
    os.path.join(os.path.dirname(sys.executable), '..', 'lib'))
  directories.append(executable_lib)
  home = os.environ.get('HOME') or os.path.expanduser('~')
  if home:
    directories.append(os.path.join(home, '.local', 'lib'))
  if sys.platform != 'win32':
    directories.extend(['/usr/local/lib', '/usr/lib'])
  return directories


def _plugin_search_path_from_dirs(installed, libyamlstar_path):
  if not installed:
    return None
  configured = os.environ.get('YAMLSTAR_LIBRARY_PATH')
  directories = configured.split(os.pathsep) if configured else []
  directories.extend(installed)
  directories.extend(_default_plugin_dirs(libyamlstar_path))
  unique = []
  for directory in directories:
    if not directory:
      continue
    directory = os.path.abspath(directory)
    if directory not in unique:
      unique.append(directory)
  return os.pathsep.join(unique)


def _plugin_search_path(options, libyamlstar_path):
  if 'YAMLSTAR_LIBRARY_PATH' in os.environ:
    return None
  return _plugin_search_path_from_dirs(
    _installed_plugin_dirs(options), libyamlstar_path)


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

  def plugin_install(self, enabled=True):
    """Enable or disable installation of missing shared plugins."""
    self._options['plugin-install'] = bool(enabled)
    return self

  def to_dict(self):
    return dict(self._options)


def parser(name):
  """Return a parser plugin option fragment."""
  return {'parser': {'name': name}}


def json_comments(name='json-comments'):
  """Return a JSON comments event-source plugin option fragment."""
  return {'json-comments': {'name': name}}


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

  def __init__(self, options=None, so='libyamlstar',
               install_plugins=None):
    self._options = _options_dict(options)
    if install_plugins is not None:
      self._options['plugin-install'] = bool(install_plugins)
    self._libyamlstar_path = find_libyamlstar(so)
    self._plugin_installer_path = os.path.join(
      os.path.dirname(self._libyamlstar_path), 'yamlstar-plugin')
    self._installed_plugin_dirs = _binding_plugin_dirs(self._options)

    with _plugin_environment_lock:
      previous_path = os.environ.get('YAMLSTAR_LIBRARY_PATH')
      previous_installer = os.environ.get('YAMLSTAR_PLUGIN_INSTALLER')
      plugin_path = _plugin_search_path_from_dirs(
        self._installed_plugin_dirs, self._libyamlstar_path)
      selected_installer = self._selected_plugin_installer()
      native_path = (
        plugin_path if plugin_path is not None else previous_path)
      native_installer = (
        selected_installer
        if selected_installer is not None else previous_installer)
      if plugin_path is not None:
        os.environ['YAMLSTAR_LIBRARY_PATH'] = plugin_path
      if selected_installer is not None:
        os.environ['YAMLSTAR_PLUGIN_INSTALLER'] = selected_installer
      try:
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
        self._set_native_plugin_environment(native_path, native_installer)
      finally:
        if hasattr(self, '_plugin_environment'):
          self._set_native_plugin_environment(
            previous_path, previous_installer)
        self._restore_environment(
          'YAMLSTAR_LIBRARY_PATH', previous_path)
        self._restore_environment(
          'YAMLSTAR_PLUGIN_INSTALLER', previous_installer)

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

    try:
      self._plugin_environment = (
        self._libyamlstar.yamlstar_set_plugin_environment)
    except AttributeError:
      self._plugin_environment = None
    if self._plugin_environment is not None:
      self._plugin_environment.argtypes = [
        ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
      self._plugin_environment.restype = ctypes.c_int

  @staticmethod
  def _restore_environment(name, value):
    if value is None:
      os.environ.pop(name, None)
    else:
      os.environ[name] = value

  def _selected_plugin_installer(self):
    install = self._options.get(
      'plugin-install', self._options.get('plugin_install'))
    installer = self._plugin_installer_path
    if (install is True and os.path.isfile(installer) and
        os.access(installer, os.X_OK) and
        not os.environ.get('YAMLSTAR_PLUGIN_INSTALLER')):
      return installer
    return None

  def _set_native_plugin_environment(self, plugin_path, installer):
    function = getattr(self, '_plugin_environment', None)
    if function is None:
      return
    path_bytes = _bytes(plugin_path) if plugin_path is not None else None
    installer_bytes = _bytes(installer) if installer is not None else None
    rc = function(self._isolatethread, path_bytes, installer_bytes)
    if rc != 0:
      raise Exception("Failed to configure YAMLStar plugin environment")

  def _opts_bytes(self):
    return _bytes(json.dumps(self._options))

  def _call(self, function, input_text):
    self.error = None
    def call_native():
      return function(
        self._isolatethread,
        _bytes(input_text),
        self._opts_bytes()).decode()

    installed_plugin_dirs = getattr(
      self, '_installed_plugin_dirs', None)
    if installed_plugin_dirs is None:
      installed_plugin_dirs = _binding_plugin_dirs(self._options)
    selected_installer = self._selected_plugin_installer()
    if installed_plugin_dirs or selected_installer is not None:
      with _plugin_environment_lock:
        previous_path = os.environ.get('YAMLSTAR_LIBRARY_PATH')
        previous_installer = os.environ.get('YAMLSTAR_PLUGIN_INSTALLER')
        plugin_path = _plugin_search_path_from_dirs(
          installed_plugin_dirs, self._libyamlstar_path)
        native_path = (
          plugin_path if plugin_path is not None else previous_path)
        native_installer = (
          selected_installer
          if selected_installer is not None else previous_installer)
        if plugin_path is not None:
          os.environ['YAMLSTAR_LIBRARY_PATH'] = plugin_path
        if selected_installer is not None:
          os.environ['YAMLSTAR_PLUGIN_INSTALLER'] = selected_installer
        try:
          self._set_native_plugin_environment(
            native_path, native_installer)
          data_json = call_native()
        finally:
          self._set_native_plugin_environment(
            previous_path, previous_installer)
          if plugin_path is not None:
            self._restore_environment(
              'YAMLSTAR_LIBRARY_PATH', previous_path)
          if selected_installer is not None:
            self._restore_environment(
              'YAMLSTAR_PLUGIN_INSTALLER', previous_installer)
    else:
      data_json = call_native()

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
