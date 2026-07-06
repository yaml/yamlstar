## Nim Usage

Use `yamlstar` as a drop-in replacement for your current YAML
loader:

File `main.nim`:

```nim
import std/json

import yamlstar

let yaml = newYAMLStar()
let data = yaml.load(readFile("config.yaml"))
echo data.pretty
yaml.close()
```


## Installation

Install the `yamlstar` Nimble package and the `libyamlstar.so` shared
library:

```bash
nimble install yamlstar
curl -sSL https://yamlstar.org/install | LIB=1 bash
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

See <https://yamlstar.org/doc/install/> for more info.


### Requirements

* Nim 2.0 or higher
* Linux, macOS or Windows
