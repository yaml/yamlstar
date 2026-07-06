## R Usage

Use `yamlstar` as a drop-in replacement for your current YAML
loader:

File `main.R`:

```r
library(yamlstar)

input <- paste(readLines("config.yaml"), collapse = "\n")
data <- yamlstar_load(input)
str(data)
```


## Installation

Install the `yamlstar` R package from GitHub and the `libyamlstar.so`
shared library:

```r
remotes::install_github("yaml/yamlstar-r")
```

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

See <https://yamlstar.org/doc/install/> for more info.


### Requirements

* R 4.0 or higher (with a C compiler for the package build)
* The jsonlite package
* Linux or macOS
