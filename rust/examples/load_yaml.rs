// Copyright 2024 yaml.org
// MIT License

//! Example demonstrating basic usage of the yamlstar library.

use serde::Deserialize;
use yamlstar::YAMLStar;

#[derive(Deserialize, Debug)]
struct Config {
    host: String,
    port: u16,
    debug: bool,
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Create a new YAMLStar instance
    let ys = YAMLStar::new()?;

    // Example 1: Load to dynamic JSON value
    println!("Example 1: Load to serde_json::Value");
    let data: serde_json::Value = ys.load("key: value")?;
    println!("  Result: {data}");

    // Example 2: Load to typed struct
    println!("\nExample 2: Load to typed struct");
    let config: Config = ys.load(
        r#"
host: localhost
port: 8080
debug: true
"#,
    )?;
    println!("  Config: {config:?}");
    println!("  Server will run on {}:{}", config.host, config.port);
    println!("  Debug mode: {}", config.debug);

    // Example 3: Load sequence
    println!("\nExample 3: Load sequence");
    let items: Vec<String> = ys.load("- apple\n- banana\n- cherry")?;
    println!("  Items: {items:?}");

    // Example 4: Load all documents
    println!("\nExample 4: Load multiple documents");
    let docs: Vec<String> = ys.load_all("---\nfirst\n---\nsecond\n---\nthird")?;
    println!("  Documents: {docs:?}");

    // Example 5: Get library version
    println!("\nExample 5: Library version");
    let version = ys.version()?;
    println!("  YAMLStar version: {version}");

    Ok(())
}
