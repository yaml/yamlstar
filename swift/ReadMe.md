# YAMLStar Swift Binding

Swift binding for the YAMLStar shared library.

```swift
import YAMLStar

let yaml = try YAMLStar()
let data = try yaml.load("key: value")
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
