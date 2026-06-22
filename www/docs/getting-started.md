# Getting Started

YAMLStar provides consistent YAML loading and dumping APIs across all supported
languages.
This guide will get you up and running quickly.

## Installation

Install the `yaml` CLI and the `libyamlstar` shared library:

```bash
curl -sSL https://yamlstar.org/install | bash
```

Use `BIN=1` to install only the CLI, `LIB=1` to install only the shared
library, or `PREFIX=...` to choose an installation directory.

```bash
curl -sSL https://yamlstar.org/install | BIN=1 bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
curl -sSL https://yamlstar.org/install | PREFIX=/opt/yamlstar bash
```

Or install with Homebrew:

```bash
brew trust yaml/yamlstar
brew tap yaml/yamlstar

brew install yaml/yamlstar/yamlstar
brew install yaml/yamlstar/libyamlstar
```

See [Installing YAMLStar](installation.md) for details.

## CLI

The `yaml` command reads YAML from a file or stdin and prints compact JSON:

```bash
printf 'a: 1\n' | yaml
yaml -J config.yaml
```

See the [CLI documentation](cli.md) for all options.

## Language Binding Packages

Choose your language below for package manager installation.

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

=== "Crystal"

    Add to your `shard.yml`:
    ```yaml
    dependencies:
      yamlstar:
        github: yaml/yamlstar-crystal
    ```

=== "Haskell"

    ```bash
    cabal install yamlstar
    ```

=== "Julia"

    ```julia
    using Pkg
    Pkg.add("YAMLStar")
    ```

=== "Raku"

    ```bash
    zef install YAMLStar
    ```

=== "Delphi (Pascal)"

    Requires building the shared library first:
    ```bash
    cd libyamlstar
    make native
    sudo make install PREFIX=/usr/local

    cd ../delphi
    make build
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

# Dump native values to YAML
yaml_text = ys.dump({'foo': [['bar']]})
print(yaml_text)
# foo:
# - - bar

# Dump multiple YAML documents
stream = ys.dump_all(['doc1', {'a': 1}, ['b']])
print(stream)
# ---
# doc1
# ---
# a: 1
# ---
# - b

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

// Dump native values to YAML
const yamlText = ys.dump({foo: [['bar']]});
console.log(yamlText);
// foo:
// - - bar

// Dump multiple YAML documents
const stream = ys.dumpAll(['doc1', {a: 1}, ['b']]);
console.log(stream);
// ---
// doc1
// ---
// a: 1
// ---
// - b

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

;; Dump native values to YAML
(yaml/dump {"foo" [["bar"]]})
;=> "foo:\n- - bar\n"

;; Dump multiple YAML documents
(yaml/dump-all ["doc1" {"a" 1} ["b"]])
;=> "---\ndoc1\n---\na: 1\n---\n- b\n"

;; Complex nested structures
(yaml/load "
person:
  name: Alice
  age: 30
  hobbies: [reading, coding]
")
;=> {"person" {"name" "Alice", "age" 30, "hobbies" ["reading" "coding"]}}
```

### Delphi (Pascal)

```pascal
program example;
uses yamlstar, fpjson, sysutils;

var
  ys: TYAMLStar;
  data: TJSONData;
  docs: TJSONArray;
  yaml: string;
  i: Integer;
begin
  // Create a YAMLStar instance
  ys := TYAMLStar.Create;
  try
    // Load a YAML string
    data := ys.Load('key: value');
    try
      WriteLn(data.FormatJSON);
      // {"key":"value"}
    finally
      data.Free;
    end;

    // Load with type coercion
    yaml := 'num: 42' + LineEnding +
            'bool: true' + LineEnding +
            'null: null';
    data := ys.Load(yaml);
    try
      WriteLn(data.FormatJSON);
      // {"num":42,"bool":true,"null":null}
    finally
      data.Free;
    end;

    // Load multiple documents
    yaml := '---' + LineEnding +
            'doc1' + LineEnding +
            '---' + LineEnding +
            'doc2';
    docs := ys.LoadAll(yaml);
    try
      for i := 0 to docs.Count - 1 do
        WriteLn(docs.Items[i].AsString);
      // doc1
      // doc2
    finally
      docs.Free;
    end;
  finally
    ys.Free;
  end;
end.
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

## Dumping YAML

Use `dump()` to emit one YAML document from JSON-compatible native values, and
`dump_all()` / `dumpAll()` to emit a YAML stream:

=== "Python"

    ```python
    ys.dump({'name': 'YAMLStar', 'items': ['load', 'dump']})
    ys.dump_all(['doc1', {'doc': 2}])
    ```

=== "Node.js"

    ```javascript
    ys.dump({name: 'YAMLStar', items: ['load', 'dump']});
    ys.dumpAll(['doc1', {doc: 2}]);
    ```

=== "Clojure"

    ```clojure
    (yaml/dump {"name" "YAMLStar" "items" ["load" "dump"]})
    (yaml/dump-all ["doc1" {"doc" 2}])
    ```

Dump currently targets JSON-compatible data: maps with string keys, lists,
strings, numbers, booleans, and null values.

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
