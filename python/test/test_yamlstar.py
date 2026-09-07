"""
Tests for yamlstar Python package.
"""
import pytest
import sys
import os
import threading
import time

# Add lib directory to path for testing
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'lib'))

import yamlstar


class FakeEntryPoint:
    """Minimal installed plugin entry point for discovery tests."""

    def __init__(self, name, directory):
        self.name = name
        self.group = 'yamlstar.plugins'
        self.directory = directory
        self.loaded = False

    def load(self):
        self.loaded = True
        return lambda: self.directory


@pytest.fixture
def ys():
    """Create a YAMLStar instance for testing."""
    return yamlstar.YAMLStar()


def test_load_simple_scalar(ys):
    """Test loading a simple scalar value."""
    result = ys.load("hello")
    assert result == "hello"


def test_load_integer(ys):
    """Test loading an integer."""
    result = ys.load("42")
    assert result == 42
    assert isinstance(result, int)


def test_load_float(ys):
    """Test loading a float."""
    result = ys.load("3.14")
    assert result == 3.14
    assert isinstance(result, float)


def test_load_boolean_true(ys):
    """Test loading boolean true."""
    result = ys.load("true")
    assert result is True


def test_load_boolean_false(ys):
    """Test loading boolean false."""
    result = ys.load("false")
    assert result is False


def test_load_null(ys):
    """Test loading null value."""
    result = ys.load("null")
    assert result is None


def test_load_simple_mapping(ys):
    """Test loading a simple mapping."""
    result = ys.load("key: value")
    assert result == {"key": "value"}


def test_load_nested_mapping(ys):
    """Test loading nested mappings."""
    yaml_str = """
outer:
  inner: value
"""
    result = ys.load(yaml_str)
    assert result == {"outer": {"inner": "value"}}


def test_load_mapping_multiple_keys(ys):
    """Test loading a mapping with multiple keys."""
    yaml_str = """
key1: value1
key2: value2
key3: value3
"""
    result = ys.load(yaml_str)
    assert result == {
        "key1": "value1",
        "key2": "value2",
        "key3": "value3"
    }


def test_load_simple_sequence(ys):
    """Test loading a simple sequence."""
    yaml_str = """
- item1
- item2
- item3
"""
    result = ys.load(yaml_str)
    assert result == ["item1", "item2", "item3"]


def test_load_flow_sequence(ys):
    """Test loading flow-style sequence."""
    result = ys.load("[a, b, c]")
    assert result == ["a", "b", "c"]


def test_load_type_coercion(ys):
    """Test YAML 1.2 type coercion."""
    yaml_str = """
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
"""
    result = ys.load(yaml_str)
    assert result == {
        "string": "hello",
        "integer": 42,
        "float": 3.14,
        "bool_true": True,
        "bool_false": False,
        "null_value": None
    }


def test_load_sequence_of_mappings(ys):
    """Test loading a sequence of mappings."""
    yaml_str = """
- name: Alice
  age: 30
- name: Bob
  age: 25
"""
    result = ys.load(yaml_str)
    assert result == [
        {"name": "Alice", "age": 30},
        {"name": "Bob", "age": 25}
    ]


def test_load_mapping_with_sequence_values(ys):
    """Test loading a mapping with sequence values."""
    yaml_str = """
fruits:
  - apple
  - banana
colors:
  - red
  - blue
"""
    result = ys.load(yaml_str)
    assert result == {
        "fruits": ["apple", "banana"],
        "colors": ["red", "blue"]
    }


def test_load_all_single_document(ys):
    """Test load_all with a single document."""
    result = ys.load_all("hello")
    assert result == ["hello"]


def test_load_all_multiple_documents(ys):
    """Test load_all with multiple documents."""
    yaml_str = """---
doc1
---
doc2
---
doc3"""
    result = ys.load_all(yaml_str)
    assert result == ["doc1", "doc2", "doc3"]


def test_load_all_with_explicit_markers(ys):
    """Test load_all with explicit document markers."""
    yaml_str = """---
a: 1
...
---
b: 2
..."""
    result = ys.load_all(yaml_str)
    assert result == [{"a": 1}, {"b": 2}]


def test_dump_simple_mapping(ys):
    """Test dumping a simple mapping."""
    assert ys.dump({"key": "value"}) == "key: value\n"


def test_dump_roundtrip(ys):
    """Test dump output can be loaded back."""
    value = {"items": ["a", "b"], "flag": True, "text": "42"}
    assert ys.load(ys.dump(value)) == value


def test_dump_all(ys):
    """Test dumping multiple documents."""
    assert ys.dump_all(["doc1", {"a": 1}]) == "---\ndoc1\n---\na: 1\n"


def test_version(ys):
    """Test getting the version string."""
    version = ys.version()
    assert isinstance(version, str)
    assert len(version) > 0


