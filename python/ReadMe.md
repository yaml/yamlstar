# YAMLStar Python Bindings

Python bindings for YAMLStar - a pure YAML 1.2 loader implemented in Clojure.

## Features

- **YAML 1.2 Spec Compliance**: 100% compliant with YAML 1.2 core schema
- **Pure Implementation**: No dependencies on SnakeYAML or other external parsers
- **Fast Native Performance**: Uses GraalVM native-image shared library
- **Simple API**: Load YAML documents with a single function call
- **Multi-Document Support**: Load multiple YAML documents from a single string

## Installation

### Prerequisites

First, build and install the shared library:

```bash
cd ../libyamlstar
make native
sudo make install PREFIX=/usr/local
```

Or install to user-local directory:

```bash
cd ../libyamlstar
make native
make install PREFIX=~/.local
```

### Install Python Package

```bash
pip install .
```

For development:

```bash
pip install -e .
```

## Quick Start

```python
import yamlstar

# Create a YAMLStar instance
ys = yamlstar.YAMLStar()

# Load a simple YAML string
data = ys.load("key: value")
print(data)  # {'key': 'value'}
```

## Usage Examples

### Basic Types

```python
import yamlstar

ys = yamlstar.YAMLStar()

# Strings
ys.load("hello")  # 'hello'

# Integers
ys.load("42")  # 42

# Floats
ys.load("3.14")  # 3.14

# Booleans
ys.load("true")   # True
ys.load("false")  # False

# Null
ys.load("null")  # None
```

### Collections

```python
# Mappings (dictionaries)
data = ys.load("""
name: Alice
age: 30
city: Seattle
""")
# {'name': 'Alice', 'age': 30, 'city': 'Seattle'}

# Sequences (lists)
data = ys.load("""
- apple
- banana
- orange
""")
# ['apple', 'banana', 'orange']

# Flow style
data = ys.load("[a, b, c]")
# ['a', 'b', 'c']
```

### Nested Structures

```python
data = ys.load("""
person:
  name: Alice
  age: 30
  hobbies:
    - reading
    - coding
    - hiking
""")
# {
#   'person': {
#     'name': 'Alice',
#     'age': 30,
#     'hobbies': ['reading', 'coding', 'hiking']
#   }
# }
```

### Multi-Document YAML

```python
# Load all documents from a multi-document YAML string
docs = ys.load_all("""---
name: Document 1
---
name: Document 2
---
name: Document 3
""")
# [
#   {'name': 'Document 1'},
#   {'name': 'Document 2'},
#   {'name': 'Document 3'}
# ]
```

### Type Coercion

YAMLStar follows YAML 1.2 core schema type inference:

```python
data = ys.load("""
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
""")
# {
#   'string': 'hello',
#   'integer': 42,
#   'float': 3.14,
#   'bool_true': True,
#   'bool_false': False,
#   'null_value': None
# }
```

### Error Handling

```python
try:
    data = ys.load("invalid: yaml: syntax")
except Exception as e:
    print(f"Error loading YAML: {e}")
```

### Version Information

```python
# Get YAMLStar version
version = ys.version()
print(f"YAMLStar version: {version}")
```

## API Reference

### `YAMLStar` Class

#### `__init__()`
Create a new YAMLStar instance. Each instance maintains its own GraalVM isolate.

```python
ys = yamlstar.YAMLStar()
```

#### `load(yaml_input)`
Load a single YAML document.

**Parameters:**
- `yaml_input` (str): String containing YAML content

**Returns:**
- Python object representing the YAML document (dict, list, str, int, float, bool, or None)

**Raises:**
- `Exception` if the YAML is malformed

**Example:**
```python
data = ys.load("key: value")
```

#### `load_all(yaml_input)`
Load all YAML documents from a multi-document string.

**Parameters:**
- `yaml_input` (str): String containing one or more YAML documents

**Returns:**
- List of Python objects, one per YAML document

**Raises:**
- `Exception` if the YAML is malformed

**Example:**
```python
docs = ys.load_all("---\ndoc1\n---\ndoc2")
```

#### `version()`
Get the YAMLStar version string.

**Returns:**
- str: Version string

**Example:**
```python
version = ys.version()
```

## Development

### Running Tests

```bash
# Run all tests
make test

# Run only pytest tests
make test-pytest

# Run only FFI tests
make test-ffi
```

### Building Distribution

```bash
# Build source distribution
make dist

# Build and install in development mode
make install
```

## Requirements

- **Python**: 3.6 or higher
- **libyamlstar**: Shared library (installed separately)
- **System**: Linux or macOS

## Library Search Path

The package searches for `libyamlstar.so` (or `.dylib` on macOS) in:

1. Development path (relative to package)
2. Directories in `LD_LIBRARY_PATH` environment variable
3. `/usr/local/lib` (default install location)
4. `~/.local/lib` (user-local install location)

## Comparison to PyYAML

| Feature | YAMLStar | PyYAML |
|---------|----------|--------|
| YAML Version | 1.2 | 1.1 |
| Implementation | Pure Clojure | C + Python |
| Type Inference | YAML 1.2 core schema | YAML 1.1 + custom |
| Native Performance | Yes (GraalVM) | Yes (C extension) |
| Dependencies | libyamlstar.so | None |

## License

MIT License - See [License](License) file

## Credits

Created by Ingy döt Net, inventor of YAML.

YAMLStar is built on the YAML Reference Parser (pure Clojure implementation).

## Links

- **GitHub**: https://github.com/yaml/yamlstar
- **YAML Specification**: https://yaml.org/spec/1.2/spec.html
