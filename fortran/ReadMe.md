# YAMLStar Fortran Bindings

Fortran bindings for YAMLStar - a pure YAML 1.2 loader implemented in Clojure.

## Features

- **YAML 1.2 Spec Compliance**: 100% compliant with YAML 1.2 core schema
- **Pure Implementation**: No dependencies on other YAML parsers
- **Fast Native Performance**: Uses GraalVM native-image shared library
- **Simple API**: Load YAML documents with clean Fortran interface
- **Multi-Document Support**: Load multiple YAML documents from a single string
- **Type-Safe**: Uses ISO_C_BINDING for safe C interoperability
- **FPM Support**: Build and test with Fortran Package Manager

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

### Build with FPM

```bash
fpm build
```

For development:

```bash
make build
```

## Quick Start

```fortran
program example
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: result

  ! Initialize YAMLStar
  call ys%init()

  ! Load a YAML document (returns JSON string)
  result = ys%load("key: value")
  print *, result
  ! Output: {"data":{"key":"value"}}

  ! Cleanup
  call ys%destroy()
end program example
```

## Usage Examples

### Basic Types

```fortran
program basic_types
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: result

  call ys%init()

  ! String
  result = ys%load("hello")
  ! Returns: {"data":"hello"}

  ! Integer
  result = ys%load("42")
  ! Returns: {"data":42}

  ! Float
  result = ys%load("3.14")
  ! Returns: {"data":3.14}

  ! Boolean
  result = ys%load("true")
  ! Returns: {"data":true}

  ! Null
  result = ys%load("null")
  ! Returns: {"data":null}

  call ys%destroy()
end program basic_types
```

### Collections

```fortran
program collections
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: result
  character(len=*), parameter :: yaml_map = &
    "name: Alice" // new_line('A') // &
    "age: 30" // new_line('A') // &
    "city: Seattle"

  call ys%init()

  ! Mapping (object)
  result = ys%load(yaml_map)
  ! Returns: {"data":{"name":"Alice","age":30,"city":"Seattle"}}

  ! Sequence (array)
  result = ys%load("[apple, banana, orange]")
  ! Returns: {"data":["apple","banana","orange"]}

  call ys%destroy()
end program collections
```

### Nested Structures

```fortran
program nested
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: result
  character(len=*), parameter :: yaml = &
    "person:" // new_line('A') // &
    "  name: Alice" // new_line('A') // &
    "  age: 30" // new_line('A') // &
    "  hobbies:" // new_line('A') // &
    "    - reading" // new_line('A') // &
    "    - coding"

  call ys%init()
  result = ys%load(yaml)
  print *, result

  call ys%destroy()
end program nested
```

### Multi-Document YAML

```fortran
program multi_doc
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: result
  character(len=*), parameter :: yaml = &
    "---" // new_line('A') // &
    "name: Document 1" // new_line('A') // &
    "---" // new_line('A') // &
    "name: Document 2" // new_line('A') // &
    "---" // new_line('A') // &
    "name: Document 3"

  call ys%init()
  result = ys%load_all(yaml)
  ! Returns array of documents
  print *, result

  call ys%destroy()
end program multi_doc
```

### Error Handling

YAMLStar returns JSON responses with either `{"data": ...}` for success or `{"error": {"cause": "..."}}` for errors. You can check for errors by searching for `"error"` in the result string.

```fortran
program error_handling
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: result

  call ys%init()

  result = ys%load("invalid: yaml: :")
  if (index(result, '"error"') > 0) then
    print *, "Error loading YAML:"
    print *, result
  end if

  call ys%destroy()
end program error_handling
```

### Version Information

```fortran
program version_info
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: ver

  call ys%init()
  ver = ys%version()
  print *, "YAMLStar version: ", ver

  call ys%destroy()
end program version_info
```

## API Reference

### `yamlstar_t` Type

The main YAMLStar class/derived type.

#### `init()`
Initialize YAMLStar and create a new GraalVM isolate.

**Example:**
```fortran
type(yamlstar_t) :: ys
call ys%init()
```

#### `destroy()`
Tear down the GraalVM isolate and free resources. Should be called when done using the instance.

**Example:**
```fortran
call ys%destroy()
```

#### `load(yaml) result(json)`
Load a single YAML document.

**Parameters:**
- `yaml` (character): String containing YAML content

**Returns:**
- `json` (allocatable character): JSON response string

**Example:**
```fortran
result = ys%load("key: value")
```

#### `load_all(yaml) result(json)`
Load all YAML documents from a multi-document string.

**Parameters:**
- `yaml` (character): String containing one or more YAML documents

**Returns:**
- `json` (allocatable character): JSON response string with array of documents

**Example:**
```fortran
result = ys%load_all("---\ndoc1\n---\ndoc2")
```

#### `version() result(ver)`
Get the YAMLStar version string.

**Returns:**
- `ver` (allocatable character): Version string

**Example:**
```fortran
ver = ys%version()
```

## Response Format

All API functions return JSON strings in one of two formats:

**Success:**
```json
{"data": <yaml_content>}
```

**Error:**
```json
{"error": {"cause": "error message"}}
```

## JSON Parsing

The Fortran bindings return raw JSON strings. For parsing JSON in Fortran, you can use:
- [json-fortran](https://github.com/jacobwilliams/json-fortran) - JSON parser for Fortran

## Development

### Running Tests

```bash
# Run all tests
make test

# Or directly with fpm
fpm test
```

### Building

```bash
# Build the library
make build

# Or directly with fpm
fpm build
```

## Requirements

- **Fortran**: gfortran (installed via fortran.mk)
- **FPM**: Fortran Package Manager v0.12.0 (installed via fortran.mk)
- **libyamlstar**: Shared library (installed separately)
- **System**: Linux x86_64

## Library Search Path

The module searches for `libyamlstar.so` using the system's dynamic library loading mechanism (typically `LD_LIBRARY_PATH`).

## Comparison to Other YAML Libraries

| Feature | YAMLStar | Other Fortran YAML libs |
|---------|----------|-------------------------|
| YAML Version | 1.2 | 1.1 or partial |
| Implementation | Pure Clojure | C wrappers |
| Type Inference | YAML 1.2 core schema | Custom |
| Native Performance | Yes (GraalVM) | Varies |
| Dependencies | libyamlstar.so | libyaml, etc. |

## License

MIT License - See [License](License) file

## Credits

Created by Ingy döt Net, inventor of YAML.

YAMLStar is built on the YAML Reference Parser (pure Clojure implementation).

## Links

- **GitHub**: https://github.com/yaml/yamlstar
- **YAML Specification**: https://yaml.org/spec/1.2/spec.html
- **Fortran Package Manager**: https://fpm.fortran-lang.org/
