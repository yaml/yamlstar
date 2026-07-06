# YAMLStar Nim Binding

Nim binding for the YAMLStar shared library.

```nim
import yamlstar

let yaml = newYAMLStar()
let data = yaml.load("key: value")
yaml.close()
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
