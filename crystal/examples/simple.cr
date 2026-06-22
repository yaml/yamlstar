require "../src/yamlstar"

ys = YAMLStar.new
puts ys.load("key: value")["key"]
ys.close
