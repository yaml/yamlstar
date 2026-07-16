# YAMLStar Ada Binding

Ada binding for the YAMLStar shared library.

```ada
with YAMLStar;

procedure Example is
   JSON : constant String := YAMLStar.Load_JSON ("key: value");
begin
   null;
end Example;
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
