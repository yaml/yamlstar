## Zig Usage

```zig
const yamlstar = @import("yamlstar");

var yaml = try yamlstar.YAMLStar.init(allocator);
defer yaml.deinit();
var result = try yaml.load("key: value");
defer result.deinit();
```
