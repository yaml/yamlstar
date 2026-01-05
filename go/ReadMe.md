# YAMLStar Go Bindings

Go bindings for YAMLStar - a pure YAML 1.2 loader implemented in Clojure.

## Features

- **YAML 1.2 Spec Compliance**: 100% compliant with YAML 1.2 core schema
- **Pure Implementation**: No dependencies on external YAML parsers
- **Fast Native Performance**: Uses GraalVM native-image shared library
- **Simple API**: Load YAML documents with a single function call
- **Multi-Document Support**: Load multiple YAML documents from a single string
- **Thread Safe**: Proper GraalVM isolate thread management

## Installation

### Prerequisites

First, build and install the shared library:

```bash
cd ../libyamlstar
make build
sudo make install PREFIX=/usr/local
```

Or install to user-local directory:

```bash
cd ../libyamlstar
make build
make install PREFIX=~/.local
```

### Install Go Package

```bash
go get github.com/yaml/yamlstar/go@latest
```

Set required environment variables:

```bash
export CGO_CFLAGS="-I $HOME/.local/include"
export CGO_LDFLAGS="-L $HOME/.local/lib"
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

## Quick Start

```go
package main

import (
    "fmt"
    "log"

    "github.com/yaml/yamlstar/go"
)

func main() {
    // Load a simple YAML string
    data, err := yamlstar.Load("key: value")
    if err != nil {
        log.Fatal(err)
    }
    fmt.Printf("%v\n", data) // map[key:value]
}
```

## Usage Examples

### Basic Types

```go
import "github.com/yaml/yamlstar/go"

// Strings
data, _ := yamlstar.Load("hello")  // "hello"

// Integers (returned as float64)
data, _ := yamlstar.Load("42")     // float64(42)

// Floats
data, _ := yamlstar.Load("3.14")   // 3.14

// Booleans
data, _ := yamlstar.Load("true")   // true
data, _ := yamlstar.Load("false")  // false

// Null
data, _ := yamlstar.Load("null")   // nil
```

### Collections

```go
// Mappings (map[string]any)
data, _ := yamlstar.Load(`
name: Alice
age: 30
city: Seattle
`)
// map[string]any{"name": "Alice", "age": float64(30), "city": "Seattle"}

// Sequences ([]any)
data, _ := yamlstar.Load(`
- apple
- banana
- orange
`)
// []any{"apple", "banana", "orange"}

// Flow style
data, _ := yamlstar.Load("[a, b, c]")
// []any{"a", "b", "c"}
```

### Multi-Document YAML

```go
// Load all documents from a multi-document YAML string
docs, err := yamlstar.LoadAll(`---
name: Document 1
---
name: Document 2
---
name: Document 3
`)
// []any{
//   map[string]any{"name": "Document 1"},
//   map[string]any{"name": "Document 2"},
//   map[string]any{"name": "Document 3"},
// }
```

### Error Handling

```go
data, err := yamlstar.Load(`invalid: yaml: syntax`)
if err != nil {
    fmt.Printf("Error loading YAML: %v\n", err)
}

// Check for specific error types
var yamlErr *yamlstar.YAMLError
if errors.As(err, &yamlErr) {
    fmt.Printf("YAML error type: %s\n", yamlErr.Type)
    fmt.Printf("YAML error cause: %s\n", yamlErr.Cause)
}
```

### Version Information

```go
// Get library version
version, err := yamlstar.LibVersion()
fmt.Printf("YAMLStar library version: %s\n", version)

// Get binding version constant
fmt.Printf("Go binding version: %s\n", yamlstar.Version)
```

## API Reference

### Functions

#### `Load(input string) (any, error)`

Load a single YAML document.

**Parameters:**
- `input`: String containing YAML content

**Returns:**
- Go value representing the YAML document (nil, bool, float64, string, []any, or map[string]any)
- Error if the YAML is malformed or library not initialized

#### `LoadAll(input string) ([]any, error)`

Load all YAML documents from a multi-document string.

**Parameters:**
- `input`: String containing one or more YAML documents

**Returns:**
- Slice of Go values, one per YAML document
- Error if the YAML is malformed or library not initialized

#### `LibVersion() (string, error)`

Get the YAMLStar library version string.

**Returns:**
- Version string from the native library
- Error if library not initialized

### Constants

#### `Version`

The version of the Go binding (matches libyamlstar version).

### Types

#### `YAMLError`

Represents an error returned from the yamlstar library.

```go
type YAMLError struct {
    Cause   string // The error cause
    Type    string // The error type
    Message string // Optional detailed message
}
```

## Development

### Running Tests

```bash
# Run all tests
make test

# Run specific test
go test -v -run TestLoadSimpleMapping
```

### Building

```bash
# Build the package
make build

# Run the example
make example
```

## Requirements

- **Go**: 1.18 or higher
- **CGO**: Enabled
- **libyamlstar**: Shared library (installed separately)
- **System**: Linux or macOS

## Library Search

The CGO linker searches for `libyamlstar.so` in standard library paths.
Set `CGO_LDFLAGS` and `LD_LIBRARY_PATH` to include custom locations.

## Thread Safety

The library is thread-safe. Each call to Load/LoadAll creates a dedicated
GraalVM thread that is properly cleaned up after the call completes.
The package uses `runtime.LockOSThread()` to ensure proper GraalVM operation.

## License

MIT License - See [License](License) file

## Credits

Created by Ingy dot Net, inventor of YAML.

YAMLStar is built on the YAML Reference Parser (pure Clojure implementation).

## Links

- **GitHub**: https://github.com/yaml/yamlstar
- **YAML Specification**: https://yaml.org/spec/1.2/spec.html
