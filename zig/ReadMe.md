# YAMLStar Zig Binding

Zig binding for the YAMLStar shared library.

```zig
const yamlstar = @import("yamlstar");

var yaml = try yamlstar.YAMLStar.init(allocator);
defer yaml.deinit();
var result = try yaml.load("key: value");
defer result.deinit();
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
