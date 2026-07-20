YAMLStar for MoonBit
====================

This package loads YAML into MoonBit JSON values through the YAMLStar shared
library.

## Usage

```moonbit
fn main {
  let data = @yamlstar.load("answer: 42")
  println(data)
}
```

## Installation

```bash
moon add ingydotnet/yamlstar
curl -sSL https://yamlstar.org/install | LIB=1 bash
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

Requires MoonBit with native target support. The package is published on
Mooncakes as `ingydotnet/yamlstar`.
