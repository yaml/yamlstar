# YAMLStar R Binding

R binding for the YAMLStar shared library.

```r
library(yamlstar)

data <- yamlstar_load("key: value")
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
