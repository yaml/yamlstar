# YAMLStar Raku Binding

Raku binding for the YAMLStar shared library.

```raku
use YAMLStar;

my YAMLStar $ys .= new;
say $ys.load('key: value')<key>;
$ys.close;
```

Install the matching `libyamlstar` release before using this module:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
