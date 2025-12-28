# YAMLStar

A YAML framwork for all programming languages

## Vision

YAMLStar aims to be the best YAML loader available, with these key features:

- **YAML 1.2 Spec Compliance**: 100% compliant with the YAML 1.2 specification
- **Pure Clojure Parser**: No dependencies on SnakeYAML or other external parsers
- **Cross-Language Consistency**: Identical behavior in 15+ languages via shared library
- **Highly Configurable**: Plugin system for extensibility (coming in Phase 3)
- **Lightweight**: Minimal dependencies, fast startup, small binaries

## Project Status

🚧 **Early Development** - Phase 1 in progress

### Roadmap

- **Phase 1: Minimal Viable Loader** (Current)
  - Pure YAML 1.2 loading
  - Event-based parser integration
  - Clojure data structure output
  - GraalVM native-image shared library
  - Language bindings (Python, Ruby, Node.js, Go, etc.)

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

## Current Status

✅ **Phase 1A Complete** - Core implementation finished!

- Pure Clojure YAML 1.2 parser integrated (100% spec compliant)
- Event-to-node composer implemented
- Node-to-data resolver implemented
- Complete test suite (23 tests covering all major features)
- Zero external dependencies (except Clojure + data.json)

**Ready for testing!** Install Leiningen and run `lein test` in the `core/` directory.

## Quick Start

### Install Clojure Tools

```bash
# Install Leiningen
curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
chmod +x /usr/local/bin/lein
```

### Run Tests

```bash
cd core
lein test
```

### Use in REPL

```bash
cd core
lein repl
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

## Comparison to YAMLScript

YAMLStar is derived from YAMLScript but with a different focus:

| Feature | YAMLScript | YAMLStar |
|---------|-----------|----------|
| **Purpose** | YAML + scripting language | Pure YAML loader |
| **Runtime** | Includes SCI interpreter | No runtime evaluation |
| **Pipeline** | 7 stages (parser → runtime) | 3 stages (parser → data) |
| **Dependencies** | Heavy (Babashka, SCI, etc.) | Minimal (Clojure only) |
| **Extensibility** | Built-in scripting | Plugin system (Phase 3) |

## License

MIT License - See LICENSE file

## Credits

Created by Ingy döt Net, inventor of YAML and YAMLScript.

Built on the YAML Reference Parser (pure Clojure implementation).
