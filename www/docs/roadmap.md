# Roadmap

YAMLStar is developed in phases, each building on the previous work.
Here's what's complete, what's in progress, and what's coming next.

## Phase 1: Minimal Viable Loader ✅ Complete

**Status**: Released v0.1.2

Phase 1 delivers a production-ready YAML 1.2 loader with multi-language
support.

### Completed Features

- ✅ Pure Clojure YAML 1.2 parser integration
- ✅ Event-based parsing with 211 grammar rules
- ✅ 4-stage pipeline (Parser → Composer → Resolver → Constructor)
- ✅ Complete YAML 1.2 Core Schema support
- ✅ Anchors and aliases
- ✅ Explicit tags (!!str, !!int, !!float, !!bool, !!null)
- ✅ Multi-document streams
- ✅ Comprehensive test suite (23+ tests)
- ✅ GraalVM native-image shared library
- ✅ 10 language bindings:
    - Clojure
    - C#
    - Delphi (Pascal)
    - Fortran
    - Go
    - Java
    - Node.js
    - Perl
    - Python
    - Rust

### Architecture

```
YAML Input
    ↓
Parser (pure Clojure, YAML 1.2)
    ↓
Composer (events → node tree)
    ↓
Resolver (type inference, alias resolution)
    ↓
Constructor (nodes → native data)
    ↓
Output (maps, vectors, scalars)
```

### Key Achievements

- **Zero Dependencies**: Only Clojure + data.json (for FFI)
- **100% YAML 1.2 Compliant**: Via reference parser
- **Cross-Language Consistency**: Same behavior everywhere
- **Lightweight**: ~80% lighter than YAMLScript's pipeline
- **Well-Tested**: Comprehensive test coverage
- **Production Ready**: Stable API, semantic versioning

## Phase 2: Glojure Migration 🚧 Planned

**Target**: Q2 2026

Phase 2 will port YAMLStar from Clojure/GraalVM to Glojure (Clojure on Go),
eliminating the GraalVM dependency and improving cross-platform support.

### Goals

- Port core implementation to Glojure
- AOT compilation to Go instead of JVM bytecode
- Generate Go shared library directly
- Eliminate GraalVM native-image dependency
- Improve build times and binary size
- Better cross-platform support (especially Windows)

### Benefits

**Simpler Build Process**
: No more GraalVM installation or native-image compilation.
  Just Go compiler required.

**Smaller Binaries**
: Go produces smaller binaries than GraalVM native-image.

**Faster Builds**
: Go compilation is significantly faster than native-image.

**Better Windows Support**
: Go has excellent Windows support.
  No more native-image quirks.

**Easier Contribution**
: Lower barrier to entry for contributors.
  Standard Go toolchain.

### Migration Path

1. **Prototype**: Port parser to Glojure, validate correctness
2. **Core**: Migrate composer, resolver, constructor
3. **FFI**: Update C API to use Go shared library
4. **Bindings**: Update all language bindings (transparent to users)
5. **Testing**: Ensure 100% compatibility with Phase 1
6. **Release**: Ship v0.2.0 with Glojure backend

### Backwards Compatibility

The API and behavior will remain identical.
Users won't need to change any code.
This is purely an implementation detail.

## Phase 3: Plugin System 🔮 Future

**Target**: Q4 2026

Phase 3 will add an extensible plugin system for custom tags, schema
validation, and expression evaluation.

### Planned Features

**Custom Tag Handlers**
: Register handlers for custom tags like `!include`, `!env`, `!base64`

**Schema Validation**
: Validate YAML against JSON Schema or custom validators

**Expression Evaluation**
: Optional YAMLScript-like features (opt-in)

**Directives**
: Support for `%TAG` and `%YAML` directives

**Stream Processing**
: Handle large YAML files incrementally

### Plugin Architecture

```yaml
# Example with custom tags
database:
  password: !env DATABASE_PASSWORD
  config: !include database.yaml
  schema: !validate
    type: object
    properties:
      host: { type: string }
      port: { type: integer }
```

Plugins will be language-specific modules that hook into the resolver stage:

```clojure
(yamlstar.core/register-tag
  "!env"
  (fn [node] (System/getenv (:value node))))
```

### Design Principles

- **Opt-in**: Core library remains minimal
- **Composable**: Mix and match plugins
- **Safe**: Plugins are sandboxed by default
- **Portable**: Plugin API works across all language bindings

## Phase 4: Advanced Features 🌟 Future

**Target**: 2027

Long-term enhancements for specialized use cases.

### Potential Features

- **YAML 1.3 Support**: When the spec is finalized
- **Streaming API**: Process large documents incrementally
- **Pretty Printing**: Format and emit YAML (not just load)
- **Source Maps**: Track line/column info through pipeline
- **Comments Preservation**: Retain comments when round-tripping
- **Performance Optimizations**: Profile and optimize hot paths
- **Additional Bindings**: C, C++, Swift, Kotlin, Zig, etc.

## Recent Releases

### v0.1.2 (January 27, 2026)

- Add release process and workflow
- Change npm publishing name to `yamlstar`
- Add Clojure library for direct use
- Add examples for Clojure, Python, Go, Java
- Refactor binding Makefiles for Windows support
- Update Maven group to `com.yaml`
- Remove SNAPSHOT from shared library name

### v0.1.0 (January 10, 2026)

- Initial release
- Pure Clojure YAML 1.2 parser implementation
- Support for scalars, mappings, sequences, anchors/aliases
- CLI tool for YAML to JSON conversion
- GraalVM native-image shared library
- Bindings for 9 languages
- Auto-installing build system via Makes

## Contributing

YAMLStar is open source and welcomes contributions!

### How to Help

**Report Bugs**
: Found an issue?
  Report it at [github.com/yaml/yamlstar/issues](https://github.com/yaml/yamlstar/issues)

**Request Features**
: Have an idea?
  Open a feature request on GitHub

**Write Code**
: Submit pull requests for bug fixes or new features

**Improve Docs**
: Help make the documentation better

**Add Bindings**
: Port YAMLStar to a new programming language

**Write Tests**
: Expand test coverage with edge cases

### Development Setup

```bash
# Clone the repository
git clone https://github.com/yaml/yamlstar.git
cd yamlstar

# Run core tests
cd core
make test

# Build shared library
cd ../libyamlstar
make build

# Test a binding
cd ../python
make test
```

The build system uses [Makes](https://github.com/makeplus/makes) to auto-install
all dependencies.

### Roadmap Discussions

Want to influence the roadmap?
Join the discussion on GitHub:

- [Phase 2: Glojure Migration](https://github.com/yaml/yamlstar/discussions)
- [Phase 3: Plugin System](https://github.com/yaml/yamlstar/discussions)
- [Feature Requests](https://github.com/yaml/yamlstar/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)

## Stay Updated

- **GitHub**: Watch the [repository](https://github.com/yaml/yamlstar) for
  updates
- **Releases**: Subscribe to [release notifications](https://github.com/yaml/yamlstar/releases)
- **Changelog**: Review the [Changes](https://github.com/yaml/yamlstar/blob/main/Changes)
  file

## Support

YAMLStar is open source (MIT License) and maintained by Ingy döt Net and
contributors.

For help:

- Read the [Getting Started](getting-started.md) guide
- Check the [Language Bindings](bindings.md) documentation
- Search [GitHub Issues](https://github.com/yaml/yamlstar/issues)
- Ask questions in [GitHub Discussions](https://github.com/yaml/yamlstar/discussions)

Let's make YAML great again! 🌟
