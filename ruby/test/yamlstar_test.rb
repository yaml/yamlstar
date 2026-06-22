# frozen_string_literal: true

require "minitest/autorun"
require "yamlstar"

class YAMLStarTest < Minitest::Test
  def setup
    @ys = YAMLStar.new
  end

  def teardown
    @ys.close
  end

  def test_load
    assert_equal({"key" => "value"}, @ys.load("key: value"))
    assert_equal(["a", "b"], @ys.load("- a\n- b\n"))
  end

  def test_load_all
    assert_equal(["doc1", {"a" => 1}], @ys.load_all("---\ndoc1\n---\na: 1\n"))
  end

  def test_dump
    assert_equal("key: value\n", @ys.dump({"key" => "value"}))
    value = {"items" => ["a", "b"], "flag" => true, "text" => "42"}
    assert_equal(value, @ys.load(@ys.dump(value)))
  end

  def test_dump_all
    assert_equal("---\ndoc1\n---\na: 1\n", @ys.dump_all(["doc1", {"a" => 1}]))
  end

  def test_version
    assert_match(/\A\d+\.\d+\.\d+(-SNAPSHOT)?\z/, @ys.version)
  end

  def test_error
    error = assert_raises(YAMLStar::Error) { @ys.load('key: "unclosed') }
    assert_match(/libyamlstar:/, error.message)
  end
end
