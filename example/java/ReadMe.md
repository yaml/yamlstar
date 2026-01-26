# YAMLStar Java Example

This example demonstrates using the YAMLStar library from Maven Central as a
standalone Java application.


## What it does

The `YamlToJson.java` program loads `../sample.yaml` and outputs it as
formatted JSON, demonstrating:

- Installing YAMLStar from Maven Central
- Loading YAML files
- Converting YAML to Java objects (HashMap, ArrayList)
- Outputting as JSON


## Running the Example

```bash
make test
```

This will:

1. Auto-install Maven
2. Download YAMLStar from Maven Central (com.yaml:yamlstar)
3. Compile and run the program


### With a custom YAML file

From within `make shell`:

```bash
mvn -q compile exec:java -Dexec.mainClass=example.YamlToJson -Dexec.args="my-file.yaml"
```

If no file is specified, it defaults to `../sample.yaml`.


## Expected Output

```
YAMLStar Example - Loading ../sample.yaml and outputting JSON

Input YAML:
name: YAMLStar Example
version: 0.1.0
...

---

Output JSON:
{
  "name": "YAMLStar Example",
  "version": "0.1.0",
  ...
}

Success! YAMLStar loaded from Maven Central and working correctly.
```


## Development Shell

Get a shell with `mvn` and `java` installed:

```bash
make shell
```

This drops you into a bash shell with Maven and Java available in your PATH.
The tools are installed locally to `.cache/makes/` so they don't require
system-wide installation.

Exit the shell with `exit` or Ctrl-D.


## Maven Dependency

To use YAMLStar in your own project, add to your `pom.xml`:

```xml
<dependency>
  <groupId>com.yaml</groupId>
  <artifactId>yamlstar</artifactId>
  <version>0.1.0</version>
</dependency>
```


## Java API

```java
import com.yaml.YAMLStar;

// Load a single YAML document
Object data = YAMLStar.load(yamlString);

// Load multiple YAML documents
List<Object> docs = YAMLStar.loadAll(yamlString);

// Get version
String version = YAMLStar.version();
```

YAML types are converted to Java types:
- Mappings -> `java.util.HashMap`
- Sequences -> `java.util.ArrayList`
- Strings -> `String`
- Integers -> `Long`
- Floats -> `Double`
- Booleans -> `Boolean`
- Null -> `null`
