///|
test "load YAML" {
  let data = @yamlstar.load("answer: 42")
  match data {
    Object(object) =>
      match object.get("answer") {
        Some(Number(value, ..)) => @test.assert_eq(value, 42.0)
        _ => @test.fail("unexpected test value")
      }
    _ => @test.fail("unexpected data value")
  }
}

///|
test "load plain YAML" {
  let data = @yamlstar.load("foo: bar")
  match data {
    Object(object) =>
      match object.get("foo") {
        Some(String(value)) => @test.assert_eq(value, "bar")
        _ => @test.fail("unexpected foo value")
      }
    _ => @test.fail("unexpected data value")
  }
}

///|
test "load error raises" {
  @test.assert_raise(() => @yamlstar.load("key: \"unclosed"))
}
