module libyamlstar

import Base.Libc: Libdl

const YAMLSTAR_VERSION = "0.1.10"
const libhandle = Ref{Ptr{Cvoid}}()
const graal_create_isolate_fptr = Ref{Ptr{Cvoid}}()
const graal_tear_down_isolate_fptr = Ref{Ptr{Cvoid}}()
const yamlstar_load_fptr = Ref{Ptr{Cvoid}}()
const yamlstar_load_all_fptr = Ref{Ptr{Cvoid}}()
const yamlstar_dump_fptr = Ref{Ptr{Cvoid}}()
const yamlstar_dump_all_fptr = Ref{Ptr{Cvoid}}()
const yamlstar_version_fptr = Ref{Ptr{Cvoid}}()

function create_isolate(isolate::Ref{Ptr{Cvoid}}, thread::Ref{Ptr{Cvoid}})
    init()
    rc = ccall(graal_create_isolate_fptr[],
               Cint, (Ptr{Cvoid}, Ptr{Ptr{Cvoid}}, Ptr{Ptr{Cvoid}}),
               C_NULL, isolate, thread)
    if rc != 0
        error("Failed to create GraalVM isolate")
    end
end

function tear_down_isolate(thread)
    rc = ccall(graal_tear_down_isolate_fptr[], Cint, (Ptr{Cvoid},), thread)
    if rc != 0
        error("Failed to tear down GraalVM isolate")
    end
end

function yamlstar_load(thread, input::String)
    ccall(yamlstar_load_fptr[], Cstring, (Ptr{Cvoid}, Cstring), thread, input)
end

function yamlstar_load_all(thread, input::String)
    ccall(yamlstar_load_all_fptr[], Cstring, (Ptr{Cvoid}, Cstring), thread, input)
end

function yamlstar_dump(thread, input::String)
    ccall(yamlstar_dump_fptr[], Cstring, (Ptr{Cvoid}, Cstring), thread, input)
end

function yamlstar_dump_all(thread, input::String)
    ccall(yamlstar_dump_all_fptr[], Cstring, (Ptr{Cvoid}, Cstring), thread, input)
end

function yamlstar_version(thread)
    ccall(yamlstar_version_fptr[], Cstring, (Ptr{Cvoid},), thread)
end

function init()
    if libhandle[] != C_NULL
        return
    end

    libpath = find_library()
    libhandle[] = Libdl.dlopen(libpath, Libdl.RTLD_LAZY | Libdl.RTLD_LOCAL)
    graal_create_isolate_fptr[] = Libdl.dlsym(libhandle[], :graal_create_isolate)
    graal_tear_down_isolate_fptr[] = Libdl.dlsym(libhandle[], :graal_tear_down_isolate)
    yamlstar_load_fptr[] = Libdl.dlsym(libhandle[], :yamlstar_load)
    yamlstar_load_all_fptr[] = Libdl.dlsym(libhandle[], :yamlstar_load_all)
    yamlstar_dump_fptr[] = Libdl.dlsym(libhandle[], :yamlstar_dump)
    yamlstar_dump_all_fptr[] = Libdl.dlsym(libhandle[], :yamlstar_dump_all)
    yamlstar_version_fptr[] = Libdl.dlsym(libhandle[], :yamlstar_version)
end

function find_library()
    names = ["libyamlstar.$(Libdl.dlext)", "libyamlstar.$(Libdl.dlext).$(YAMLSTAR_VERSION)"]
    paths = String[]

    devpath = normpath(joinpath(@__DIR__, "..", "..", "..", "libyamlstar", "lib"))
    push!(paths, devpath)
    append!(paths, split(get(ENV, "LD_LIBRARY_PATH", ""), ":", keepempty=false))
    push!(paths, "/usr/local/lib")
    push!(paths, joinpath(homedir(), ".local", "lib"))

    for path in paths
        for name in names
            candidate = joinpath(path, name)
            if isfile(candidate)
                return candidate
            end
        end
    end

    error("""
Shared library file `libyamlstar.$(Libdl.dlext)` not found
Try: curl -sSL https://yamlstar.org/install | LIB=1 bash
""")
end

end
