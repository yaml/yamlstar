# Plugin System

The YAMLStar plugin system extends YAML processing with swappable
components while keeping the same API and results across all language
bindings.

## Overview

Plugins can customize internal processing during loading and dumping.
Parser plugins replace the YAML parser.
Event-source plugins produce the complete parser event stream and can be
distributed as shared libraries.

Every load operation accepts an options structure that selects plugins
and configures them.
The options are the same in every binding; only the syntax is idiomatic
to each language.

## Available Parsers

| Name | Description |
|------|-------------|
| `reference` | The pure Clojure YAML 1.2 reference parser (default) |
| `go-yaml` | The go-yaml parser (Glojure runtime only) |
| `snakeyaml` | The SnakeYAML Engine parser (JVM only) |

All parsers produce identical results for conforming YAML documents.
SnakeYAML rejects some edge cases that the reference parser accepts
(tabs in certain positions, multiline flow mapping keys, and other
yaml-test-suite corner cases).

Not every parser is available in every binding.
The released `yaml` CLI and the `libyamlstar` shared library are built
with Glojure and bundle the `reference` and `go-yaml` parsers.
The `snakeyaml` parser needs a JVM, so it is only available from the
Clojure and Java bindings.
The GraalVM native build that included it is no longer part of the
release artifacts.

## Options Shape

Options are a nested mapping.
The `plugin` key holds a map of plugin type to plugin configuration.
For parser plugins, `name` names the parser and any sibling keys are
passed to it as configuration:

```yaml
plugin:
  parser:
    name: snakeyaml
```

## Shared Event-Source Plugins

The native `yaml` CLIs and both native `libyamlstar` implementations can
load an event-source plugin from a Unix shared library.
The JVM library and the pure Go package do not load shared plugins.

Select the JSON comments plugin with either of these equivalent forms:

```bash
yaml --plugin=json-comments
yaml --plugin=json-comments=json-comments
```

Only one event-source plugin can be active for a load operation.
An event-source plugin supersedes the default parser.
It can coexist with an explicit `reference` parser selection, but a
different explicitly selected parser is a configuration error.

When `YAMLSTAR_LIBRARY_PATH` is set, YAMLStar searches only its
colon-separated directories, in order.
Empty entries are ignored.
Otherwise, YAMLStar searches for
`libyamlstar-plugin-json-comments.so` on Linux and FreeBSD, or the
corresponding `.dylib` on macOS, in these default locations:

1. Beside the hosting `libyamlstar` library.
2. `../lib` relative to the CLI executable.
3. `$HOME/.local/lib`.
4. `/usr/local/lib`.
5. `/usr/lib`.

Print the default path in colon-separated form with:

```bash
yaml --path
```

For example, this adds `foo` ahead of the default path:

```bash
YAMLSTAR_LIBRARY_PATH=foo:$(yaml --path) yaml file.yaml
```

The same override can be scoped to one invocation:

```bash
yaml --path=foo --plugin=json-comments file.yaml
yaml --path=foo:$(yaml --path) --plugin=json-comments file.yaml
```

The current working directory is never searched by default.

### Installing Missing Plugins

The native `yaml` command automatically installs a missing official
plugin before loading the input:

```bash
printf '%s\n' '{"a": true // comment}' |
  yaml --plugin=json-comments
```

Use `--no-plugin-install` when network access or filesystem changes are
not wanted.
An installed plugin is never checked for updates during loading.

Automatic installation maps plugin name `NAME` to the GitHub repository
`yamlstar/yamlstar-plugin-NAME` and selects its newest published release.
It supports Linux x64 and aarch64 and macOS x64 and arm64 when the release
contains the corresponding asset.
The installer requires `curl`, `tar`, and either `sha256sum` or `shasum`.
It verifies `SHA256SUMS` before installing the library atomically in the
first writable plugin path.
When `YAMLSTAR_LIBRARY_PATH` is set, only its directories are considered.
Otherwise, the default path printed by `yaml --path` is used.

The `libyamlstar` API does not install plugins unless the caller opts in:

```json
{
  "plugin": {
    "json-comments": {
      "name": "json-comments"
    }
  },
  "plugin-install": true
}
```

Python provides a convenience argument for the same option:

```python
opts = yamlstar.Options().plugin(yamlstar.json_comments())
ys = yamlstar.YAMLStar(opts, install_plugins=True)
```

The installer can also be run directly:

```bash
yamlstar-plugin install json-comments
```

