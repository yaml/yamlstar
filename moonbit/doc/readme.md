## MoonBit Usage

```moonbit
fn main {
  let data = @yamlstar.load("answer: 42")
  println(data)
}
```

Install the MoonBit package and YAMLStar shared library:

```bash
moon add ingydotnet/yamlstar
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
