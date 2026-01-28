# Language Bindings

YAMLStar provides native bindings for 10 programming languages, all using the
same underlying shared library.
This ensures 100% consistent behavior across all platforms.

## Available Bindings

<div class="binding-grid" markdown>

<div class="binding-card" markdown>

### Python

Pure Python binding using ctypes.

**Install:**
```bash
pip install yamlstar
```

**Quick Example:**
```python
from yamlstar import YAMLStar

ys = YAMLStar()
data = ys.load('key: value')
print(data)  # {'key': 'value'}
ys.close()
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/python)

</div>

<div class="binding-card" markdown>

### Node.js

Native JavaScript binding for Node.js.

**Install:**
```bash
npm install yamlstar
```

**Quick Example:**
```javascript
const YAMLStar = require('yamlstar');

const ys = new YAMLStar();
const data = ys.load('key: value');
console.log(data);  // { key: 'value' }
ys.close();
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/nodejs)

</div>

<div class="binding-card" markdown>

### Clojure

Native Clojure library (no FFI required).

**Install:**
```clojure
{:deps {org.yamlstar/yamlstar {:mvn/version "0.1.2"}}}
```

**Quick Example:**
```clojure
(require '[yamlstar.core :as yaml])

(yaml/load "key: value")
;=> {"key" "value"}
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/clojure)

</div>

<div class="binding-card" markdown>

### Go

Native Go binding using cgo.

**Install:**
```bash
go get github.com/yaml/yamlstar-go
```

**Quick Example:**
```go
import "github.com/yaml/yamlstar-go"

ys := yamlstar.New()
data := ys.Load("key: value")
fmt.Println(data)
ys.Close()
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/go)

</div>

<div class="binding-card" markdown>

### Java

Java binding using JNI.

**Install (Maven):**
```xml
<dependency>
  <groupId>com.yaml</groupId>
  <artifactId>yamlstar</artifactId>
  <version>0.1.2</version>
</dependency>
```

**Quick Example:**
```java
import com.yaml.yamlstar.YAMLStar;

YAMLStar ys = new YAMLStar();
Map<String, Object> data = ys.load("key: value");
System.out.println(data);
ys.close();
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/java)

</div>

<div class="binding-card" markdown>

### Rust

Rust binding using FFI.

**Install:**
```toml
[dependencies]
yamlstar = "0.1.2"
```

**Quick Example:**
```rust
use yamlstar::YAMLStar;

let ys = YAMLStar::new();
let data = ys.load("key: value");
println!("{:?}", data);
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/rust)

</div>

<div class="binding-card" markdown>

### Perl

Perl binding using FFI::Platypus.

**Install:**
```bash
cpanm YAMLStar
```

**Quick Example:**
```perl
use YAMLStar;

my $ys = YAMLStar->new();
my $data = $ys->load('key: value');
print $data->{key};  # value
$ys->close();
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/perl)

</div>

<div class="binding-card" markdown>

### C#

C# binding using P/Invoke.

**Install:**
```bash
dotnet add package YAMLStar
```

**Quick Example:**
```csharp
using YAMLStar;

var ys = new YAMLStar();
var data = ys.Load("key: value");
Console.WriteLine(data["key"]);
ys.Close();
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/csharp)

</div>

<div class="binding-card" markdown>

### Delphi (Pascal)

Free Pascal binding using native FFI.

**Install:**
```bash
# Build the YAMLStar shared library first
cd libyamlstar
make native && sudo make install

# Build the Delphi binding
cd ../delphi
make build
```

**Quick Example:**
```pascal
program example;
uses yamlstar, fpjson;
var
  ys: TYAMLStar;
  data: TJSONData;
begin
  ys := TYAMLStar.Create;
  try
    data := ys.Load('key: value');
    try
      WriteLn(data.FormatJSON);
    finally
      data.Free;
    end;
  finally
    ys.Free;
  end;
end.
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/delphi)

</div>

<div class="binding-card" markdown>

### Fortran

Modern Fortran binding using iso_c_binding.

**Install:**
```bash
fpm install yamlstar
```

**Quick Example:**
```fortran
use yamlstar
type(yamlstar_t) :: ys

ys = yamlstar_new()
call ys%load('key: value')
call ys%close()
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/fortran)

</div>

</div>

## Common API

All bindings provide the same core functionality:

### Constructor/Initialization

Create a new YAMLStar instance:

- **Python/Node.js/Java/C#**: `YAMLStar()` or `new YAMLStar()`
- **Clojure**: `(require '[yamlstar.core :as yaml])` - no instance needed
- **Go**: `yamlstar.New()`
- **Rust**: `YAMLStar::new()`
- **Perl**: `YAMLStar->new()`
- **Fortran**: `yamlstar_new()`

### Loading Single Documents

Load a single YAML document:

