# YAMLStar Elixir Binding

Elixir binding for the YAMLStar shared library.

```elixir
{:ok, data} = YAMLStar.load("key: value")
data = YAMLStar.load!("foo: bar")
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
