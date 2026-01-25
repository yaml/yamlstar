# YAMLStar for Java

Pure YAML 1.2 loader for Java, powered by Clojure.

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
  <groupId>org.yamlstar</groupId>
  <artifactId>yamlstar</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'org.yamlstar:yamlstar:0.1.0'
}
```

## Usage

```java
import org.yamlstar.YAMLStar;
import java.util.Map;
import java.util.List;

// Load a simple mapping
Map<String, Object> config = (Map<String, Object>) YAMLStar.load("key: value");
System.out.println(config.get("key")); // "value"

// Load a sequence
List<Object> items = (List<Object>) YAMLStar.load("- a\n- b\n- c");
System.out.println(items.get(0)); // "a"

// Load multiple documents
List<Object> docs = YAMLStar.loadAll("---\ndoc1\n---\ndoc2");
System.out.println(docs.size()); // 2

// Get version
String version = YAMLStar.version();
```

## API

### `YAMLStar.load(String yaml)`

Load a single YAML document and return it as a Java object.

**Returns:** `Object` (can be Map, List, String, Long, Double, Boolean, or null)

**Type Mapping:**

| YAML Type | Java Type |
|-----------|-----------|
| mapping | `java.util.HashMap` |
| sequence | `java.util.ArrayList` |
| string | `String` |
| integer | `Long` |
| float | `Double` |
| boolean | `Boolean` |
| null | `null` |

If the input contains multiple YAML documents, only the first document is
returned.

### `YAMLStar.loadAll(String yaml)`

Load all YAML documents from a string.

**Returns:** `java.util.List<Object>` containing all documents

Documents are separated by `---` markers in the YAML input.

### `YAMLStar.version()`

Get the YAMLStar version string.

**Returns:** `String`

## Features

- **100% YAML 1.2 spec compliant** - Implements the full YAML 1.2 Core Schema
- **Pure implementation** - No external YAML parser dependencies
- **Multi-document support** - Handle streams with multiple YAML documents
- **Anchors & Aliases** - Full support for YAML references
- **Explicit tags** - Support for `!!str`, `!!int`, `!!float`, etc.
- **Special float values** - `.inf`, `-.inf`, `.nan`

## YAML 1.2 Core Schema

Type inference follows the YAML 1.2 Core Schema:

- **null:** `null`, `Null`, `NULL`, `~`
- **bool:** `true`, `True`, `TRUE`, `false`, `False`, `FALSE`
- **int:** `[-+]?[0-9]+`
- **float:** Including `.inf`, `-.inf`, `.nan`
- **str:** Everything else

## Examples

### Loading Configuration

```java
String yaml = """
database:
  host: localhost
  port: 5432
  credentials:
    username: admin
    password: secret
""";

Map<String, Object> config = (Map<String, Object>) YAMLStar.load(yaml);
Map<String, Object> db = (Map<String, Object>) config.get("database");
System.out.println(db.get("host")); // "localhost"
System.out.println(db.get("port")); // 5432
```

### Loading a List of Items

```java
String yaml = """
- name: Alice
  age: 30
- name: Bob
  age: 25
""";

List<Object> people = (List<Object>) YAMLStar.load(yaml);
for (Object person : people) {
    Map<String, Object> p = (Map<String, Object>) person;
    System.out.println(p.get("name") + " is " + p.get("age"));
}
```

### Multi-Document Streams

```java
String yaml = """
---
document: 1
---
document: 2
---
document: 3
""";

List<Object> docs = YAMLStar.loadAll(yaml);
System.out.println(docs.size()); // 3
```

## Requirements

- Java 8 or higher
- Includes Clojure runtime (~5MB additional JAR size)

## License

MIT License - See [License](../License) file for details

## More Information

- **Project:** https://github.com/yaml/yamlstar
- **YAML 1.2 Spec:** https://yaml.org/spec/1.2/spec.html