Set `YAMLSTAR_PLUGIN_INSTALLER` to an alternate installer executable.
Third-party plugins continue to use manual installation or
`YAMLSTAR_LIBRARY_PATH`.

The version 1 shared-plugin ABI uses raw UTF-8 input and EDN output.
The plugin manifest declares its API, independent version, plugin kind,
required parser, and event format.
Returned buffers are owned by the plugin and must be released through its
exported free function.

```c
uint64_t yamlstar_plugin_v1_abi(void);
int32_t yamlstar_plugin_v1_manifest(
    uint8_t **output, size_t *output_length);
int32_t yamlstar_plugin_v1_parse(
    const uint8_t *input, size_t input_length,
    const uint8_t *options_edn, size_t options_length,
    uint8_t **output, size_t *output_length);
void yamlstar_plugin_v1_free(uint8_t *output);
```

Status `0` is success, `1` is a plugin parse or configuration error, and
`2` is an ABI or internal failure.

## Using Parser Plugins

### Clojure

```clojure
(require '[yamlstar.core :as yaml]
         '[yamlstar.options :as opts]
         '[yamlstar.plugin.parser :as parser])

(def options
  (-> (opts/options)
      (opts/plugin (parser/name "snakeyaml"))))

(yaml/load "key: value" options)
```

### Python

```python
import yamlstar

opts = yamlstar.Options().plugin(yamlstar.parser('go-yaml'))
ys = yamlstar.YAMLStar(opts)
data = ys.load("key: value")

# Full options form:
ys = yamlstar.YAMLStar({'plugin': {'parser': {'name': 'go-yaml'}}})
data = ys.load("key: value")
```

### Go

```go
import "github.com/yaml/yamlstar/go"

data, err := yamlstar.Load("key: value",
    yamlstar.WithPlugin(yamlstar.Parser("reference")))
```

## Environment Override

The `YAMLSTAR_PARSER` environment variable changes the default parser
for operations that don't select one explicitly:

```bash
YAMLSTAR_PARSER=reference python my-program.py
```

This is useful for testing a whole program or test suite against a
different parser without code changes.

## FFI Wire Format

Language bindings pass options to `libyamlstar` as a JSON string.
The load and dump C entry points take the options JSON as their final
argument:

```c
char *yamlstar_load(long long isolate, const char *yaml,
                    const char *opts_json);
```

- Pass `"{}"` (or NULL) when no options are set.
- Example: `{"plugin": {"parser": {"name": "snakeyaml"}}}`
- Keys are normalized from `snake_case` to `kebab-case`; values are
  never rewritten.

## Writing a Parser Plugin

A parser plugin is a Clojure map registered with
`yamlstar.plugin/register-parser!`:

```clojure
(require '[yamlstar.plugin :as plugin])

(plugin/register-parser!
  {:name "my-parser"
   :parse (fn [yaml-str config] ...)   ; -> event map sequence
   :default-config {}})                 ; optional
```

The `:parse` function receives the YAML string and a config map (the
`:name` siblings merged over `:default-config`) and must return the
standard YAMLStar event stream: an ordered sequence of maps using this
vocabulary:

| Event | Keys |
|-------|------|
| `{:event "stream_start"}` | |
| `{:event "stream_end"}` | |
| `{:event "document_start"}` | `:explicit` (true), `:version` ("1.2") |
| `{:event "document_end"}` | `:explicit` (true) |
| `{:event "mapping_start"}` | `:flow` (bool, always), `:anchor`, `:tag` |
| `{:event "mapping_end"}` | |
| `{:event "sequence_start"}` | `:flow` (bool, always), `:anchor`, `:tag` |
| `{:event "sequence_end"}` | |
| `{:event "scalar"}` | `:value`, `:style`, `:anchor`, `:tag` |
| `{:event "alias"}` | `:name` |

Notes:

- `:style` is one of `"single"`, `"double"`, `"literal"`, `"folded"`
  and is omitted for plain scalars.
- `:anchor` and `:tag` are included only when present.
- Tags are fully resolved URIs (`!!int` becomes
  `tag:yaml.org,2002:int`); local tags keep their `!` prefix.
- A namespace named `yamlstar.plugin.<name>` that self-registers on
  load is resolved automatically when `<name>` is first used.

## Roadmap

- A `rapidyaml` parser plugin (C++ parser, working on a branch) will
  register through this same API.
- Dumper-side plugins (emitter, representer) are planned.
- Additional load options (duplicate key handling, merge keys) will
  join `plugin` at the top level of the options mapping.
