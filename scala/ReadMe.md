# YAMLStar Scala Binding

Scala binding for the YAMLStar shared library.

```scala
import org.yamlstar.YAMLStar

val data = YAMLStar.load("key: value")
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
