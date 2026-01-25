# YAMLStar Rust Binding

Pure YAML 1.2 loader for Rust using the `libyamlstar` shared library.

## Overview

YAMLStar is a Rust binding for the [YAMLStar](https://github.com/yaml/yamlstar) library - a pure YAML 1.2 implementation written in Clojure. This binding provides a fast, spec-compliant YAML parser with zero Rust dependencies beyond the FFI layer.

## Quick Start

```rust
use yamlstar::YAMLStar;
use serde::Deserialize;

#[derive(Deserialize)]
struct Config {
    host: String,
    port: u16,
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let ys = YAMLStar::new()?;

    // Load YAML to dynamic type
    let data: serde_json::Value = ys.load("key: value")?;

    // Load YAML to typed struct
    let config: Config = ys.load("host: localhost\nport: 8080")?;

    // Load multiple documents
    let docs: Vec<String> = ys.load_all("---\ndoc1\n---\ndoc2")?;

    Ok(())
}
```

## Installation

Add to your `Cargo.toml`:

```toml
[dependencies]
yamlstar = "0.1"
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"
```

### Installing the Shared Library

The binding requires `libyamlstar.so` to be available at runtime. You have several options:

**Option 1: Build from source**
```bash
cd libyamlstar
make native
```

**Option 2: Install to system**
```bash
# Copy to system library directory
sudo cp libyamlstar/lib/libyamlstar.so.0.1.0 /usr/local/lib/
```

**Option 3: Use LD_LIBRARY_PATH**
```bash
export LD_LIBRARY_PATH=/path/to/libyamlstar/lib:$LD_LIBRARY_PATH
cargo run
```

## API Reference

### `YAMLStar::new() -> Result<Self, Error>`

Creates a new YAMLStar instance. This initializes the GraalVM isolate for thread-safe YAML parsing.

### `YAMLStar::load<T>(&self, yaml: &str) -> Result<T, Error>`

Loads a single YAML document and deserializes it into type `T`. The type must implement `serde::de::DeserializeOwned`.

**Example:**
```rust
let config: Config = ys.load("host: localhost\nport: 8080")?;
```

### `YAMLStar::load_all<T>(&self, yaml: &str) -> Result<Vec<T>, Error>`

Loads all YAML documents from a multi-document string and returns them as a `Vec<T>`.

**Example:**
```rust
let docs: Vec<String> = ys.load_all("---\ndoc1\n---\ndoc2")?;
```

### `YAMLStar::version(&self) -> Result<String, Error>`

Returns the version string of the underlying YAMLStar library.

## Features

- **100% YAML 1.2 Core Schema Compliance**: Implements the full YAML 1.2 specification
- **Type-Safe**: Generic deserialization using Serde
- **Zero-Copy where possible**: Efficient FFI layer
- **Thread-Safe**: Each `YAMLStar` instance has its own GraalVM isolate

## Known Limitations

- **Special Float Values**: While YAML 1.2 supports `.inf`, `-.inf`, and `.nan`, these values cannot be serialized to JSON by the underlying library and will result in an error

## Examples

Run the included example:
```bash
cd rust
make example
```

Or with cargo:
```bash
LD_LIBRARY_PATH=../libyamlstar/lib cargo run --example load_yaml
```

## Testing

```bash
make test
```

Or with cargo:
```bash
LD_LIBRARY_PATH=../libyamlstar/lib cargo test
```

## YAML 1.2 Core Schema

YAMLStar implements the YAML 1.2 Core Schema with the following type mappings:

| YAML Type | Rust Type |
|-----------|-----------|
| `!!null` | `None`, `()` |
| `!!bool` | `bool` |
| `!!int` | `i64`, `u64`, etc. |
| `!!float` | `f64`, `f32` |
| `!!str` | `String`, `&str` |
| `!!map` | `HashMap`, structs |
| `!!seq` | `Vec`, arrays |

## Requirements

- Rust 1.70 or higher
- `libyamlstar` shared library (built with GraalVM)
- Linux or macOS (other platforms not yet supported)

## License

MIT License - Copyright 2024 yaml.org

See [License](License) file for details.

## Related Projects

- [YAMLStar](https://github.com/yaml/yamlstar) - Core YAML 1.2 implementation in Clojure
- [YAMLScript](https://github.com/yaml/yamlscript) - Programming language that compiles to YAML
