# YAMLStar C# Bindings

C# bindings for YAMLStar - a pure YAML 1.2 loader implemented in Clojure.

## Features

- **YAML 1.2 Spec Compliance**: 100% compliant with YAML 1.2 core schema
- **Pure Implementation**: No dependencies on YamlDotNet or other external parsers
- **Fast Native Performance**: Uses GraalVM native-image shared library
- **Simple API**: Load YAML documents with a single method call
- **Multi-Document Support**: Load multiple YAML documents from a single string
- **IDisposable Pattern**: Proper resource management

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

### Install NuGet Package

```bash
dotnet add package YAMLStar
```

For development:

```bash
dotnet build
```

## Quick Start

```csharp
using YAMLStar;

// Create a YAMLStar instance
using var ys = new YAMLStar();

// Load a simple YAML string
var data = ys.Load("key: value");
// Returns a JsonElement
```

## Usage Examples

### Basic Types

```csharp
using YAMLStar;
using System.Text.Json;

using var ys = new YAMLStar();

// Strings
var str = ys.Load("hello");
var element = (JsonElement)str;
Console.WriteLine(element.GetString());  // "hello"

// Integers
var num = ys.Load("42");
var element = (JsonElement)num;
Console.WriteLine(element.GetInt32());  // 42

// Floats
var flt = ys.Load("3.14");
var element = (JsonElement)flt;
Console.WriteLine(element.GetDouble());  // 3.14

// Booleans
var t = ys.Load("true");
var element = (JsonElement)t;
Console.WriteLine(element.GetBoolean());  // True

// Null
var n = ys.Load("null");
var element = (JsonElement)n;
Console.WriteLine(element.ValueKind);  // Null
```

### Collections

```csharp
// Mappings (objects)
var data = ys.Load(@"
name: Alice
age: 30
city: Seattle
");
var element = (JsonElement)data;
Console.WriteLine(element.GetProperty("name").GetString());  // "Alice"

// Sequences (arrays)
var data = ys.Load(@"
- apple
- banana
- orange
");
var element = (JsonElement)data;
Console.WriteLine(element[0].GetString());  // "apple"

// Flow style
var data = ys.Load("[a, b, c]");
var element = (JsonElement)data;
Console.WriteLine(element.GetArrayLength());  // 3
```

### Nested Structures

```csharp
var data = ys.Load(@"
person:
  name: Alice
  age: 30
  hobbies:
    - reading
    - coding
    - hiking
");
var element = (JsonElement)data;
var person = element.GetProperty("person");
Console.WriteLine(person.GetProperty("name").GetString());  // "Alice"
```

### Multi-Document YAML

```csharp
// Load all documents from a multi-document YAML string
var docs = ys.LoadAll(@"---
name: Document 1
---
name: Document 2
---
name: Document 3
");
var element = (JsonElement)docs;
Console.WriteLine(element.GetArrayLength());  // 3
```

### Type Coercion

YAMLStar follows YAML 1.2 core schema type inference:

```csharp
var data = ys.Load(@"
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
");
var element = (JsonElement)data;
// All types are properly inferred and converted
```

### Error Handling

```csharp
try
{
    var data = ys.Load("invalid: yaml: syntax");
}
catch (YAMLStarException ex)
{
    Console.Error.WriteLine($"Error loading YAML: {ex.Message}");
}
```

### Version Information

```csharp
// Get YAMLStar version
var version = ys.Version();
Console.WriteLine($"YAMLStar version: {version}");
```

### Resource Cleanup

```csharp
// Using statement automatically disposes
using var ys = new YAMLStar();
// ... use ys ...
// Disposed automatically at end of scope

// Or manual dispose
var ys = new YAMLStar();
try
{
    // ... use ys ...
}
finally
{
    ys.Dispose();
}
```

## API Reference

### `YAMLStar` Class

#### `YAMLStar()` Constructor
Create a new YAMLStar instance. Each instance maintains its own GraalVM isolate.

```csharp
var ys = new YAMLStar();
```

#### `object? Load(string yaml)`
Load a single YAML document.

**Parameters:**
- `yaml` (string): String containing YAML content

**Returns:**
- `object?` representing the YAML document (typically a JsonElement)

**Throws:**
- `YAMLStarException` if the YAML is malformed
- `ObjectDisposedException` if called after disposal

**Example:**
```csharp
var data = ys.Load("key: value");
```

#### `object? LoadAll(string yaml)`
Load all YAML documents from a multi-document string.

**Parameters:**
- `yaml` (string): String containing one or more YAML documents

**Returns:**
- `object?` representing an array of documents (typically a JsonElement array)

**Throws:**
- `YAMLStarException` if the YAML is malformed
- `ObjectDisposedException` if called after disposal

**Example:**
```csharp
var docs = ys.LoadAll("---\ndoc1\n---\ndoc2");
```

#### `string? Version()`
Get the YAMLStar version string.

**Returns:**
- `string?`: Version string

**Throws:**
- `ObjectDisposedException` if called after disposal

**Example:**
```csharp
var version = ys.Version();
```

#### `void Dispose()`
Tear down the GraalVM isolate and free resources. Should be called when done using the instance.

**Example:**
```csharp
ys.Dispose();
```

## Development

### Running Tests

```bash
# Run all tests
make test

# Or directly with dotnet
dotnet test test/YAMLStar.Tests.csproj
```

### Building

```bash
# Build the project
make build

# Or directly with dotnet
dotnet build
```

## Requirements

- **.NET**: 8.0 or higher
- **libyamlstar**: Shared library (installed separately)
- **System**: Linux or macOS

## Library Search Path

The module searches for `libyamlstar.so` (or `.dylib` on macOS) using the system's dynamic library loading mechanism (typically `LD_LIBRARY_PATH`).

## Comparison to YamlDotNet

| Feature | YAMLStar | YamlDotNet |
|---------|----------|------------|
| YAML Version | 1.2 | 1.1 + some 1.2 |
| Implementation | Pure Clojure | C# |
| Type Inference | YAML 1.2 core schema | Custom |
| Native Performance | Yes (GraalVM) | Managed code |
| Dependencies | libyamlstar.so | None |

## License

MIT License - See [License](License) file

## Credits

Created by Ingy döt Net, inventor of YAML.

YAMLStar is built on the YAML Reference Parser (pure Clojure implementation).

## Links

- **GitHub**: https://github.com/yaml/yamlstar
- **YAML Specification**: https://yaml.org/spec/1.2/spec.html
