Use `Invoke-YAMLStar` to load YAML text through `libyamlstar`.

```powershell
Import-Module YAMLStar
$data = Invoke-YAMLStar "key: value"
```

Install the module and the `libyamlstar` shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
