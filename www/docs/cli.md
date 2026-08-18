# YAMLStar CLI

The `yaml` command loads YAML and prints JSON by default. It can also expose
and consume the same event and node contracts as the go-yaml CLI.

## Examples

Read from standard input and print compact JSON:

```bash
printf 'name: YAMLStar\n' | yaml
```

Load a file:

```bash
yaml config.yaml
```

Pretty-print JSON:

```bash
yaml -J config.yaml
```

Emit normalized YAML:

```bash
yaml -y config.yaml
```

Inspect events or detailed nodes:

```bash
yaml -e config.yaml
yaml -N config.yaml
```

Chain representations forward through YAMLStar or between both CLIs:

```bash
yaml -e config.yaml | yaml -N | yaml -Y
go-yaml -e config.yaml | yaml -Y
yaml -N config.yaml | go-yaml -Y
```

The supported direction is YAML text to events to nodes to YAML text. The
contracts are YAML data, so `yq` transformations can be inserted between
commands:

```bash
yaml -e config.yaml |
  yq '(.[] | select(.event == "SCALAR" and .value == "old")).value = "new"' |
  yaml -Y
```

Input is detected from its schema. Use `-f event`, `-f node`, or `-f yaml`
when it is ambiguous. The short stage names `e`, `n`, and `y` are accepted.
Token input is deliberately unsupported for now: it remains the explicit
follow-up for `org.yamlstar/yaml-parser`.

Load every document in a YAML stream:

```bash
yaml -s stream.yaml
```

Emit every document in a YAML stream:

```bash
yaml -y -s stream.yaml
```

Evaluate a YAML string directly:

```bash
yaml --eval 'a: 1'
```

Write output to a file:

```bash
yaml config.yaml -o config.json
```

## Options

```text
Usage: yaml [options] [file]

Default: Read stdin as YAML, output compact JSON

Options:
  -f, --from STAGE         Force input stage: token, event, node, or yaml
      --file FILE          Input file (or use positional arg)
      --eval YAML          Evaluate YAML string
  -e, --event              Event output
  -E, --EVENT              Event output with metadata
  -n, --node               Node representation output
  -N, --NODE               Detailed node output
  -j, --json               Output compact JSON
  -J, --JSON               Output pretty JSON
  -y, --yaml               Normalized YAML output
  -Y, --YAML               YAML output preserving representation details
  -o, --output FILE        Output file
  -s, --stream             Output all documents
  -d, --debug              Debug all stages
  -D, --debug-stage STAGE  Debug specific stage: parse, compose, resolve,
                           construct
  -S, --stack-trace        Show full stack traces
  -v, --version            Print version
  -h, --help               Print help
```

`-y` emits normalized YAML using YAMLStar's dump stack:

```text
native value
  -> representer/represent
  -> desolver/desolve
  -> serializer/serialize
  -> emitter/emit
  -> YAML string
```

With `-s`, it emits all input documents as a YAML stream with document
separators.

## Debugging

The CLI can show each loader stage. This is useful when developing the parser
or investigating how a YAML document is interpreted:

```bash
yaml -D parse config.yaml
yaml -D compose config.yaml
yaml -D resolve config.yaml
yaml -D construct config.yaml
```

Use `-d` to run every debug stage.
