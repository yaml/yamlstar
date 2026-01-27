# YAMLStar Python Example

This example demonstrates using the YAMLStar Python binding.


## What it does

The `yaml-to-json.py` script loads `../sample.yaml` and outputs it as formatted
JSON, demonstrating:

- Using the YAMLStar Python binding
- Loading YAML files
- Converting YAML to Python data structures
- Outputting as JSON


## Running the Example

```bash
make test
```

This will:

1. Auto-install Python virtualenv
2. Set up `LD_LIBRARY_PATH` for libyamlstar
3. Run the script with `python yaml-to-json.py`


### With a custom YAML file

From within `make shell`:

```bash
python yaml-to-json.py my-file.yaml
```

If no file is specified, it defaults to `../sample.yaml`.

## Expected Output

```
YAMLStar Example - Loading ../sample.yaml and outputting JSON

Input YAML:
name: YAMLStar Example
version: 0.1.1
...

---

Output JSON:
{
  "name": "YAMLStar Example",
  "version": "0.1.1",
  ...
}
```


## Development Shell

Get a shell with Python and the library path set up:

```bash
make shell
```

This drops you into a bash shell with Python installed and `LD_LIBRARY_PATH`
configured to find libyamlstar.
The tools are installed locally to `.cache/makes/` so they don't require
system-wide installation.

Exit the shell with `exit` or Ctrl-D.


## Dependencies

- Python 3.6+
- libyamlstar shared library (from `../../libyamlstar/lib`)

The example uses the local YAMLStar Python binding from `../../python/lib` and
the native libyamlstar shared library.
