# YAMLStar

A YAML framework for all programming languages.

Documentation: <https://yamlstar.org>

## Install

Install the `yaml` CLI and the `libyamlstar` shared library:

```bash
curl -sSL https://yamlstar.org/install | bash
```

Install only the CLI or only the shared library:

```bash
curl -sSL https://yamlstar.org/install | BIN=1 bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```

Install to a custom prefix:

```bash
curl -sSL https://yamlstar.org/install | PREFIX=/opt/yamlstar bash
```

Homebrew is also supported on Linux x64 and macOS ARM64:

```bash
brew trust yaml/yamlstar
brew tap yaml/yamlstar

brew install yaml/yamlstar/yamlstar
brew install yaml/yamlstar/libyamlstar
```

## CLI

The `yaml` command loads YAML and prints compact JSON by default. Use `-Y` to
emit YAML through YAMLStar's dump stack:

```bash
printf 'a: 1\n' | yaml
yaml config.yaml
yaml -J config.yaml
yaml -Y config.yaml
yaml -s stream.yaml
yaml -Y -s stream.yaml
yaml -e 'a: 1' -o out.json
yaml -D parse config.yaml
yaml --version
```

Run `yaml --help` or see <https://yamlstar.org/cli/> for the full CLI
reference.

## Vision

YAMLStar aims to be the best YAML load/dump framework available, with these key
features:

- **YAML 1.2 Spec Compliance**: 100% compliant with the YAML 1.2 specification
- **Pure Clojure Parser**: No dependencies on SnakeYAML or other external parsers
- **Dump Stack**: Convert JSON-compatible native values back to YAML
- **Cross-Language Consistency**: Identical behavior in 15+ languages via shared library
- **Highly Configurable**: Plugin system for extensibility (coming in Phase 3)
- **Lightweight**: Minimal dependencies, fast startup, small binaries

## Project Status

✅ **Phase 1 Complete** - Ready for production use with 10 language bindings!

### Roadmap

- **Phase 1: Minimal Viable YAML Stack** ✅ Complete
  - YAML 1.2 loading
  - YAML dumping for JSON-compatible values
  - Event-based parser integration
  - Clojure data structure output
  - GraalVM native-image shared library
  - Language bindings (Clojure, C#, Fortran, Go, Java, Node.js, Perl, Python, Rust)

- **Phase 2: Glojure Migration**
  - Port to Glojure (Clojure on Go)
  - AOT compilation to Go
  - Eliminate GraalVM dependency
  - Improved cross-platform support

- **Phase 3: Plugin System**
  - Custom tag handlers (!include, !env, etc.)
  - Schema validation plugins
  - Expression evaluation (YAMLScript-like features)

## Architecture

```
YAML Input
    ↓
Parser (pure Clojure, YAML 1.2)
    ↓
Composer (events → node tree)
    ↓
Resolver (nodes → Clojure data)
    ↓
Output (maps, vectors, scalars)
```

Dumping follows the reverse stack:

```
Native Data
    ↓
Representer (data → node tree)
    ↓
Desolver (tags/styles for readable YAML)
    ↓
Serializer (nodes → events)
    ↓
Emitter (events → YAML text)
    ↓
YAML Output
```

## Current Status

✅ **Phase 1A Complete** - Core implementation finished!

- Pure Clojure YAML 1.2 parser integrated (100% spec compliant)
- Event-to-node composer implemented
- Node-to-data resolver implemented
- Complete test suite covering all major features
- Zero external dependencies (except Clojure + data.json)
- GraalVM native-image shared library (`libyamlstar.so`)
- **10 language bindings**: Clojure, C#, Delphi, Fortran, Go, Java, Node.js, Perl, Python, Rust

**Ready for testing!** Run `make test` in the `core/` directory, or try any of the language bindings.

## Quick Start

### Run Tests

The build system automatically installs all dependencies (Leiningen, GraalVM, etc.) on first run:

```bash
cd core
make test
```

### Use in REPL

```bash
cd core
make repl
```

```clojure
(require '[yamlstar.core :as yaml])

;; Load a YAML string
(yaml/load "key: value")
;=> {"key" "value"}

;; Load with type coercion
(yaml/load "num: 42\nbool: true\nnull: null")
;=> {"num" 42, "bool" true, "null" nil}

;; Load multiple documents
(yaml/load-all "---\ndoc1\n---\ndoc2")
;=> ["doc1" "doc2"]

;; Dump native values as YAML
(yaml/dump {"foo" [["bar"]]})
;=> "foo:\n- - bar\n"

;; Dump multiple documents
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

See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed development guide.

## Language Bindings

YAMLStar provides bindings for multiple programming languages via a shared GraalVM native-image library (`libyamlstar.so`). All bindings provide identical behavior and API.

For language bindings that use FFI, install the matching `libyamlstar`
release with the install script or Homebrew before loading YAML.

| Language | Directory | Test Command |
|----------|-----------|--------------|
| **Clojure** | [clojure/](clojure/) | `make test` (auto-installs lein) |
| **C#** | [csharp/](csharp/) | `make test` (auto-installs dotnet) |
| **Fortran** | [fortran/](fortran/) | `make test` (auto-installs gfortran, FPM) |
| **Go** | [go/](go/) | `make test` (auto-installs go) |
| **Java** | [java/](java/) | `make test` (auto-installs lein, Maven) |
| **Node.js** | [nodejs/](nodejs/) | `make test` (auto-installs node, npm) |
| **Perl** | [perl/](perl/) | `make test` (auto-installs perl, cpanm) |
| **Python** | [python/](python/) | `make test` (auto-installs python, pip) |
| **Rust** | [rust/](rust/) | `make test` (auto-installs cargo) |

### Quick Example (Node.js)

```javascript
const YAMLStar = require('yamlstar');
const ys = new YAMLStar();

const data = ys.load('key: value');
console.log(data);  // { key: 'value' }

const text = ys.dump({foo: [['bar']]});
console.log(text);
// foo:
// - - bar

ys.close();
```

### Quick Example (Python)

```python
from yamlstar import YAMLStar

ys = YAMLStar()
data = ys.load('key: value')
print(data)  # {'key': 'value'}

text = ys.dump({'foo': [['bar']]})
print(text)
# foo:
# - - bar

ys.close()
```

### Quick Example (Clojure)

```clojure
(require '[yamlstar.core :as yaml])

(yaml/load "key: value")
;=> {"key" "value"}

(yaml/dump {"foo" [["bar"]]})
;=> "foo:\n- - bar\n"
```

Each binding directory contains its own `ReadMe.md` with detailed installation and usage instructions.

## Comparison to YAMLScript

YAMLStar is derived from YAMLScript but with a different focus:

| Feature | YAMLScript | YAMLStar |
|---------|-----------|----------|
| **Purpose** | YAML + scripting language | YAML load/dump framework |
| **Runtime** | Includes SCI interpreter | No runtime evaluation |
| **Pipeline** | 7 stages (parser → runtime) | load and dump stacks |
| **Dependencies** | Heavy (Babashka, SCI, etc.) | Minimal (Clojure only) |
| **Extensibility** | Built-in scripting | Plugin system (Phase 3) |

## License

MIT License - See LICENSE file

## Credits

Created by Ingy döt Net, inventor of YAML and YAMLScript.

Built on the YAML Reference Parser (pure Clojure implementation).
