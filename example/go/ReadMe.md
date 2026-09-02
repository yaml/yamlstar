# YAMLStar Go Example

This example demonstrates using the YAMLStar Go binding.


## What it does

The `yaml-to-json.go` script loads `../sample.yaml` and outputs it as formatted
JSON, demonstrating:

- Using the pure-Go YAMLStar module
- Loading YAML files
- Converting YAML to Go data structures
- Outputting as JSON


## Running the Example

```bash
make test
```

This will:

1. Auto-install Go
2. Run the script with `go run yaml-to-json.go`


### With a custom YAML file

From within `make shell`:

```bash
go run yaml-to-json.go my-file.yaml
```

If no file is specified, it defaults to `../sample.yaml`.

## Expected Output

```
YAMLStar Example - Loading ../sample.yaml and outputting JSON

Input YAML:
name: YAMLStar Example
version: 0.1.19
...

---

Output JSON:
{
  "name": "YAMLStar Example",
  "version": "0.1.19",
  ...
}
```


## Development Shell

Get a shell with Go set up:

```bash
make shell
```

This drops you into a bash shell with Go installed. The tools are installed
locally to `.cache/makes/` so they don't require system-wide installation.

Exit the shell with `exit` or Ctrl-D.


## Dependencies

- Go 1.24+
- No cgo or shared library

The repository example imports the root `github.com/yaml/yamlstar` module.
The published compatibility import path is `github.com/yaml/yamlstar-go`.
