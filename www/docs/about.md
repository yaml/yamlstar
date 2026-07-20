# About YAMLStar

## Vision

YAMLStar aims to be the best YAML load/dump framework available, with these key
features:

- **YAML 1.2 Spec Compliance**: 100% compliant with the YAML 1.2 specification
- **Pure Clojure Parser**: No dependencies on SnakeYAML or other external parsers
- **Dump Stack**: Convert JSON-compatible native values back to readable YAML
- **Cross-Language Consistency**: Identical behavior in 32 languages via shared
  library
- **Highly Configurable**: Plugin system for extensibility (coming in Phase 3)
- **Lightweight**: Minimal dependencies, fast startup, small binaries

## What Makes YAMLStar Different

YAMLStar is designed from the ground up to provide a consistent YAML loading and
dumping experience across all programming languages.
Unlike traditional YAML libraries that are implemented separately for each
language, YAMLStar uses a single core implementation compiled to a native
shared library.

### Key Advantages

**Consistency Across Languages**
: Every language binding uses the same underlying YAML parser, ensuring
  identical behavior everywhere.
  No more "it works in Python but not in Go" issues.

**100% Spec Compliance**
: Built on the pure Clojure YAML Reference Parser, YAMLStar implements the full
  YAML 1.2 specification without shortcuts or omissions.

**Zero External Dependencies**
: The core library depends only on Clojure itself.
  No SnakeYAML, libyaml, or other external YAML parsers.

**Lightweight Design**
: YAMLStar implements only what's needed for YAML loading and dumping.
  The load and dump stacks are much lighter than YAMLScript's runtime pipeline.

## Architecture

YAMLStar uses a clean 4-stage load stack to convert YAML text into native data
structures:

<div class="architecture-diagram">
<pre>
YAML String (input)
    ↓
┌─────────────────────┐
│  Parser             │  yamlstar.parser
│  (Pure Clojure)     │  - PEG grammar engine
│  YAML 1.2 spec      │  - 211 production rules
└─────────────────────┘  - Event emission
    ↓ Event Stream
    [{:event "mapping_start"}
     {:event "scalar" :value "key"}
     {:event "scalar" :value "value"}
     {:event "mapping_end"}]
    ↓
┌─────────────────────┐
│  Composer           │  yamlstar.composer
│  (Stack-based)      │  - Event→Node tree
└─────────────────────┘  - Anchor/tag tracking
    ↓ Node Tree
    {:kind :mapping
     :value [[{:kind :scalar :value "key"}
              {:kind :scalar :value "value"}]]}
    ↓
┌─────────────────────┐
│  Resolver           │  yamlstar.resolver
│  (Tag resolution)   │  - Tag inference (!!str, !!int, etc.)
└─────────────────────┘  - Alias resolution
    ↓ Resolved Nodes
    {:kind :mapping :tag "!!map"
     :value [[{:kind :scalar :tag "!!str" :value "key"}
              {:kind :scalar :tag "!!str" :value "value"}]]}
    ↓
┌─────────────────────┐
│  Constructor        │  yamlstar.constructor
│  (Data conversion)  │  - Node→Data
└─────────────────────┘  - Type coercion
    ↓
Native Data: {"key" "value"}
</pre>
</div>

Dumping uses the reverse stack to convert native data back into YAML text:

<div class="architecture-diagram">
<pre>
Native Data: {"key" ["value"]}
    ↓
┌─────────────────────┐
│  Representer        │  yamlstar.representer
│  (Data conversion)  │  - Data→Node tree
└─────────────────────┘
    ↓ Node Tree
┌─────────────────────┐
│  Desolver           │  yamlstar.desolver
│  (Presentation)     │  - Minimal tags
└─────────────────────┘  - Scalar style hints
    ↓ Desolved Nodes
┌─────────────────────┐
│  Serializer         │  yamlstar.serializer
│  (Event stream)     │  - Node→Event stream
└─────────────────────┘
    ↓ Events
┌─────────────────────┐
│  Emitter            │  yamlstar.emitter
│  (YAML output)      │  - Event→YAML text
└─────────────────────┘
    ↓
YAML String (output)
</pre>
</div>

### Pipeline Stages

1. **Parser**: Converts YAML text into a stream of events using a PEG
   (Parsing Expression Grammar) implementation of the YAML 1.2 spec.

2. **Composer**: Converts the flat event stream into a hierarchical node tree,
   tracking anchors and tags along the way.

