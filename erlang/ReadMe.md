# YAMLStar Erlang Binding

Erlang binding for the YAMLStar shared library.

```erlang
{ok, Data} = yamlstar:load(<<"key: value">>).
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
