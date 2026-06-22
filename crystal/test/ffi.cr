require "../src/yamlstar"

ys = YAMLStar.new
raise "load failed" unless ys.load("key: value")["key"].as_s == "value"
raise "dump failed" unless ys.dump({"key" => "value"}) == "key: value\n"
ys.close

puts "ok - yamlstar crystal ffi"
