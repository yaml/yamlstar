Use `YAMLStar.load` to load YAML text through `libyamlstar`.

```scala
import org.yamlstar.YAMLStar

val data = YAMLStar.load("key: value")
```

Install the package and the `libyamlstar` shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
