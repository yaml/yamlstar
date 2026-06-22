# YAMLStar Julia Binding

Julia binding for the YAMLStar shared library.

```julia
import YAMLStar as YS

ys = YS.Runtime()
data = YS.load(ys, "key: value")
text = YS.dump(ys, Dict("foo" => ["bar"]))
YS.close(ys)
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
