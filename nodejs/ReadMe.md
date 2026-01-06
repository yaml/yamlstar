# YAMLStar Node.js Bindings

Node.js bindings for YAMLStar - a pure YAML 1.2 loader implemented in Clojure.

## Features

- **YAML 1.2 Spec Compliance**: 100% compliant with YAML 1.2 core schema
- **Pure Implementation**: No dependencies on js-yaml or other external parsers
- **Fast Native Performance**: Uses GraalVM native-image shared library
- **Simple API**: Load YAML documents with a single method call
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

### Install Node.js Package

```bash
npm install @yaml/yamlstar
```

For development:

```bash
npm install
```

## Quick Start

```javascript
const YAMLStar = require('@yaml/yamlstar');

// Create a YAMLStar instance
const ys = new YAMLStar();

// Load a simple YAML string
const data = ys.load('key: value');
console.log(data);  // { key: 'value' }

// Clean up when done
ys.close();
```

## Usage Examples

### Basic Types

```javascript
const YAMLStar = require('@yaml/yamlstar');
const ys = new YAMLStar();

// Strings
ys.load('hello');  // 'hello'

// Integers
ys.load('42');  // 42

// Floats
ys.load('3.14');  // 3.14

// Booleans
ys.load('true');   // true
ys.load('false');  // false

// Null
ys.load('null');  // null
```

### Collections

```javascript
// Mappings (objects)
const data = ys.load(`
name: Alice
age: 30
city: Seattle
`);
// { name: 'Alice', age: 30, city: 'Seattle' }

// Sequences (arrays)
const data = ys.load(`
- apple
- banana
- orange
`);
// ['apple', 'banana', 'orange']

// Flow style
const data = ys.load('[a, b, c]');
// ['a', 'b', 'c']
```

### Nested Structures

```javascript
const data = ys.load(`
person:
  name: Alice
  age: 30
  hobbies:
    - reading
    - coding
    - hiking
`);
// {
//   person: {
//     name: 'Alice',
//     age: 30,
//     hobbies: ['reading', 'coding', 'hiking']
//   }
// }
```

### Multi-Document YAML

```javascript
// Load all documents from a multi-document YAML string
const docs = ys.loadAll(`---
name: Document 1
---
name: Document 2
---
name: Document 3
`);
// [
//   { name: 'Document 1' },
//   { name: 'Document 2' },
//   { name: 'Document 3' }
// ]
```

### Type Coercion

YAMLStar follows YAML 1.2 core schema type inference:

```javascript
const data = ys.load(`
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
`);
// {
//   string: 'hello',
//   integer: 42,
//   float: 3.14,
//   bool_true: true,
//   bool_false: false,
//   null_value: null
// }
```

### Error Handling

```javascript
try {
  const data = ys.load('invalid: yaml: syntax');
} catch (error) {
  console.error('Error loading YAML:', error.message);
}
```

### Version Information

```javascript
// Get YAMLStar version
const version = ys.version();
console.log(`YAMLStar version: ${version}`);
```

### Resource Cleanup

```javascript
const ys = new YAMLStar();

// ... use ys ...

// Clean up GraalVM isolate when done
ys.close();
```

## API Reference

### `YAMLStar` Class

#### `new YAMLStar()`
Create a new YAMLStar instance. Each instance maintains its own GraalVM isolate.

```javascript
const ys = new YAMLStar();
```

#### `load(yamlInput)`
Load a single YAML document.

**Parameters:**
- `yamlInput` (string): String containing YAML content

**Returns:**
- JavaScript value representing the YAML document (object, array, string, number, boolean, or null)

**Throws:**
- `Error` if the YAML is malformed

**Example:**
```javascript
const data = ys.load('key: value');
```

#### `loadAll(yamlInput)`
Load all YAML documents from a multi-document string.

**Parameters:**
- `yamlInput` (string): String containing one or more YAML documents

**Returns:**
- Array of JavaScript values, one per YAML document

**Throws:**
- `Error` if the YAML is malformed

**Example:**
```javascript
const docs = ys.loadAll('---\ndoc1\n---\ndoc2');
```

#### `version()`
Get the YAMLStar version string.

**Returns:**
- string: Version string

**Example:**
```javascript
const version = ys.version();
```

#### `close()`
Tear down the GraalVM isolate and free resources. Should be called when done using the instance.

**Example:**
```javascript
ys.close();
```

## Development

### Running Tests

```bash
# Run all tests
make test

# Or directly with node
node test/test.js
```

### Building libyamlstar

If you need to rebuild the shared library:

```bash
cd ../libyamlstar
make clean
make native
```

## Requirements

- **Node.js**: 18.0.0 or higher
- **libyamlstar**: Shared library (installed separately)
- **System**: Linux or macOS
- **Dependencies**: `@makeomatic/ffi-napi`, `ref-napi`

## Library Search Path

The module searches for `libyamlstar.so` (or `.dylib` on macOS) in:

1. Development path (relative to module: `../../libyamlstar/lib/`)
2. Directories in `LD_LIBRARY_PATH` environment variable
3. `/usr/local/lib` (default install location)
4. `~/.local/lib` (user-local install location)

## Comparison to js-yaml

| Feature | YAMLStar | js-yaml |
|---------|----------|---------|
| YAML Version | 1.2 | 1.2 |
| Implementation | Pure Clojure | JavaScript |
| Type Inference | YAML 1.2 core schema | YAML 1.2 + custom |
| Native Performance | Yes (GraalVM) | No |
| Dependencies | libyamlstar.so | None |

## License

MIT License - See [License](License) file

## Credits

Created by Ingy döt Net, inventor of YAML.

YAMLStar is built on the YAML Reference Parser (pure Clojure implementation).

## Links

- **GitHub**: https://github.com/yaml/yamlstar
- **YAML Specification**: https://yaml.org/spec/1.2/spec.html
