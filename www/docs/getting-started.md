# Getting Started

YAMLStar provides a consistent YAML loading API across all supported languages.
This guide will get you up and running quickly.

## Installation

Choose your language below for installation instructions.

=== "Python"

    ```bash
    pip install yamlstar
    ```

=== "Node.js"

    ```bash
    npm install yamlstar
    ```

=== "Clojure"

    Add to your `deps.edn`:
    ```clojure
    {:deps {org.yamlstar/yamlstar {:mvn/version "0.1.2"}}}
    ```

    Or `project.clj` (Leiningen):
    ```clojure
    :dependencies [[org.yamlstar/yamlstar "0.1.2"]]
    ```

=== "Go"

    ```bash
    go get github.com/yaml/yamlstar-go
    ```

=== "Java"

    Maven:
    ```xml
    <dependency>
      <groupId>com.yaml</groupId>
      <artifactId>yamlstar</artifactId>
      <version>0.1.2</version>
    </dependency>
    ```

=== "Rust"

    Add to your `Cargo.toml`:
    ```toml
    [dependencies]
    yamlstar = "0.1.2"
    ```

=== "Perl"

    ```bash
    cpanm YAMLStar
    ```

=== "C#"

    ```bash
    dotnet add package YAMLStar
    ```

=== "Fortran"

    See [fortran/ReadMe.md](https://github.com/yaml/yamlstar/tree/main/fortran)
    for FPM installation.

## Basic Usage

All language bindings follow a similar API pattern.

### Python

```python
from yamlstar import YAMLStar

# Create a YAMLStar instance
ys = YAMLStar()

# Load a YAML string
data = ys.load('key: value')
print(data)  # {'key': 'value'}

# Load with type coercion
data = ys.load('''
num: 42
bool: true
null: null
''')
print(data)
# {'num': 42, 'bool': True, 'null': None}

# Load multiple documents
docs = ys.load_all('''
---
doc1
---
doc2
''')
print(docs)  # ['doc1', 'doc2']

# Always close when done
ys.close()
```

### Node.js

```javascript
const YAMLStar = require('yamlstar');

// Create a YAMLStar instance
const ys = new YAMLStar();

// Load a YAML string
const data = ys.load('key: value');
console.log(data);  // { key: 'value' }

// Load with type coercion
const data2 = ys.load(`
num: 42
bool: true
null: null
`);
console.log(data2);
// { num: 42, bool: true, null: null }

// Load multiple documents
const docs = ys.loadAll(`
---
doc1
---
doc2
`);
console.log(docs);  // ['doc1', 'doc2']

// Always close when done
ys.close();
```

### Clojure

```clojure
(require '[yamlstar.core :as yaml])

;; Load a YAML string
(yaml/load "key: value")
;=> {"key" "value"}

;; Load with type coercion
(yaml/load "
num: 42
bool: true
null: null
")
;=> {"num" 42, "bool" true, "null" nil}

;; Load multiple documents
(yaml/load-all "---\ndoc1\n---\ndoc2")
;=> ["doc1" "doc2"]

;; Complex nested structures
(yaml/load "
person:
  name: Alice
  age: 30
  hobbies: [reading, coding]
")
;=> {"person" {"name" "Alice", "age" 30, "hobbies" ["reading" "coding"]}}
```

### Go

```go
package main

import (
    "fmt"
    "github.com/yaml/yamlstar-go"
)

func main() {
    // Create a YAMLStar instance
    ys := yamlstar.New()
    defer ys.Close()

    // Load a YAML string
    data := ys.Load("key: value")
    fmt.Println(data)
    // map[string]interface{}{"key": "value"}

    // Load with type coercion
    data2 := ys.Load(`
num: 42
bool: true
null: null
`)
    fmt.Println(data2)
    // map[string]interface{}{"num": 42, "bool": true, "null": nil}

    // Load multiple documents
    docs := ys.LoadAll("---\ndoc1\n---\ndoc2")
    fmt.Println(docs)
    // []interface{}{"doc1", "doc2"}
}
```

### Java

```java
import com.yaml.yamlstar.YAMLStar;
import java.util.Map;

public class Example {
    public static void main(String[] args) {
        // Create a YAMLStar instance
        YAMLStar ys = new YAMLStar();

        // Load a YAML string
        Map<String, Object> data = ys.load("key: value");
        System.out.println(data);
        // {key=value}

        // Load with type coercion
        Map<String, Object> data2 = ys.load(
            "num: 42\n" +
            "bool: true\n" +
            "null: null"
        );
        System.out.println(data2);
        // {num=42, bool=true, null=null}

        // Always close when done
        ys.close();
    }
}
```

## Core Concepts

### Type Coercion

YAMLStar implements the YAML 1.2 Core Schema for automatic type detection:

```yaml
# Null values
null_value: null
also_null: ~

# Booleans (case insensitive)
bool_true: true
bool_false: FALSE

# Integers
integer: 42
negative: -42

# Floats
float: 3.14
infinity: .inf
neg_infinity: -.inf
not_a_number: .nan

# Strings (everything else)
string: hello world
quoted: "42"  # String, not integer
```

### Explicit Tags

Override automatic type detection with explicit tags:

```yaml
# Force string type
number_as_string: !!str 42

# Force integer type
string_as_int: !!int "42"

# Force float type
int_as_float: !!float 42
```

### Anchors and Aliases

Reuse YAML nodes with anchors (`&name`) and aliases (`*name`):

```yaml
defaults: &defaults
  timeout: 30
  retries: 3

development:
  <<: *defaults
  host: localhost

production:
  <<: *defaults
  host: prod.example.com
```

### Multi-Document Streams

Load multiple YAML documents from a single string:

```yaml
---
document: 1
---
document: 2
---
document: 3
```

Use `load_all()` (or `loadAll()` in camelCase languages) to get all documents
as a list.

## Advanced Usage

### Loading from Files

Most bindings don't include file I/O (to keep them lightweight), so you'll
typically read the file yourself:

=== "Python"

    ```python
    with open('config.yaml', 'r') as f:
        data = ys.load(f.read())
    ```

=== "Node.js"

    ```javascript
    const fs = require('fs');
    const yaml = fs.readFileSync('config.yaml', 'utf8');
    const data = ys.load(yaml);
    ```

=== "Clojure"

    ```clojure
    (require '[clojure.java.io :as io])
    (yaml/load (slurp "config.yaml"))
    ```

### Error Handling

YAMLStar will raise exceptions for invalid YAML:

=== "Python"

    ```python
    try:
        data = ys.load('invalid: yaml: syntax')
    except Exception as e:
        print(f"Error: {e}")
    ```

=== "Node.js"

    ```javascript
    try {
        const data = ys.load('invalid: yaml: syntax');
    } catch (e) {
        console.error(`Error: ${e.message}`);
    }
    ```

## Next Steps

- Explore [Language Bindings](bindings.md) for language-specific details
- Review the [Roadmap](roadmap.md) to see what's coming next
- Check out [About](about.md) for architectural details
- Visit the [GitHub repository](https://github.com/yaml/yamlstar) for source
  code and examples

## Getting Help

- **GitHub Issues**: Report bugs or request features at
  [github.com/yaml/yamlstar/issues](https://github.com/yaml/yamlstar/issues)
- **Documentation**: Each binding has detailed documentation in its directory
  `ReadMe.md`
- **Examples**: Check the `example/` directory for working examples in each
  language

## Building from Source

If you want to build YAMLStar from source or contribute to development:

```bash
# Clone the repository
git clone https://github.com/yaml/yamlstar.git
cd yamlstar

# Run core tests (auto-installs dependencies)
cd core
make test

# Build the shared library
cd ../libyamlstar
make build

# Test a specific binding
cd ../python
make test
```

The build system uses [Makes](https://github.com/makeplus/makes), which
auto-installs all dependencies (Leiningen, GraalVM, language tools) on first
run.
Everything is installed locally in `.cache/` - no system-wide installation
required.
