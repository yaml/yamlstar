YAMLStar for Dyalog APL
=======================

This package loads YAML into Dyalog APL values through the YAMLStar shared
library.

## Usage

```apl
⎕FIX⊃⎕NGET 'src/YAMLStar.apln' 1
data←YAMLStar.Load 'answer: 42'
⎕←data.answer
```

## Installation

Install the
[`yaml-yamlstar`](https://tatin.dev/v1/packages/versions/yaml-yamlstar-0)
package from Tatin and install the YAMLStar shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```

Development and tests use the Docker-based Makefile:

```bash
make -C dyalog test
```

Requires Dyalog APL 18.2 or newer.
