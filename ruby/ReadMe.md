# YAMLStar Ruby Binding

Ruby binding for the YAMLStar shared library.

```ruby
require "yamlstar"

ys = YAMLStar.new
data = ys.load("key: value")
text = ys.dump({"foo" => [["bar"]]})
ys.close
```

Install the matching `libyamlstar` release before using this gem:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
gem install yamlstar
```

