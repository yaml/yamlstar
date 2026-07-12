rockspec_format = "3.0"
package = "yamlstar"
version = "%VERSION%-%REVISION%"
source = {
   url = "https://github.com/yaml/yamlstar-lua/archive/refs/tags/v%VERSION%.tar.gz"
}
description = {
   summary = "Lua binding for YAMLStar",
   detailed = [[
      YAMLStar is a YAML 1.2 load/dump framework.
      This Lua binding provides an interface to the YAMLStar shared library.
   ]],
   license = "MIT",
   homepage = "https://yamlstar.org",
   maintainer = "Ingy dot Net <ingy@ingy.net>",
   labels = { "yaml", "yamlstar", "ffi", "binding" }
}
dependencies = {
   "lua >= 5.1",
   "cffi-lua >= 0.1.14",
   "lua-cjson >= 2.1.0"
}
build = {
   type = "builtin",
   modules = {
      yamlstar = "lib/yamlstar.lua"
   },
   copy_directories = { "test" },
   external_dependencies = {
      libyamlstar = "== %VERSION%"
   }
}
