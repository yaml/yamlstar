# YAMLStar Kotlin Binding

Kotlin binding for the YAMLStar Java package and shared library.

```kotlin
import org.yamlstar.yamlstar.YS

val data = YS.loadObject("key: value")
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
