# YAMLStar Delphi Example

This example demonstrates using the YAMLStar Delphi/Pascal binding.

## What it does

The `yaml-to-json.pas` program loads `../sample.yaml` and outputs it as
formatted JSON, demonstrating:

- Using the YAMLStar Delphi/Pascal binding
- Loading YAML files
- Converting YAML to Pascal data structures (TJSONData)
- Outputting as formatted JSON

## Running the Example

```bash
make test
```

This will:

1. Check for Free Pascal Compiler (FPC) installation
2. Compile the example program
3. Set up `LD_LIBRARY_PATH` for libyamlstar
4. Run the program with `./yaml-to-json`

**Note:** FPC must be installed on your system first.
Install from https://www.freepascal.org/ or via your package manager.

### With a custom YAML file

After building with `make test`, you can run:

```bash
LD_LIBRARY_PATH=../../libyamlstar/lib ./yaml-to-json my-file.yaml
```

If no file is specified, it defaults to `../sample.yaml`.

## Expected Output

```
YAMLStar Example - Loading ../sample.yaml and outputting JSON

Input YAML:
name: YAMLStar Example
version: 0.1.2
...

---

Output JSON:
{
  "name": "YAMLStar Example",
  "version": "0.1.2",
  ...
}
```

## Development

To rebuild after making changes:

```bash
make clean
make test
```

## Dependencies

- Free Pascal Compiler (FPC) 3.0+
- libyamlstar shared library (from `../../libyamlstar/lib`)

The example uses the local YAMLStar Delphi binding from `../../delphi/src` and
the native libyamlstar shared library.
