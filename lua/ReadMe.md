# YAMLStar Lua Binding

Lua binding for the YAMLStar shared library.

```lua
local yamlstar = require("yamlstar")

local ys = yamlstar.new()
local data = ys:load("key: value")
local text = ys:dump({foo = {{"bar"}}})
ys:close()
```

Install the matching `libyamlstar` release before using this rock:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
luarocks install yamlstar
```

