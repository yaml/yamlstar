# Plugin System

The YAMLStar plugin system extends YAML processing with swappable
components while keeping the same API and results across all language
bindings.

## Overview

Plugins can customize internal processing during loading and dumping.
The first supported plugin type is the **parser plugin**, which replaces
the entire YAML parser.

Every load operation accepts an options structure that selects plugins
and configures them.
The options are the same in every binding; only the syntax is idiomatic
to each language.

## Available Parsers

| Name | Description |
|------|-------------|
| `reference` | The pure Clojure YAML 1.2 reference parser (default) |
| `snakeyaml` | The SnakeYAML Engine parser (used by YAMLScript) |

Both parsers produce identical results for conforming YAML documents.
SnakeYAML rejects some edge cases that the reference parser accepts
(tabs in certain positions, multiline flow mapping keys, and other
yaml-test-suite corner cases).

## Options Shape

Options are a nested mapping.
The `plugin` key holds a map of plugin type to plugin configuration.
For parser plugins, `use` names the parser and any sibling keys are
passed to it as configuration:

```yaml
plugin:
  parser:
    use: snakeyaml
```

## Using Parser Plugins

### Clojure

```clojure
(require '[yamlstar.core :as yaml]
         '[yamlstar.plugin.snakeyaml])

(yaml/load "key: value" {:plugin {:parser {:use "snakeyaml"}}})
```

### Python

```python
import yamlstar

ys = yamlstar.YAMLStar()

# Shorthand:
data = ys.load("key: value", parser='snakeyaml')

# Full options form:
data = ys.load("key: value",
               options={'plugin': {'parser': {'use': 'snakeyaml'}}})
```

### Go

```go
import "github.com/yaml/yamlstar/go"

data, err := yamlstar.Load("key: value",
    yamlstar.WithParser("snakeyaml"))
```

## Environment Override

The `YAMLSTAR_PARSER` environment variable changes the default parser
for operations that don't select one explicitly:

```bash
YAMLSTAR_PARSER=snakeyaml python my-program.py
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
- Example: `{"plugin": {"parser": {"use": "snakeyaml"}}}`
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
`:use` siblings merged over `:default-config`) and must return the
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
