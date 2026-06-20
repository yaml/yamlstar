# YAMLStar CLI

The `yaml` command loads YAML and prints JSON by default.

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

Load every document in a YAML stream:

```bash
yaml -s stream.yaml
```

Evaluate a YAML string directly:

```bash
yaml -e 'a: 1'
```

Write output to a file:

```bash
yaml config.yaml -o config.json
```

## Options

```text
Usage: yaml [options] [file]

Default: Read stdin, output compact JSON

Options:
  -f, --file FILE          Input file (or use positional arg)
  -e, --eval YAML          Evaluate YAML string
  -J, --json               Output pretty JSON
  -Y, --yaml               Output YAML
  -o, --output FILE        Output file
  -s, --stream             Output all documents
  -d, --debug              Debug all stages
  -D, --debug-stage STAGE  Debug specific stage: parse, compose, resolve,
                           construct
  -S, --stack-trace        Show full stack traces
  -v, --version            Print version
  -h, --help               Print help
```

!!! note

    `-Y` currently emits JSON with a YAML-output notice. Full YAML emission is
    planned for a later release.

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
