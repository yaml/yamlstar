local ffi
local loaded, cffi = pcall(require, "cffi")
if loaded then
  ffi = cffi
else
  loaded, cffi = pcall(require, "jit.ffi")
  if loaded then
    ffi = cffi
  end
end

if not ffi then
  error("FFI library not found. Install cffi-lua or use LuaJIT with FFI.")
end

local json = require("cjson")
local yamlstar_version = "0.1.10"

local function platform_extension()
  if ffi.os == "Linux" then
    return "so"
  elseif ffi.os == "OSX" then
    return "dylib"
  elseif ffi.os == "Windows" then
    return "dll"
  end
  error("Unsupported platform '" .. tostring(ffi.os) .. "' for yamlstar.")
end

local function file_exists(path)
  local file = io.open(path, "r")
  if file then
    file:close()
    return true
  end
  return false
end

local function split_paths(value)
  local paths = {}
  if value then
    for path in value:gmatch("[^:]+") do
      table.insert(paths, path)
    end
  end
  return paths
end

local function find_libyamlstar_path()
  local extension = platform_extension()
  local names = {
    "libyamlstar." .. extension,
    "libyamlstar." .. extension .. "." .. yamlstar_version,
  }
  local paths = split_paths(os.getenv("LD_LIBRARY_PATH"))
  table.insert(paths, "../libyamlstar/lib")
  table.insert(paths, "/usr/local/lib")
  local home = os.getenv("HOME")
  if home then
    table.insert(paths, home .. "/.local/lib")
  end

  for _, path in ipairs(paths) do
    for _, name in ipairs(names) do
      local full_path = path .. "/" .. name
      if file_exists(full_path) then
        return full_path
      end
    end
  end

  error(string.format([[
Shared library file 'libyamlstar.%s' not found
Try: curl -sSL https://yamlstar.org/install | LIB=1 bash
]], extension))
end

ffi.cdef[[
typedef struct graal_isolate_t graal_isolate_t;
typedef struct graal_isolatethread_t graal_isolatethread_t;

int graal_create_isolate(void* params, graal_isolate_t** isolate,
                        graal_isolatethread_t** thread);
int graal_tear_down_isolate(graal_isolatethread_t* thread);

const char* yamlstar_load(graal_isolatethread_t* thread, const char* s);
const char* yamlstar_load_all(graal_isolatethread_t* thread, const char* s);
const char* yamlstar_dump(graal_isolatethread_t* thread, const char* s);
const char* yamlstar_dump_all(graal_isolatethread_t* thread, const char* s);
const char* yamlstar_version(graal_isolatethread_t* thread);
]]

local libyamlstar = ffi.load(find_libyamlstar_path())

local YAMLStar = {}
YAMLStar.__index = YAMLStar

function YAMLStar.new()
  local self = setmetatable({}, YAMLStar)
  local isolate = ffi.new("graal_isolate_t*[1]")
  local thread = ffi.new("graal_isolatethread_t*[1]")
  local rc = libyamlstar.graal_create_isolate(nil, isolate, thread)
  if rc ~= 0 then
    error("Failed to create GraalVM isolate")
  end
  self.thread = thread[0]
  self.error = nil
  return self
end

local function handle_response(self, data_json)
  local resp = json.decode(ffi.string(data_json))
  self.error = resp.error
  if self.error then
    error("libyamlstar: " .. self.error.cause)
  end
  if resp.data == nil and data_json:find('"data"') == nil then
    error("Unexpected response from 'libyamlstar'")
  end
  return resp.data
end

function YAMLStar:load(input)
  return handle_response(self, libyamlstar.yamlstar_load(self.thread, input))
end

function YAMLStar:load_all(input)
  return handle_response(self, libyamlstar.yamlstar_load_all(self.thread, input))
end

function YAMLStar:dump(value)
  return handle_response(self, libyamlstar.yamlstar_dump(self.thread, json.encode(value)))
end

function YAMLStar:dump_all(values)
  return handle_response(self, libyamlstar.yamlstar_dump_all(self.thread, json.encode(values)))
end

function YAMLStar:version()
  return ffi.string(libyamlstar.yamlstar_version(self.thread))
end

function YAMLStar:close()
  if self.thread then
    local rc = libyamlstar.graal_tear_down_isolate(self.thread)
    if rc ~= 0 then
      error("Failed to tear down GraalVM isolate")
    end
    self.thread = nil
  end
end

local M = {}
M.YAMLStar = YAMLStar
M.new = YAMLStar.new

return M