- **Python/Java/C#**: `ys.load(yaml_string)`
- **Node.js/Go**: `ys.load(yaml_string)` or `ys.Load(yaml_string)`
- **Clojure**: `(yaml/load yaml-string)`
- **Rust**: `ys.load(&yaml_string)`
- **Perl**: `$ys->load($yaml_string)`
- **Fortran**: `call ys%load(yaml_string)`

### Loading Multiple Documents

Load all documents from a multi-document YAML stream:

- **Python**: `ys.load_all(yaml_string)`
- **Node.js**: `ys.loadAll(yaml_string)`
- **Clojure**: `(yaml/load-all yaml-string)`
- **Go**: `ys.LoadAll(yaml_string)`
- **Java/C#**: `ys.loadAll(yaml_string)`
- **Rust**: `ys.load_all(&yaml_string)`
- **Perl**: `$ys->load_all($yaml_string)`
- **Fortran**: `call ys%load_all(yaml_string)`

### Cleanup

Close the YAMLStar instance when done:

- **Python/Node.js/Java/C#/Rust/Perl**: `ys.close()`
- **Clojure**: No cleanup needed
- **Go**: `ys.Close()`
- **Fortran**: `call ys%close()`

!!! note "Resource Management"
    The YAMLStar instance uses native resources (shared library handles).
    Always call `close()` when you're done to free these resources.
    In languages with RAII (Rust, C++), this happens automatically.

## Building Bindings from Source

Each binding can be built and tested independently:

```bash
# Clone the repository
git clone https://github.com/yaml/yamlstar.git
cd yamlstar

# Build the core shared library first
cd libyamlstar
make build

# Build and test a specific binding
cd ../python
make test
```

The build system automatically installs all required tools and dependencies on
first run using the [Makes](https://github.com/makeplus/makes) system.

## Platform Support

YAMLStar bindings are tested on:

- **Linux**: x86_64, arm64
- **macOS**: x86_64 (Intel), arm64 (Apple Silicon)
- **Windows**: x86_64 (via WSL or native)

The shared library (`libyamlstar.so`, `libyamlstar.dylib`, `yamlstar.dll`) is
built using GraalVM native-image for optimal performance and small binary size.

## Language-Specific Notes

### Python

- Requires Python 3.7+
- Uses `ctypes` for FFI (no compilation required)
- Thread-safe when using separate instances
- Pip package includes pre-built binaries for common platforms

### Node.js

- Requires Node.js 14+
- Uses `node-gyp` for native bindings
- Async API planned for future release
- NPM package includes pre-built binaries

### Clojure

- Requires Clojure 1.12+
- No FFI overhead (native Clojure implementation)
- Works with Leiningen and deps.edn
- Available on Clojars

### Go

- Requires Go 1.20+
- Uses cgo (requires C compiler)
- Native Go types (map[string]interface{}, []interface{})
- Available via go get

### Java

- Requires Java 11+
- Uses JNI for native calls
- Returns standard Java collections
- Available on Maven Central

### Rust

- Requires Rust 1.70+
- Uses FFI with safety guarantees
- Returns serde-compatible types
- Available on crates.io

### Perl

- Requires Perl 5.32+
- Uses FFI::Platypus
- Returns Perl hashes and arrays
- Available on CPAN

### C#

- Requires .NET 6+
- Uses P/Invoke
- Returns standard .NET collections
- Available on NuGet

### Delphi (Pascal)

- Requires Free Pascal Compiler (FPC) 3.0+
- Uses native FFI
- Returns FPJson TJSONData objects
- Compatible with Delphi and Lazarus

### Fortran

- Requires gfortran 10+ or Intel Fortran 2021+
- Uses iso_c_binding
- Modern Fortran 2018 features
- Available via FPM (Fortran Package Manager)

## Contributing a New Binding

Want to add support for another language?
See the [Contributing Guide](https://github.com/yaml/yamlstar/blob/main/CONTRIBUTING.md)
for instructions on creating new language bindings.

The shared library provides a simple JSON-based FFI:

```c
char* yamlstar_load(char* yaml_input);
char* yamlstar_load_all(char* yaml_input);
```

Both functions return JSON strings that can be parsed by your language's native
JSON library.

## Performance

All bindings use the same underlying C library, so performance is consistent:

- **Parsing**: ~50-100 MB/s (depends on document complexity)
- **Memory**: ~2-5x the input size during parsing
- **Startup**: <10ms (native binary, no JVM warmup)

For detailed benchmarks, see the [performance documentation](https://github.com/yaml/yamlstar/tree/main/bench).

## Next Steps

- [Get started](getting-started.md) with your language of choice
- Explore the [roadmap](roadmap.md) for upcoming features
- Read [about YAMLStar](about.md) to understand the architecture
- Visit the [GitHub repository](https://github.com/yaml/yamlstar) to contribute
