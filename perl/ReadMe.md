# YAMLStar Perl Bindings

Perl bindings for YAMLStar - a pure YAML 1.2 loader implemented in Clojure.

## Features

- **YAML 1.2 Spec Compliance**: 100% compliant with YAML 1.2 core schema
- **Pure Implementation**: No dependencies on YAML::XS or other external parsers
- **Fast Native Performance**: Uses GraalVM native-image shared library
- **Simple API**: Load YAML documents with a single method call
- **Multi-Document Support**: Load multiple YAML documents from a single string
- **Modern OO**: Uses Moo for clean object-oriented interface

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

### Install Perl Dependencies

```bash
cd perl
make local
```

This will install required CPAN modules to `local/`:
- Moo
- FFI::Platypus
- FFI::CheckLib
- Cpanel::JSON::XS
- Test2::V0

## Quick Start

```perl
use YAMLStar;

# Create a YAMLStar instance
my $ys = YAMLStar->new();

# Load a simple YAML string
my $data = $ys->load("key: value");
# $data = {key => "value"}
```

## Usage Examples

### Basic Types

```perl
use YAMLStar;

my $ys = YAMLStar->new();

# Strings
$ys->load("hello");  # "hello"

# Integers
$ys->load("42");  # 42

# Floats
$ys->load("3.14");  # 3.14

# Booleans
$ys->load("true");   # JSON::PP::true
$ys->load("false");  # JSON::PP::false

# Null
$ys->load("null");  # undef
```

### Collections

```perl
# Mappings (hashes)
my $data = $ys->load(q{
name: Alice
age: 30
city: Seattle
});
# {name => "Alice", age => 30, city => "Seattle"}

# Sequences (arrays)
my $data = $ys->load(q{
- apple
- banana
- orange
});
# ["apple", "banana", "orange"]

# Flow style
my $data = $ys->load("[a, b, c]");
# ["a", "b", "c"]
```

### Nested Structures

```perl
my $data = $ys->load(q{
person:
  name: Alice
  age: 30
  hobbies:
    - reading
    - coding
    - hiking
});
# {
#   person => {
#     name => "Alice",
#     age => 30,
#     hobbies => ["reading", "coding", "hiking"]
#   }
# }
```

### Multi-Document YAML

```perl
# Load all documents from a multi-document YAML string
my $docs = $ys->load_all(q{---
name: Document 1
---
name: Document 2
---
name: Document 3
});
# [
#   {name => "Document 1"},
#   {name => "Document 2"},
#   {name => "Document 3"}
# ]
```

### Type Coercion

YAMLStar follows YAML 1.2 core schema type inference:

```perl
my $data = $ys->load(q{
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
});
# {
#   string => "hello",
#   integer => 42,
#   float => 3.14,
#   bool_true => JSON::PP::true,
#   bool_false => JSON::PP::false,
#   null_value => undef
# }
```

### Error Handling

```perl
use Try::Tiny;

try {
    my $data = $ys->load("invalid: yaml: syntax");
} catch {
    warn "Error loading YAML: $_";
};
```

Or with eval:

```perl
my $data = eval { $ys->load("invalid: yaml: syntax") };
if ($@) {
    warn "Error loading YAML: $@";
}
```

### Version Information

```perl
# Get YAMLStar version
my $version = $ys->version();
print "YAMLStar version: $version\n";
```

## API Reference

### `YAMLStar` Class

#### `new()`
Create a new YAMLStar instance. Each instance maintains its own GraalVM isolate.

```perl
my $ys = YAMLStar->new();
```

#### `load($yaml_input)`
Load a single YAML document.

**Parameters:**
- `$yaml_input` (string): String containing YAML content

**Returns:**
- Perl data structure representing the YAML document (hashref, arrayref, string, number, boolean, or undef)

**Dies:**
- If the YAML is malformed

**Example:**
```perl
my $data = $ys->load("key: value");
```

#### `load_all($yaml_input)`
Load all YAML documents from a multi-document string.

**Parameters:**
- `$yaml_input` (string): String containing one or more YAML documents

**Returns:**
- Arrayref of Perl data structures, one per YAML document

**Dies:**
- If the YAML is malformed

**Example:**
```perl
my $docs = $ys->load_all("---\ndoc1\n---\ndoc2");
```

#### `version()`
Get the YAMLStar version string.

**Returns:**
- String: Version string

**Example:**
```perl
my $version = $ys->version();
```

## Development

### Running Tests

```bash
# Run all tests
make test

# Run only FFI smoke tests
make test-ffi
```

### Building libyamlstar

If you need to rebuild the shared library:

```bash
cd ../libyamlstar
make clean
make native
```

## Requirements

- **Perl**: 5.16.0 or higher
- **libyamlstar**: Shared library (installed separately)
- **System**: Linux or macOS
- **CPAN Modules**: Moo, FFI::Platypus, FFI::CheckLib, Cpanel::JSON::XS

## Library Search Path

The module searches for `libyamlstar.so` (or `.dylib` on macOS) in:

1. Development path (relative to module: `../libyamlstar/lib/`)
2. Directories in `LD_LIBRARY_PATH` environment variable
3. `/usr/local/lib` (default install location)
4. `/usr/lib`
5. `~/.local/lib` (user-local install location)

## Comparison to YAML::XS

| Feature | YAMLStar | YAML::XS |
|---------|----------|----------|
| YAML Version | 1.2 | 1.1 |
| Implementation | Pure Clojure | C (libyaml) |
| Type Inference | YAML 1.2 core schema | YAML 1.1 + custom |
| Native Performance | Yes (GraalVM) | Yes (C extension) |
| Dependencies | libyamlstar.so | libyaml |
| OO Interface | Moo | No |

## License

MIT License - See [License](License) file

## Credits

Created by Ingy döt Net, inventor of YAML.

YAMLStar is built on the YAML Reference Parser (pure Clojure implementation).

## Links

- **GitHub**: https://github.com/yaml/yamlstar
- **YAML Specification**: https://yaml.org/spec/1.2/spec.html
