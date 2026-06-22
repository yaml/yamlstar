# YAMLStar Haskell Binding

Haskell binding for the YAMLStar shared library.

```haskell
import YAMLStar

main = do
  value <- loadYAMLStar "key: value"
  print value
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
