# YAMLStar PowerShell Binding

PowerShell binding for the YAMLStar shared library.

```powershell
Import-Module YAMLStar
$data = Invoke-YAMLStar "key: value"
```

Install the matching `libyamlstar` release before using this module:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
