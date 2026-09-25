# Plugin system

YAMLStar plugin APIs identify replaceable processing roles.
Each API can have several named implementations.
The current APIs and implementations are:

| API | Implementation | Availability |
|---|---|---|
| `yaml-parser` | `reference` | JVM and native |
| `yaml-parser` | `go-yaml` | Native runtime |
| `yaml-parser` | `snakeyaml` | JVM runtime |
| `yaml-emitter` | `reference` | JVM and native |
| `yaml-emitter` | `go-yaml` | Native runtime |
| `yaml-emitter` | `snakeyaml` | JVM runtime |
| `tab-indent` | built in | Native runtime with go-yaml |
| `json-comments` | `sanitizer` | JVM artifact or native shared library |

The JSON-comments sanitizer transforms source text before the selected parser
runs.
It can therefore be combined with any available parser.

## Configuration

Options use a `plugin` mapping keyed by plugin API:

```yaml
plugin:
  yaml-parser: reference@v0.2.5
  yaml-emitter: reference
  json-comments: sanitizer@v0.1.9
```

An implementation can also use a mapping:

```yaml
plugin:
  yaml-parser:
    name: reference
    version: v0.2.5
  yaml-emitter:
    name: reference
  json-comments:
    name: sanitizer
    version: v0.1.9
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
yaml --plugin=yaml-parser=reference@v0.2.5,json-comments file.yaml
```

The short `json-comments` selector chooses `sanitizer`.
The short `yaml-parser` and `yaml-emitter` selectors choose the runtime
defaults.
Use `--config=FILE` for a YAML options file.
The former `--parser` flag has been removed.

The old `parser` API name is rejected with a message directing callers to
`yaml-parser`.

## YAML parser and emitter implementations

The `reference` implementation uses release `v0.2.5` of the YAML reference
parser.
The canonical Clojure source remains in `yaml/yaml-reference-parser-clj`.
JVM builds use its Clojars artifact directly.
Glojure builds use the generated Go module
`github.com/yamlstar/yamlstar-plugin-parser-reference`.

The released native CLI uses the built-in `go-yaml` implementation for both
parsing and emitting.
The `snakeyaml` implementations are available to JVM applications.

All parser implementations return YAMLStar's standard event maps.
Applications can select another parser without changing JSON-comments
configuration.
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

## JSON-comments implementation

The `sanitizer` implementation removes recognized `//` and `/* */` comments
from UTF-8 input while preserving line endings and non-comment text.
The selected parser receives the transformed source.
Comment markers inside quoted scalars, block scalars, and URLs remain text.
See the plugin's syntax documentation for the exact recognition rules.

JVM users add the independent Clojars artifact:

```clojure
[org.yamlstar/yamlstar-plugin-json-comments "0.1.9"]
```

YAMLStar resolves it from the classpath only when `json-comments` is selected.
It needs no native library.
The standard JVM CLI includes this artifact.

Native hosts load the implementation from
`libyamlstar-plugin-json-comments.so` on Linux and FreeBSD or the matching
`.dylib` on macOS.
The implementation name remains `sanitizer`; the native distribution and
library artifact retain the `json-comments` repository name.

## Native plugin search and installation

When `YAMLSTAR_LIBRARY_PATH` is set, YAMLStar searches only its
colon-separated directories.
Empty entries are ignored.
Otherwise it searches:

1. Beside the hosting `libyamlstar` library.
2. `../lib` relative to the CLI executable.
3. `$HOME/.local/lib`.
4. `/usr/local/lib`.
5. `/usr/lib`.

Print the default path with:

```bash
yaml --path
```

Add a directory for one invocation with:

```bash
YAMLSTAR_LIBRARY_PATH=foo:$(yaml --path) \
  yaml --plugin=json-comments file.yaml
```

The native CLI can install a missing official plugin automatically.
Use `--no-plugin-install` to disable that behavior.
The installer downloads the newest release of
`yamlstar/yamlstar-plugin-json-comments`, verifies `SHA256SUMS`, and installs
the library atomically in the first writable search directory.
An existing library is not updated during loading.

The installer can also be run directly:

```bash
yamlstar-plugin install json-comments
```

Python users can install the native distribution through its wheel:

```bash
pip install yamlstar-plugin-json-comments
```

```python
opts = yamlstar.Options().plugin(yamlstar.json_comments())
ys = yamlstar.YAMLStar(opts)
data = ys.load('''{
  "a": true // comment
}''')
```

The Python helper produces this common configuration:

```json
{"plugin": {"json-comments": {"name": "sanitizer"}}}
```

The binding maps the logical `sanitizer` implementation to the
`json-comments` package entry point and native library artifact.

## Shared text-transform ABI

Native JSON-comments plugins use shared ABI version 2.
The manifest is EDN and declares `:api "json-comments"`,
`:name "sanitizer"`, `:kind "text-transform"`, and its release version.
The transform input and successful output are raw UTF-8 bytes.
Only the small manifest and options value use EDN.

```c
uint64_t yamlstar_plugin_v2_abi(void);
int32_t yamlstar_plugin_v2_manifest(
    uint8_t **output, size_t *output_length);
int32_t yamlstar_plugin_v2_transform(
    const uint8_t *input, size_t input_length,
    const uint8_t *options_edn, size_t options_length,
    uint8_t **output, size_t *output_length);
void yamlstar_plugin_v2_free(uint8_t *output);
```

Status `0` is success.
Status `1` returns a plugin error message.
Status `2` reports an ABI or internal host failure.
The host releases every returned buffer through the plugin's free function.

## Writing a YAML parser implementation

A Clojure parser implementation registers a map with
`yamlstar.plugin/register-yaml-parser!`:

```clojure
(plugin/register-yaml-parser!
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
