Use `yamlstar:load/1` to load YAML text through `libyamlstar`.

```erlang
{ok, Data} = yamlstar:load(<<"key: value">>).
```

Install the package and the `libyamlstar` shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
