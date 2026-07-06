## D Usage

Use `yamlstar` as a drop-in replacement for your current YAML loader:

File `app.d`:

```d
import std.file : readText;
import std.stdio : writeln;

import yamlstar;

void main()
{
  auto yaml = new YAMLStar();
  auto data = yaml.load(readText("config.yaml"));
  writeln(data.toPrettyString);
  yaml.close();
}
```


## Installation

Add the `yamlstar` package to your project and install the `libyamlstar.so`
shared library:

```bash
dub add yamlstar
curl -sSL https://yamlstar.org/install | LIB=1 bash
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

See <https://yamlstar.org/doc/install/> for more info.


### Requirements

* A D compiler (DMD, LDC or GDC) and DUB
* Linux, macOS or Windows
