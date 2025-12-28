"""
Tests for yamlstar Python package.
"""
import pytest
import sys
import os

# Add lib directory to path for testing
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'lib'))

import yamlstar


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