3. **Resolver**: Infers YAML types for untagged nodes using the YAML 1.2 Core
   Schema rules (null, bool, int, float, str).
   Also resolves anchor references to their aliased nodes.

4. **Constructor**: Converts resolved nodes into native data structures for the
   target language (maps, lists, strings, numbers, etc.).

5. **Representer**: Converts JSON-compatible native values into YAMLStar node
   trees for dumping.

6. **Desolver**: Chooses minimal tags and scalar style hints for readable YAML.

7. **Serializer**: Converts node trees into YAML events.

8. **Emitter**: Emits valid YAML text from events, preserving requested
   anchors, tags, aliases, and legal scalar styles.

## Comparison to YAMLScript

YAMLStar is derived from YAMLScript but with a different focus:

| Feature | YAMLScript | YAMLStar |
|---------|-----------|----------|
| **Purpose** | YAML + scripting language | Pure YAML load/dump framework |
| **Runtime** | Includes SCI interpreter | No runtime evaluation |
| **Pipeline** | 7 stages (parser → runtime) | load and dump stacks |
| **Dependencies** | Heavy (Babashka, SCI, etc.) | Minimal (Clojure only) |
| **Extensibility** | Built-in scripting | Plugin system (Phase 3) |
| **Use Case** | Dynamic config, scripting | Static config loading and YAML output |

YAMLScript is excellent for configurations that need dynamic behavior, template
expansion, or computation.
YAMLStar is ideal when you need lightweight, reliable YAML loading and dumping
that behaves identically across all your applications.

## Technical Details

### YAML 1.2 Core Schema

YAMLStar implements the YAML 1.2 Core Schema for type resolution:

- **null**: `null`, `Null`, `NULL`, `~`
- **bool**: `true`, `True`, `TRUE`, `false`, `False`, `FALSE`
- **int**: `[-+]?[0-9]+` (base 10 only)
- **float**: Decimal floats including `.inf`, `-.inf`, `.nan`
- **str**: Everything else (default type)

Explicit tags like `!!str`, `!!int`, `!!float`, `!!bool`, `!!null` override
type inference.

### Event Processing

The parser emits a finite sequence of events:

- `stream_start` / `stream_end`
- `document_start` / `document_end`
- `mapping_start` / `mapping_end`
- `sequence_start` / `sequence_end`
- `scalar` (with value, style, anchor, tag)
- `alias` (reference to anchored node)

The composer uses a stack-based algorithm to build the node tree from these
events.

### Multi-Document Support

YAMLStar supports YAML streams with multiple documents separated by `---`:

- `load(yaml)` - Returns the first document (or only document)
- `load_all(yaml)` - Returns a list of all documents
- `dump(value)` - Emits one YAML document from JSON-compatible data
- `dump_all(values)` - Emits a YAML stream from multiple values

## Project Statistics

- **Total Lines of Code**: ~5,700 (including grammar)
- **Core Implementation**: ~500 lines (excluding parser)
- **Grammar Rules**: 211 (YAML 1.2 spec)
- **Test Coverage**: 23+ comprehensive tests
- **Dependencies**: 2 (Clojure + data.json for FFI)
- **Language Bindings**: 32 (Ada, Clojure, Crystal, C#, D, Dart, Delphi,
  Dyalog APL, Elixir, Erlang, F#, Fortran, Go, Haskell, Java, Julia, Kotlin,
  Lua, MoonBit, Nim,
  Node.js, Perl, PHP, PowerShell, Python, R, Raku, Ruby, Rust, Scala, Swift,
  Zig)

## Credits

**Created by**: Ingy döt Net

- Inventor of YAML
- Creator of YAMLScript
- Maintainer of the YAML Reference Parser

**Built on**:

- **YAML Specification** - Created by Ingy döt Net
- **YAMLScript** - Clojure-based YAML + scripting language
- **yaml-reference-parser** - Pure Clojure YAML 1.2 parser
- **Clojure** - Rich Hickey's elegant functional language

**License**: MIT License - See LICENSE file

## Get Involved

YAMLStar is open source and welcomes contributions:

- **GitHub**: [github.com/yaml/yamlstar](https://github.com/yaml/yamlstar)
- **Issues**: Report bugs or request features
- **Pull Requests**: Contribute code or documentation
- **Discussions**: Share ideas and ask questions

Join us in making YAML great again! 🌟