def test_error_handling_malformed_yaml(ys):
    """Test that malformed YAML raises an exception."""
    # Unclosed quote is truly malformed
    malformed_yaml = 'key: "unclosed'
    with pytest.raises(Exception):
        ys.load(malformed_yaml)


def test_empty_document(ys):
    """Test loading an empty document."""
    result = ys.load("")
    assert result is None


def test_whitespace_only(ys):
    """Test loading whitespace-only document."""
    result = ys.load("   \n  \n  ")
    assert result is None


def test_quoted_strings(ys):
    """Test loading quoted strings."""
    result1 = ys.load("'hello world'")
    assert result1 == "hello world"

    result2 = ys.load('"hello world"')
    assert result2 == "hello world"


def test_module_version():
    """Test that the module has a version attribute."""
    assert hasattr(yamlstar, 'yamlstar_version')
    assert isinstance(yamlstar.yamlstar_version, str)


def test_load_with_snakeyaml_parser(ys):
    """Test loading with the snakeyaml parser plugin."""
    yaml_str = "a: [1, {b: two}]\nc: |\n  text\n"
    opts = yamlstar.Options().plugin(yamlstar.parser('snakeyaml'))
    try:
        snakeyaml = yamlstar.YAMLStar(opts, so='libyamlstar-graalvm')
    except Exception as error:
        if 'not found' not in str(error):
            raise
        pytest.skip('SnakeYAML parser library is not available')
    assert snakeyaml.load(yaml_str) == ys.load(yaml_str)
    assert snakeyaml.load_all("---\ndoc1\n---\ndoc2") == ["doc1", "doc2"]


def test_requested_plugin_names():
    """Extract plugin distribution names from native plugin options."""
    options = {
        'plugin': {
            'parser': {'name': 'reference'},
            'json-comments': {},
            'ignored': 'not-a-plugin',
        },
    }
    assert yamlstar._requested_plugin_names(options) == [
        'reference',
        'json-comments',
    ]


def test_installed_plugin_discovery_loads_only_requested(
        monkeypatch, tmp_path):
    """Do not import unrelated YAMLStar plugin distributions."""
    requested_dir = tmp_path / 'requested'
    requested_dir.mkdir()
    requested = FakeEntryPoint('json-comments', str(requested_dir))
    unrelated = FakeEntryPoint('other-plugin', str(tmp_path / 'missing'))
    monkeypatch.setattr(
        yamlstar.importlib_metadata,
        'entry_points',
        lambda: [requested, unrelated],
    )

    options = {
        'plugin': {'json-comments': {'name': 'json-comments'}},
    }
    assert yamlstar._installed_plugin_dirs(options) == [
        str(requested_dir),
    ]
    assert requested.loaded is True
    assert unrelated.loaded is False


def test_installed_plugin_discovery_rejects_duplicate_names(
        monkeypatch, tmp_path):
    """Duplicate plugin distributions must not depend on entry point order."""
    directory = tmp_path / 'plugin'
    directory.mkdir()
    monkeypatch.setattr(
        yamlstar.importlib_metadata,
        'entry_points',
        lambda: [
            FakeEntryPoint('json-comments', str(directory)),
            FakeEntryPoint('json-comments', str(directory)),
        ],
    )

    options = {
        'plugin': {'json-comments': {'name': 'json-comments'}},
    }
    with pytest.raises(Exception, match='Multiple installed YAMLStar plugins'):
        yamlstar._installed_plugin_dirs(options)


def test_installed_plugin_discovery_rejects_missing_directory(
        monkeypatch, tmp_path):
    """A plugin entry point must return an existing directory."""
    missing = tmp_path / 'missing'
    monkeypatch.setattr(
        yamlstar.importlib_metadata,
        'entry_points',
        lambda: [FakeEntryPoint('json-comments', str(missing))],
    )

    options = {
        'plugin': {'json-comments': {'name': 'json-comments'}},
    }
    with pytest.raises(Exception, match='returned a missing directory'):
        yamlstar._installed_plugin_dirs(options)


def test_explicit_plugin_search_path_disables_discovery(
        monkeypatch, tmp_path):
    """An explicit library path remains the complete native search path."""
    configured = tmp_path / 'configured'
    installed = tmp_path / 'installed'
    configured.mkdir()
    installed.mkdir()
    entry_point = FakeEntryPoint('json-comments', str(installed))
    monkeypatch.setattr(
        yamlstar.importlib_metadata,
        'entry_points',
        lambda: [entry_point],
    )
    monkeypatch.setenv(
        'YAMLSTAR_LIBRARY_PATH',
        os.pathsep.join([str(configured), str(installed)]),
    )

    options = {
        'plugin': {'json-comments': {'name': 'json-comments'}},
    }
    library = tmp_path / 'native' / 'libyamlstar.so'
    assert yamlstar._binding_plugin_dirs(options) == []
    assert yamlstar._plugin_search_path(options, str(library)) is None
    assert entry_point.loaded is False


