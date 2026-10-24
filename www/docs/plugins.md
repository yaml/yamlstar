# Plugin system

YAMLStar plugin APIs identify replaceable processing roles.
Each API can have several named implementations.
The current APIs and implementations are:

| API | Implementation | Availability |
|---|---|---|
| `yaml-parser` | `reference` | JVM and native |
| `yaml-parser` | `go-yaml` | Native runtime |
| `yaml-parser` | `snakeyaml` | JVM runtime |
| `yaml-parser` | `toml` | Native shared library |
| `yaml-parser` | `rapidyaml` | Native shared library |
| `yaml-emitter` | `reference` | JVM and native |
| `yaml-emitter` | `go-yaml` | Native runtime |
| `yaml-emitter` | `snakeyaml` | JVM runtime |
| `tab-indent` | built in | Native runtime with go-yaml |
| `yaml-parser` | `json-comments` | JVM and native |
| `alias-data` | `alias-data` | JVM and native, built in |

The JSON-comments parser extends the reference grammar with C-style comments.
Select it as the parser implementation for that load.

## Configuration

Options use a `plugin` mapping keyed by plugin API:

```yaml
plugin:
  yaml-parser: reference@v0.2.6
  yaml-emitter: reference
  alias-data:
    data:
      defaults: {color: blue}
    env: APP_*
```

An implementation can also use a mapping:

```yaml
plugin:
  yaml-parser:
    name: reference
    version: v0.2.6
  yaml-emitter:
    name: reference
```

`true` selects the API's default implementation.
`false` disables that API.
A mapping may use `disable: true` to retain settings without selecting the
plugin.
Null is invalid.
Versions with and without the leading `v` are accepted.
Documentation and Git tags use the `v` prefix.

## Command line

`--plugin` accepts only plugin selector syntax.
It does not accept a file name or an inline YAML mapping.
Selectors have these forms:

```text
API
API@VERSION
API=IMPLEMENTATION
API=IMPLEMENTATION@VERSION
```

Several selectors can be comma-separated:

```bash
yaml --plugin=yaml-parser=json-comments@v0.1.0,yaml-emitter=reference file.yaml
```

The short `yaml-parser` and `yaml-emitter` selectors choose the runtime
defaults.
Use `--config=FILE` for a YAML options file.
The former `--parser` flag has been removed.
The old `parser` plugin API is rejected; use `yaml-parser` instead.

The old `yaml-parser` API name is rejected.

The TOML implementation parses TOML into the same YAML event stream used by
the built-in YAML parsers:

```bash
yaml --plugin=yaml-parser=toml settings.toml
```

## Alias data

The built-in `alias-data` implementation controls anchor storage and alias
resolution during loading.
With no Alias-Data selection, anchors follow standard YAML document scope.

```yaml
plugin:
  alias-data:
    data:
      defaults:
        color: blue
        size: 3
    file: aliases.yaml
    env: APP_*
    stream: true
```

`data` is a mapping from anchor names to arbitrary YAML values.
`file` names one YAML document whose root must be a mapping.
Relative paths use the current working directory.

`env` accepts `true` for the complete environment, a sequence of exact names,
or a string pattern.
Only `*` is a wildcard.
Environment values are strings.
Missing exact names and selected invalid names are errors.

Names from every source must match `[A-Za-z0-9_-]+`.
Files and environment variables are read once when the per-load policy is
created.

Aliases resolve in this order: current-document anchors, inline data, file
data, environment data, and anchors retained from previous documents.
Mappings supplied by any source work with the YAML merge key.

`stream: true` retains anchors for later documents in the same input stream.
The short selector and an explicit empty configuration both enable stream
mode:

```bash
yaml --plugin=alias-data stream.yaml
```

Alias-Data is an in-process policy API, not a native shared-library plugin.
Both the JVM and generated native runtime perform the same lifecycle actions:
begin stream, begin document, define anchor, resolve alias, end document, and
end stream.

## YAML parser and emitter implementations

The `reference` implementation uses release `v0.2.6` of the YAML reference
parser.
The canonical Clojure source remains in `yaml/yaml-reference-parser-clj`.
JVM builds use its Clojars artifact directly.
Glojure builds use the generated Go module
`github.com/yamlstar/yamlstar-plugin-parser-reference`.

The released native CLI uses the built-in `go-yaml` implementation for both
parsing and emitting.
The `snakeyaml` implementations are available to JVM applications.

All parser implementations return YAMLStar's standard event maps.
The `json-comments` implementation uses the same event interface.
All emitter implementations consume that same event stream.

## Tab indentation

The built-in `tab-indent` plugin enables structural tab indentation on load
and dump.
It currently requires the `go-yaml` parser for tab-aware loading and the
`go-yaml` emitter for tab-aware dumping.
The native CLI selects those implementations by default.

The short selector uses the defaults:

```bash
yaml --plugin=tab-indent file.yaml
```

The full configuration is:

```yaml
plugin:
  tab-indent:
    mode: auto
    load: auto
    dump: tabs
    auto: document
```

`mode` accepts `tabs` or `auto`.
`load` accepts `tabs`, `spaces`, or `auto`.
`dump` accepts `tabs` or `spaces`.
`auto` accepts `document` or `stream` and controls the scope used when
detecting input indentation.

The go-yaml emitter keeps block sequences compact under mapping keys:

```yaml
foo:
- bar
```

## JSON-comments parser

Select the grammar extension with:

```bash
yaml --plugin=yaml-parser=json-comments file.yaml
```

