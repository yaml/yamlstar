local yamlstar = require("yamlstar")

local function assert_equal(actual, expected, message)
  if type(expected) == "table" then
    local actual_json = require("cjson").encode(actual)
    local expected_json = require("cjson").encode(expected)
    if actual_json ~= expected_json then
      error(message .. ": expected " .. expected_json .. " got " .. actual_json)
    end
  elseif actual ~= expected then
    error(message .. ": expected " .. tostring(expected) .. " got " .. tostring(actual))
  end
end

local ys = yamlstar.new()

assert_equal(ys:load("key: value").key, "value", "load mapping")
assert_equal(ys:load("- a\n- b\n"), {"a", "b"}, "load sequence")
assert_equal(ys:load_all("---\ndoc1\n---\na: 1\n")[1], "doc1", "load all")
assert_equal(ys:dump({key = "value"}), "key: value\n", "dump mapping")
assert_equal(ys:dump_all({"doc1", {a = 1}}), "---\ndoc1\n---\na: 1\n", "dump all")
assert(ys:version():match("^%d+%.%d+%.%d+%-?%u*$"), "version")

local ok, err = pcall(function()
  ys:load('key: "unclosed')
end)
assert(not ok and err:match("libyamlstar:"), "malformed YAML error")

ys:close()
print("ok - yamlstar lua")
