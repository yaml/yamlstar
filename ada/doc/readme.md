Use `YAMLStar.Load_JSON` to load YAML text through `libyamlstar`.

```ada
with YAMLStar;

procedure Example is
   JSON : constant String := YAMLStar.Load_JSON ("key: value");
begin
   null;
end Example;
```

Install the package and the `libyamlstar` shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
