// Copyright 2024 yaml.org
// MIT License

#[test]
fn load_simple_scalar() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<String>("hello").unwrap();
    assert_eq!(ret, "hello");
}

#[test]
fn load_integer() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<i64>("42").unwrap();
    assert_eq!(ret, 42);
}

#[test]
fn load_float() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<f64>("3.14").unwrap();
    assert!((ret - 3.14).abs() < 0.001);
}

#[test]
fn load_boolean_true() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<bool>("true").unwrap();
    assert!(ret);
}

#[test]
fn load_boolean_false() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<bool>("false").unwrap();
    assert!(!ret);
}

#[test]
fn load_null() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<Option<String>>("null").unwrap();
    assert!(ret.is_none());
}

#[test]
fn load_simple_mapping() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<serde_json::Value>("key: value").unwrap();
    let obj = ret.as_object().unwrap();
    assert_eq!(obj.get("key").unwrap().as_str().unwrap(), "value");
}

#[derive(serde::Deserialize, Debug, PartialEq)]
struct Config {
    host: String,
    port: u16,
}

#[test]
fn load_mapping_to_struct() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let config = ys.load::<Config>("host: localhost\nport: 8080").unwrap();
    assert_eq!(config.host, "localhost");
    assert_eq!(config.port, 8080);
}

#[test]
fn load_sequence() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<Vec<String>>("- a\n- b\n- c").unwrap();
    assert_eq!(ret, vec!["a", "b", "c"]);
}

#[test]
fn load_flow_sequence() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<Vec<String>>("[a, b, c]").unwrap();
    assert_eq!(ret, vec!["a", "b", "c"]);
}

#[test]
fn load_flow_mapping() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<serde_json::Value>("{key: value}").unwrap();
    let obj = ret.as_object().unwrap();
    assert_eq!(obj.get("key").unwrap().as_str().unwrap(), "value");
}

#[test]
fn load_nested_structure() {
    let yaml = r#"
users:
  - name: Alice
    age: 30
  - name: Bob
    age: 25
"#;

    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<serde_json::Value>(yaml).unwrap();
    let obj = ret.as_object().unwrap();
    let users = obj.get("users").unwrap().as_array().unwrap();
    assert_eq!(users.len(), 2);
    assert_eq!(
        users[0].as_object().unwrap().get("name").unwrap(),
        "Alice"
    );
    assert_eq!(users[0].as_object().unwrap().get("age").unwrap(), 30);
}

#[test]
fn load_type_coercion() {
    #[derive(serde::Deserialize)]
    struct TypeTest {
        string: String,
        integer: i64,
        float: f64,
        bool_true: bool,
        bool_false: bool,
        null_value: Option<String>,
    }

    let yaml = r#"
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
"#;

    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load::<TypeTest>(yaml).unwrap();
    assert_eq!(ret.string, "hello");
    assert_eq!(ret.integer, 42);
    assert!((ret.float - 3.14).abs() < 0.001);
    assert!(ret.bool_true);
    assert!(!ret.bool_false);
    assert!(ret.null_value.is_none());
}

#[test]
fn load_all_single_document() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load_all::<String>("hello").unwrap();
    assert_eq!(ret, vec!["hello"]);
}

#[test]
fn load_all_multiple_documents() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load_all::<String>("---\ndoc1\n---\ndoc2\n---\ndoc3").unwrap();
    assert_eq!(ret, vec!["doc1", "doc2", "doc3"]);
}

#[test]
fn load_all_mixed_types() {
    let yaml = "---\n42\n---\nhello\n---\ntrue";
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ret = ys.load_all::<serde_json::Value>(yaml).unwrap();
    assert_eq!(ret.len(), 3);
    assert_eq!(ret[0].as_i64().unwrap(), 42);
    assert_eq!(ret[1].as_str().unwrap(), "hello");
    assert_eq!(ret[2].as_bool().unwrap(), true);
}

#[test]
fn version() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let ver = ys.version().unwrap();
    assert!(!ver.is_empty());
    assert!(ver.contains('.')); // Version should contain dots like "0.1.0-SNAPSHOT"
}

#[test]
fn error_handling_malformed_yaml() {
    let ys = yamlstar::YAMLStar::new().unwrap();
    let result = ys.load::<serde_json::Value>("key: \"unclosed");
    assert!(result.is_err());
}

#[test]
fn load_multiple_times() {
    let ys = yamlstar::YAMLStar::new().unwrap();

    let ret1 = ys.load::<String>("hello").unwrap();
    assert_eq!(ret1, "hello");

    let ret2 = ys.load::<i64>("42").unwrap();
    assert_eq!(ret2, 42);

    let ret3 = ys.load::<Config>("host: test\nport: 9000").unwrap();
    assert_eq!(ret3.host, "test");
    assert_eq!(ret3.port, 9000);
}

#[test]
fn load_quoted_strings() {
    let ys = yamlstar::YAMLStar::new().unwrap();

    let ret = ys.load::<String>("\"hello world\"").unwrap();
    assert_eq!(ret, "hello world");

    let ret = ys.load::<String>("'single quoted'").unwrap();
    assert_eq!(ret, "single quoted");
}

// Note: Special float values (.inf, -.inf, .nan) are valid YAML 1.2 but
// cannot be serialized to JSON by libyamlstar's JSON encoder.
// This is a known limitation of the underlying library.
