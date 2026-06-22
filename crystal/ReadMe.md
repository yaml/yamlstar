# YAMLStar Crystal Binding

Crystal binding for the YAMLStar shared library.

```crystal
require "yamlstar"

ys = YAMLStar.new
data = ys.load("key: value")
text = ys.dump({"foo" => ["bar"]})
ys.close
```

Install the matching `libyamlstar` release before using this shard:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