```yaml
plugin:
  yaml-parser: json-comments@v0.1.0
```

The implementation parses the original source using per-parse reference
production replacements.
It recognizes `//` line comments and non-nesting `/* */` block comments at
separation positions, including JSON punctuation and adjacent JSON literals.
Ordinary unquoted text still needs whitespace before a comment:
`http://example.com`, `foo/*bar*/`, `True//text`, and `01//text` remain text.
Quoted and block-scalar content retain comment markers.
An inline block comment contributes no scalar text, so
`foo /* comment */ bar` becomes `foo  bar`.
Adjacent `#` comments are accepted after quoted scalars, commas, `]`, and `}`.
Comments are discarded and are not round-tripped.

JVM builds use `org.yamlstar/yamlstar-plugin-parser-json-comments` version
`0.1.0` with reference parser `0.2.6`.
Native builds link the generated Go module
`github.com/yamlstar/yamlstar-plugin-parser-json-comments`.
No separate shared library or Python plugin wheel is needed.

```python
opts = yamlstar.Options().plugin(yamlstar.parser('json-comments'))
data = yamlstar.YAMLStar(opts).load('a: true// comment')
```

The old `json-comments` API and `sanitizer` implementation have been removed.
Use `yaml-parser=json-comments` in selectors and `plugin.yaml-parser` in
configuration.
This selects one parser and cannot be combined with another parser.

## Native shared plugins

Shared parser implementations such as TOML use ABI version 2.
Their manifests declare `:api "yaml-parser"`, `:kind "event-source"`, and the
implementation's name and version.
Older manifests declaring `:api "parser"` are rejected.
Rebuild or replace existing libraries when upgrading to this API name.
The transform input is UTF-8 source and the output is an EDN event vector.

`YAMLSTAR_LIBRARY_PATH` overrides the default library search directories.
Print the defaults with `yaml --path`.
The CLI can install missing official shared plugins unless `--no-install`
is supplied; existing libraries are not updated automatically.
For example, `yamlstar-plugin install yaml-parser=toml` installs the TOML parser.
JSON-comments uses the linked parser implementation and does not use this
shared-library installation path.

## Writing a YAML parser implementation

A Clojure parser implementation registers a map with
`yamlstar.plugin/register-parser!`:

```clojure
(plugin/register-parser!
 {:name "my-parser"
  :version "1.0.0"
  :parse (fn [yaml-str config] ...)
  :default-config {}})
```

The parse function returns an ordered sequence of event maps:

| Event | Optional keys |
|---|---|
| `stream_start`, `stream_end` | none |
| `document_start` | `explicit`, `version` |
| `document_end` | `explicit` |
| `mapping_start`, `sequence_start` | `flow`, `anchor`, `tag` |
| `mapping_end`, `sequence_end` | none |
| `scalar` | `value`, `style`, `anchor`, `tag` |
| `alias` | `name` |

The `flow` key is always present on collection start events.
Plain scalars omit `style`.
Anchor and tag keys are present only when supplied by the parser.

## Writing a YAML emitter implementation

A Clojure emitter implementation registers a map with
`yamlstar.plugin/register-yaml-emitter!`:

```clojure
(plugin/register-yaml-emitter!
 {:name "my-emitter"
  :version "1.0.0"
  :emit (fn [events multi? config] ...)
  :default-config {}})
```

The emit function receives the standard event stream, a boolean indicating
multi-document output, and the merged configuration map.
It returns the emitted YAML string.

## RapidYAML parser

The optional `yamlstar-plugin-parser-rapidyaml` implements the ABI v2
parser API, using rapidyaml v0.16.0.
Select it with `yaml --plugin=yaml-parser=rapidyaml`.
Use `--no-install` to disable automatic installation.
The plugin has documented tab-separation limitations and does not fall back
to another parser.
The earlier ABI v1 release 0.1.0 is incompatible with this host.

For a local plugin checkout, build its library once and select its directory:

```bash
make -C repos/yamlstar-plugin-parser-rapidyaml build
export YAMLSTAR_LIBRARY_PATH=$PWD/repos/yamlstar-plugin-parser-rapidyaml/lib
yaml --plugin=yaml-parser=rapidyaml --no-install -j < input.yaml
go-yaml --plugin=yaml-parser=rapidyaml -j < input.yaml
```

The go-yaml executable must already include the RapidYAML Go adapter through
`PLUGIN-PACKAGE`.
Both commands load the same shared library.
The built-in go-yaml parser remains compiled into both native CLIs and requires
no parser shared library.

## Plugin demos

Run `./util/demo-plugins` from a fresh clone to demonstrate YAMLStar plugins.
The script builds the native CLI once and downloads missing shared plugins.
Build output appears as it is produced.
No `repos/*` checkouts are needed.

```bash
./util/demo-plugins --list
./util/demo-plugins 3 5-7 9
./util/demo-plugins -i 10-
```

The sister `util/demo-plugins.yaml` catalog contains the descriptions, commands,
and build arguments.
Commands invoke `yaml` directly, using the executable selected by the script.
YAMLScript reads the catalog.
If `ys` is missing from `PATH`, the script installs it using in-1:

```bash
source <(curl -s https://in-1.cc) ys
```

Non-interactive output uses `less -FRX`.
Interactive mode lets you edit commands with readline.
Clear the line and press Enter to recall the previous command, or enter a demo
number to load that command without consuming the pending demo.
Set `YAMLSTAR_BIN` to use an existing executable without building it.
