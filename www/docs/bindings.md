# Language Bindings

YAMLStar provides native bindings for 32 programming languages, all using the
same underlying shared library.
This ensures consistent YAML behavior across supported binding platforms.

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

### Crystal

Crystal binding using native FFI.

**Install:**
```yaml
dependencies:
  yamlstar:
    github: yaml/yamlstar-crystal
```

**Quick Example:**
```crystal
require "yamlstar"

ys = YAMLStar.new
data = ys.load("key: value")
puts data["key"]
ys.close
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/crystal)

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

### Haskell

Haskell binding using FFI.

**Install:**
```bash
cabal install yamlstar
```

**Quick Example:**
```haskell
import YAMLStar

main = do
  data <- loadYAMLStar "key: value"
  print data
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/haskell)

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

### Julia

Julia binding using `ccall`.

**Install:**
```julia
using Pkg
Pkg.add("YAMLStar")
```

**Quick Example:**
```julia
import YAMLStar as YS

ys = YS.Runtime()
data = YS.load(ys, "key: value")
println(data["key"])
YS.close(ys)
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/julia)

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

### Ruby

Ruby binding using Fiddle.

**Install:**
```bash
gem install yamlstar
```

**Quick Example:**
```ruby
require "yamlstar"

ys = YAMLStar.new
data = ys.load("key: value")
puts data["key"]
ys.close
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/ruby)

</div>

<div class="binding-card" markdown>

### PHP

PHP binding using the FFI extension.

**Install:**
```bash
composer require yaml/yamlstar-php
```

**Quick Example:**
```php
$ys = new YAMLStar\YAMLStar();
$data = $ys->load("key: value");
echo $data["key"];
$ys->close();
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/php)

</div>

<div class="binding-card" markdown>

### Lua

Lua binding using cffi-lua or LuaJIT FFI.

**Install:**
```bash
luarocks install yamlstar
```

**Quick Example:**
```lua
local yamlstar = require("yamlstar")

local ys = yamlstar.new()
local data = ys:load("key: value")
print(data.key)
ys:close()
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/lua)

</div>

<div class="binding-card" markdown>

### Raku

Raku binding using NativeCall.

**Install:**
```bash
zef install YAMLStar
```

**Quick Example:**
```raku
use YAMLStar;

my YAMLStar $ys .= new;
say $ys.load('key: value')<key>;
$ys.close;
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/raku)

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

### Dyalog APL

Dyalog APL binding using a small native shim.

**Install:**
```apl
]Tatin.InstallPackages yaml-yamlstar
```

**Quick Example:**
```apl
data←YAMLStar.Load 'key: value'
⎕←data.key
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/dyalog)

</div>

<div class="binding-card" markdown>

### Ada

Ada binding using native FFI.

**Install:**
```bash
alr with yamlstar
curl -sSL https://yamlstar.org/install | LIB=1 bash
```

**Quick Example:**
```ada
with YAMLStar;

JSON : constant String := YAMLStar.Load_JSON ("key: value");
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/ada)

</div>

<div class="binding-card" markdown>

### Erlang

Erlang binding using a NIF.

**Install:**
```erlang
{deps, [yamlstar]}.
```

**Quick Example:**
```erlang
{ok, Data} = yamlstar:load(<<"key: value">>).
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/erlang)

</div>

<div class="binding-card" markdown>

### F#

F# binding using P/Invoke.

**Install:**
```bash
dotnet add package YAMLStar.FSharp
```

**Quick Example:**
```fsharp
open YAMLStar

use ys = new YAMLStar()
let data = ys.Load("key: value")
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/fsharp)

</div>

<div class="binding-card" markdown>

### PowerShell

PowerShell module using .NET native interop.

**Install:**
```powershell
Install-Module YAMLStar
```

**Quick Example:**
```powershell
Import-Module YAMLStar
$data = Invoke-YAMLStar "key: value"
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/powershell)

</div>

<div class="binding-card" markdown>

### Scala

Scala binding using JNA.

**Install:**
```scala
libraryDependencies += "org.yamlstar" % "scala-yamlstar" % "0.1.15"
```

