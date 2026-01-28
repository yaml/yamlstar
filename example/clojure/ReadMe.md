# YAMLStar Clojure Example

This example demonstrates using the YAMLStar library from Clojars as a
standalone Clojure script.


## What it does

The `yaml-to-json.clj` script loads `../sample.yaml` and outputs it as
formatted JSON, demonstrating:

- Installing YAMLStar from Clojars
- Loading YAML files
- Converting YAML to Clojure data structures
- Outputting as JSON


## Running the Example

```bash
make test
```

This will:

1. Auto-install Clojure CLI tools and Leiningen
2. Download YAMLStar from Clojars (com.yaml/yamlstar)
3. Run the script with `clj -M yaml-to-json.clj`


### With a custom YAML file

From within `make shell`:

```bash
clj -M yaml-to-json.clj my-file.yaml
```

If no file is specified, it defaults to `../sample.yaml`.

## Expected Output

```
YAMLStar Example - Loading YAML and outputting JSON

Input YAML:
name: YAMLStar Example
version: 0.1.3
...

---

Output JSON:
{
  "name": "YAMLStar Example",
  "version": "0.1.3",
  ...
}

Success! YAMLStar loaded from Clojars and working correctly.
```


## Development Shell

Get a shell with `clj`, `lein`, and `java` installed:

```bash
make shell
```

This drops you into a bash shell with all the Clojure tools available in your
PATH.
The tools are installed locally to `.cache/makes/` so they don't require
system-wide installation.

Inside the shell, you can run:

- `clj` - Clojure CLI
- `lein` - Leiningen
- `java` - Java (auto-installed by makes)

Exit the shell with `exit` or Ctrl-D.


## Dependencies

The example uses:

- `com.yaml/yamlstar` - YAML parsing
- `org.clojure/data.json` - JSON output

Dependencies are specified in `project.clj` and were converted to `deps.edn`
via the lein-lein2deps plugin.
