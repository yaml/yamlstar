Use `YAMLStar.Load` to load YAML text through `libyamlstar`.

```fsharp
open YAMLStar

use yaml = new YAMLStar()
let data = yaml.Load("key: value")
```

Install the package and the `libyamlstar` shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