def test_call_uses_and_restores_installed_plugin_path(
        monkeypatch, tmp_path):
    """A native call sees the installed plugin without leaking its path."""
    installed = tmp_path / 'installed'
    installed.mkdir()
    monkeypatch.setattr(
        yamlstar.importlib_metadata,
        'entry_points',
        lambda: [FakeEntryPoint('json-comments', str(installed))],
    )
    monkeypatch.delenv('YAMLSTAR_LIBRARY_PATH', raising=False)

    instance = object.__new__(yamlstar.YAMLStar)
    instance._options = {
        'plugin': {'json-comments': {'name': 'json-comments'}},
    }
    instance._libyamlstar_path = str(tmp_path / 'libyamlstar.so')
    instance._plugin_installer_path = str(tmp_path / 'missing-installer')
    instance._isolatethread = None
    seen = []
    native_environment = []

    def set_native_environment(thread, library_path, installer):
        native_environment.append((
            library_path.value.decode() if library_path else None,
            installer.value.decode() if installer else None,
        ))
        return 0

    instance._plugin_environment = set_native_environment

    def native_call(*args):
        seen.append(os.environ['YAMLSTAR_LIBRARY_PATH'].split(os.pathsep))
        return b'{"data":"ok"}'

    assert instance._call(native_call, 'a: b') == 'ok'
    assert seen[0][0] == str(installed)
    assert native_environment[0][0].split(os.pathsep)[0] == str(installed)
    assert native_environment[1] == (None, None)
    assert 'YAMLSTAR_LIBRARY_PATH' not in os.environ


def test_parallel_calls_keep_plugin_paths_isolated(monkeypatch, tmp_path):
    """Concurrent bindings cannot observe another call's plugin directory."""
    directories = {}
    entry_points = []
    for name in ('plugin-one', 'plugin-two'):
        directory = tmp_path / name
        directory.mkdir()
        directories[name] = str(directory)
        entry_points.append(FakeEntryPoint(name, str(directory)))
    monkeypatch.setattr(
        yamlstar.importlib_metadata,
        'entry_points',
        lambda: entry_points,
    )
    monkeypatch.delenv('YAMLSTAR_LIBRARY_PATH', raising=False)

    seen = {}
    errors = []

    def run(name):
        instance = object.__new__(yamlstar.YAMLStar)
        instance._options = {'plugin': {name: {'name': name}}}
        instance._libyamlstar_path = str(tmp_path / 'libyamlstar.so')
        instance._plugin_installer_path = str(tmp_path / 'missing-installer')
        instance._installed_plugin_dirs = [directories[name]]
        instance._isolatethread = None

        def native_call(*args):
            seen[name] = os.environ['YAMLSTAR_LIBRARY_PATH']
            time.sleep(0.05)
            return b'{"data":null}'

        try:
            instance._call(native_call, '')
        except Exception as error:
            errors.append(error)

    threads = [
        threading.Thread(target=run, args=(name,))
        for name in directories
    ]
    for thread in threads:
        thread.start()
    for thread in threads:
        thread.join()

    assert errors == []
    assert set(seen) == set(directories)
    for name, directory in directories.items():
        paths = seen[name].split(os.pathsep)
        assert directory in paths
        other = directories[
            'plugin-two' if name == 'plugin-one' else 'plugin-one']
        assert other not in paths
    assert 'YAMLSTAR_LIBRARY_PATH' not in os.environ


def test_load_with_options_dict(ys):
    """Test loading with a full options dict."""
    options = {'plugin': {'parser': {'name': 'reference'}}}
    assert yamlstar.YAMLStar(options).load("key: value") == {"key": "value"}


def test_json_comments_options():
    """Test the JSON comments plugin option helper."""
    assert yamlstar.json_comments() == {
        'json-comments': {'name': 'json-comments'}}


def test_plugin_install_options():
    """Test the shared plugin installation option helpers."""
    opts = yamlstar.Options().plugin_install()
    assert opts.to_dict()['plugin-install'] is True
    opts.plugin_install(False)
    assert opts.to_dict()['plugin-install'] is False


def test_plugin_install_constructor_option():
    """Test that the Python convenience option reaches libyamlstar."""
    installed = yamlstar.YAMLStar(install_plugins=True)
    assert installed._options['plugin-install'] is True


def test_load_with_reference_parser(ys):
    """Test explicitly selecting the reference parser."""
    opts = yamlstar.Options().plugin(yamlstar.parser('reference'))
    assert yamlstar.YAMLStar(opts).load("key: value") == {"key": "value"}


def test_load_with_unknown_parser(ys):
    """Test that an unknown parser name raises a useful error."""
    opts = yamlstar.Options().plugin(yamlstar.parser('no-such-parser'))
    unknown = yamlstar.YAMLStar(opts)
    with pytest.raises(Exception, match="Unknown YAML parser plugin"):
        unknown.load("key: value")
