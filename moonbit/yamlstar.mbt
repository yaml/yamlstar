///|
pub const VERSION = "0.1.16"

///|
pub suberror YAMLStarError {
  YAMLStarError(String)
} derive(@debug.Debug)

///|
#borrow(input)
extern "C" fn yamlstar_load(input : Bytes) -> Bytes = "ys_yamlstar_load"

///|
pub fn load(input : String) -> Json raise {
  let response = input |> @utf8.encode |> yamlstar_load |> @utf8.decode
  let json = @json.parse(response)
  match json {
    Object(object) =>
      match object.get("error") {
        Some(Object(error)) =>
          match error.get("cause") {
            Some(String(cause)) => raise YAMLStarError(cause)
            _ => raise YAMLStarError("Unknown YAMLStar error")
          }
        _ =>
          match object.get("data") {
            Some(data) => data
            _ => raise YAMLStarError("Unexpected response from libyamlstar")
          }
      }
    _ => raise YAMLStarError("Unexpected response from libyamlstar")
  }
}
