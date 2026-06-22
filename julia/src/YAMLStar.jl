module YAMLStar

import JSON

include("libyamlstar.jl")

mutable struct Runtime
    isolate::Ref{Ptr{Cvoid}}
    thread::Ref{Ptr{Cvoid}}
    error
    closed::Bool

    function Runtime()
        ys = new(Ref{Ptr{Cvoid}}(), Ref{Ptr{Cvoid}}(), nothing, false)
        ys.isolate[] = C_NULL
        ys.thread[] = C_NULL
        libyamlstar.create_isolate(ys.isolate, ys.thread)
        return ys
    end
end

function close(ys::Runtime)
    if !ys.closed
        libyamlstar.tear_down_isolate(ys.thread[])
        ys.closed = true
    end
    return nothing
end

function load(ys::Runtime, input::AbstractString)
    call_yaml(ys, libyamlstar.yamlstar_load, input)
end

function load_all(ys::Runtime, input::AbstractString)
    call_yaml(ys, libyamlstar.yamlstar_load_all, input)
end

function dump(ys::Runtime, value)
    call_json(ys, libyamlstar.yamlstar_dump, value)
end

function dump_all(ys::Runtime, values)
    call_json(ys, libyamlstar.yamlstar_dump_all, values)
end

function version(ys::Runtime)
    unsafe_string(libyamlstar.yamlstar_version(ys.thread[]))
end

function call_yaml(ys::Runtime, func::Function, input::AbstractString)
    handle_response(ys, unsafe_string(func(ys.thread[], String(input))))
end

function call_json(ys::Runtime, func::Function, value)
    handle_response(ys, unsafe_string(func(ys.thread[], JSON.json(value))))
end

function handle_response(ys::Runtime, json_src::AbstractString)
    resp = JSON.parse(json_src)
    ys.error = get(resp, "error", nothing)
    if ys.error !== nothing
        error("libyamlstar: " * get(ys.error, "cause", ""))
    end
    if haskey(resp, "data")
        return resp["data"]
    else
        error("Unexpected response from 'libyamlstar'")
    end
end

end
