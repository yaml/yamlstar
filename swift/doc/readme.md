## Swift Usage

Use `YAMLStar` as a drop-in replacement for your current YAML loader:

File `main.swift`:

```swift
import Foundation
import YAMLStar

let yaml = try YAMLStar()
let input = try String(
    contentsOfFile: "config.yaml", encoding: .utf8)
let data = try yaml.load(input)
print(data ?? "null")
```


## Installation

Add the `yamlstar-swift` package to your project and install the
`libyamlstar.so` shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

In your `Package.swift`:

```swift
dependencies: [
    .package(
        url: "https://github.com/yaml/yamlstar-swift",
        from: "0.1.16"),
],
targets: [
    .executableTarget(
        name: "your-app",
        dependencies: [
            .product(name: "YAMLStar", package: "yamlstar-swift")
        ]),
]
```

See <https://yamlstar.org/doc/install/> for more info.


### Requirements

* Swift 5.9 or higher
* Linux or macOS
