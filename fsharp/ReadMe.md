# YAMLStar F# Binding

F# binding for the YAMLStar shared library.

```fsharp
open YAMLStar

use yaml = new YAMLStar()
let data = yaml.Load("key: value")
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
