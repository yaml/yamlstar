## Dyalog APL Usage

```apl
⎕FIX⊃⎕NGET 'src/YAMLStar.apln' 1
data←YAMLStar.Load 'answer: 42'
⎕←data.answer
```

Install the `yaml-yamlstar` package from Tatin and the `libyamlstar`
shared library. Repository development and tests use `make -C dyalog test`.