**Quick Example:**
```scala
import org.yamlstar.YAMLStar

val data = YAMLStar.load("key: value")
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/scala)

</div>

<div class="binding-card" markdown>

### MoonBit

MoonBit binding using its native C interoperability.

**Install:**
```bash
moon add ingydotnet/yamlstar
curl -sSL https://yamlstar.org/install | LIB=1 bash
```

**Quick Example:**
```moonbit
let data = @yamlstar.load("key: value")
println(data)
```

[Full Documentation →](https://github.com/yaml/yamlstar/tree/main/moonbit)

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
- **Crystal**: `YAMLStar.new`
- **Go**: `yamlstar.New()`
- **Haskell**: no instance needed
- **Julia**: `YS.Runtime()`
- **Rust**: `YAMLStar::new()`
- **Perl**: `YAMLStar->new()`
- **Ruby/PHP**: `YAMLStar.new` / `new YAMLStar\YAMLStar()`
- **Raku**: `YAMLStar.new`
- **Lua**: `yamlstar.new()`
- **Fortran**: `yamlstar_new()`
- **Ada**: no instance needed
- **Erlang**: no instance needed
- **F#**: `new YAMLStar()`
- **PowerShell**: no instance needed
- **Scala**: no instance needed

### Loading Single Documents

Load a single YAML document:

- **Python/Java/C#**: `ys.load(yaml_string)`
- **Node.js/Go**: `ys.load(yaml_string)` or `ys.Load(yaml_string)`
- **Clojure**: `(yaml/load yaml-string)`
- **Crystal**: `ys.load(yaml_string)`
- **Haskell**: `loadYAMLStar yaml_text`
- **Julia**: `YS.load(ys, yaml_string)`
- **Rust**: `ys.load(&yaml_string)`
- **Perl**: `$ys->load($yaml_string)`
- **Ruby/PHP**: `ys.load(yaml_string)` / `$ys->load($yaml_string)`
- **Raku**: `$ys.load($yaml-string)`
- **Lua**: `ys:load(yaml_string)`
- **Fortran**: `call ys%load(yaml_string)`
- **Ada**: `YAMLStar.Load_JSON(yaml_string)`
- **Erlang**: `yamlstar:load(YamlBinary)`
- **F#**: `ys.Load(yaml_string)`
- **PowerShell**: `Invoke-YAMLStar $yamlString`
- **Scala**: `YAMLStar.load(yaml_string)`

### Loading Multiple Documents

Load all documents from a multi-document YAML stream:

- **Python**: `ys.load_all(yaml_string)`
- **Node.js**: `ys.loadAll(yaml_string)`
- **Clojure**: `(yaml/load-all yaml-string)`
- **Crystal**: `ys.load_all(yaml_string)`
- **Go**: `ys.LoadAll(yaml_string)`
- **Haskell**: `loadYAMLStarAll yaml_text`
- **Java/C#**: `ys.loadAll(yaml_string)`
- **Julia**: `YS.load_all(ys, yaml_string)`
- **Rust**: `ys.load_all(&yaml_string)`
- **Perl**: `$ys->load_all($yaml_string)`
- **Ruby**: `ys.load_all(yaml_string)`
- **PHP**: `$ys->loadAll($yaml_string)`
- **Raku**: `$ys.load-all($yaml-string)`
- **Lua**: `ys:load_all(yaml_string)`
- **Fortran**: `call ys%load_all(yaml_string)`
- **Ada/Dyalog/Erlang/F#/MoonBit/PowerShell/Scala**: initial bindings expose single-document load APIs

### Dumping Values

Dump JSON-compatible native values to YAML:

- **Python**: `ys.dump(value)` and `ys.dump_all(values)`
- **Node.js**: `ys.dump(value)` and `ys.dumpAll(values)`
- **Clojure**: `(yaml/dump value)` and `(yaml/dump-all values)`
- **Crystal**: `ys.dump(value)` and `ys.dump_all(values)`
- **Go**: `yamlstar.Dump(value)` and `yamlstar.DumpAll(values)`
- **Haskell**: `dumpYAMLStar value` and `dumpYAMLStarAll values`
- **Java/C#**: `dump(value)` / `Dump(value)` and `dumpAll(values)` / `DumpAll(values)`
- **Julia**: `YS.dump(ys, value)` and `YS.dump_all(ys, values)`
- **Rust**: `ys.dump(&value)` and `ys.dump_all(&values)`
- **Perl**: `$ys->dump($value)` and `$ys->dump_all($values)`
- **Ruby**: `ys.dump(value)` and `ys.dump_all(values)`
- **PHP**: `$ys->dump($value)` and `$ys->dumpAll($values)`
- **Raku**: `$ys.dump($value)` and `$ys.dump-all($values)`
- **Lua**: `ys:dump(value)` and `ys:dump_all(values)`
- **Fortran**: `ys%dump(json_value)` and `ys%dump_all(json_values)`
- **Ada/Dyalog/Erlang/F#/MoonBit/PowerShell/Scala**: load-only in the initial binding release

For example:

=== "Python"

    ```python
    ys.dump({'foo': [['bar']]})
    # "foo:\n- - bar\n"

    ys.dump_all(['doc1', {'a': 1}])
    # "---\ndoc1\n---\na: 1\n"
    ```

=== "Node.js"

    ```javascript
    ys.dump({foo: [['bar']]});
    // "foo:\n- - bar\n"

    ys.dumpAll(['doc1', {a: 1}]);
    // "---\ndoc1\n---\na: 1\n"
    ```

=== "Clojure"

    ```clojure
    (yaml/dump {"foo" [["bar"]]})
    ;; "foo:\n- - bar\n"

    (yaml/dump-all ["doc1" {"a" 1}])
    ;; "---\ndoc1\n---\na: 1\n"
    ```

### Cleanup

Close the YAMLStar instance when done:

- **Python/Node.js/Java/C#/Rust/Perl/Ruby/PHP**: `ys.close()`
- **Clojure**: No cleanup needed
- **Crystal**: `ys.close`
- **Go**: `ys.Close()`
- **Haskell**: No cleanup needed
- **Julia**: `YS.close(ys)`
- **Raku**: `$ys.close`
- **Lua**: `ys:close()`
- **Fortran**: `call ys%close()`
- **Ada/Dyalog/Erlang/MoonBit/PowerShell/Scala**: No cleanup needed
- **F#**: dispose the `YAMLStar` instance, typically with `use`

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
- **Windows**: x86_64 shared-library artifacts are built; binding-level
  native Windows support varies by language

The shared library (`libyamlstar.so`, `libyamlstar.dylib`, `yamlstar.dll`) is
built using GraalVM native-image for optimal performance and small binary size.

Ada, Crystal, Erlang, F#, Haskell, Julia, Raku, and Scala are
currently tested on the Linux/macOS shared-library path. Native Windows support
for these bindings is not claimed until their build and library lookup paths
are verified there.

Dyalog tests run only on Linux x86_64 because its Docker image is available
only for that platform. MoonBit uses its native target on supported runners.

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

### Crystal

- Requires Crystal 1.0+
- Uses native FFI
- Currently tested on Linux/macOS
- Available from the YAMLStar Crystal split repository

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

### Haskell

- Requires GHC 9.4+
- Uses FFI through Cabal
- Currently tested on Linux/macOS
- Available on Hackage

### Julia

- Requires Julia 1.x
- Uses `ccall` and JSON for native value conversion
- Currently tested on Linux/macOS
- Available through the Julia General registry

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

### Ruby

- Requires Ruby 2.7+
- Uses Fiddle from Ruby's standard library
- Available on RubyGems

### PHP

- Requires PHP 8.0+
- Uses the PHP FFI extension
- Available on Packagist

### Lua

- Requires Lua 5.1+ with cffi-lua or LuaJIT FFI
- Uses lua-cjson for JSON conversion
- Available on LuaRocks

### Raku

- Requires Rakudo
- Uses NativeCall
- Currently tested on Linux/macOS
- Available through zef/fez

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

### Dyalog APL

- Requires Dyalog APL 18.2+ and Docker for repository tests
- Uses a native C shim around `libyamlstar`
- Published through Tatin as `yaml-yamlstar`

### MoonBit

- Requires MoonBit with native target support
- Uses MoonBit's native C interoperability
- Published on Mooncakes as `ingydotnet/yamlstar`

### Ada

- Requires GNAT
- Uses native FFI
- Published through Alire
- Initial API returns the JSON envelope from `yamlstar_load`

### Erlang

- Requires Erlang/OTP
- Uses a dirty CPU NIF around `libyamlstar`
- Published on Hex as `yamlstar_erlang`

### F#

- Requires .NET 8+
- Uses P/Invoke
- Returns `System.Text.Json.JsonElement`
- Available on NuGet as `YAMLStar.FSharp`

### PowerShell

- Requires PowerShell 7+
- Uses .NET native interop
- Available through PowerShell Gallery as `YAMLStar`

### Scala

- Requires Java 17+ and Scala 3
- Uses JNA
- Returns uPickle/uJSON values
- Available on Maven Central as `org.yamlstar:scala-yamlstar`

## Contributing a New Binding

Want to add support for another language?
See the [Contributing Guide](https://github.com/yaml/yamlstar/blob/main/CONTRIBUTING.md)
for instructions on creating new language bindings.

The shared library provides a simple JSON-based FFI:

```c
char* yamlstar_load(char* yaml_input);
char* yamlstar_load_all(char* yaml_input);
char* yamlstar_dump(char* data_json);
char* yamlstar_dump_all(char* data_json);
```

`yamlstar_load` and `yamlstar_load_all` return JSON strings that can be parsed
by your language's native JSON library. `yamlstar_dump` and
`yamlstar_dump_all` accept JSON strings and return YAML text.

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
