require "json"

require "./yamlstar/version"

class YAMLStar
  class Error < Exception; end

  LIBYAMLSTAR_VERSION = VERSION

  module LibYAMLStar
    @[Link("yamlstar")]
    lib Lib
      fun graal_create_isolate(
        params : Void*,
        isolate : Void**,
        thread : Void**,
      ) : Int32
      fun graal_tear_down_isolate(thread : Void*) : Int32
      fun yamlstar_load(thread : Void*, input : LibC::Char*) : LibC::Char*
      fun yamlstar_load_all(thread : Void*, input : LibC::Char*) : LibC::Char*
      fun yamlstar_dump(thread : Void*, input : LibC::Char*) : LibC::Char*
      fun yamlstar_dump_all(thread : Void*, input : LibC::Char*) : LibC::Char*
      fun yamlstar_version(thread : Void*) : LibC::Char*
    end
  end

  def self.load(input : String)
    new.load(input)
  end

  def self.load_all(input : String)
    new.load_all(input)
  end

  def self.dump(value)
    new.dump(value)
  end

  def self.dump_all(values)
    new.dump_all(values)
  end

  getter error : JSON::Any?

  def initialize
    @isolate = Pointer(Void).malloc(1)
    @thread = Pointer(Void).malloc(1)
    @closed = false

    if LibYAMLStar::Lib.graal_create_isolate(nil, pointerof(@isolate), pointerof(@thread)) != 0
      raise Error.new("Failed to create GraalVM isolate")
    end
  end

  def load(input : String)
    call_yaml(:yamlstar_load, input)
  end

  def load_all(input : String)
    call_yaml(:yamlstar_load_all, input)
  end

  def dump(value)
    call_json(:yamlstar_dump, value)
  end

  def dump_all(values)
    call_json(:yamlstar_dump_all, values)
  end

  def version
    String.new(LibYAMLStar::Lib.yamlstar_version(@thread))
  end

  def close
    return if @closed

    if LibYAMLStar::Lib.graal_tear_down_isolate(@thread) != 0
      raise Error.new("Failed to tear down GraalVM isolate")
    end
    @closed = true
  end

  private def call_yaml(function, input)
    json_ptr = case function
               when :yamlstar_load
                 LibYAMLStar::Lib.yamlstar_load(@thread, input.to_unsafe)
               when :yamlstar_load_all
                 LibYAMLStar::Lib.yamlstar_load_all(@thread, input.to_unsafe)
               else
                 raise Error.new("Unknown libyamlstar function")
               end
    handle_response(String.new(json_ptr))
  end

  private def call_json(function, value)
    json = value.to_json
    json_ptr = case function
               when :yamlstar_dump
                 LibYAMLStar::Lib.yamlstar_dump(@thread, json.to_unsafe)
               when :yamlstar_dump_all
                 LibYAMLStar::Lib.yamlstar_dump_all(@thread, json.to_unsafe)
               else
                 raise Error.new("Unknown libyamlstar function")
               end
    handle_response(String.new(json_ptr))
  end

  private def handle_response(json)
    resp = JSON.parse(json).as_h
    @error = resp["error"]?
    raise Error.new("libyamlstar: #{@error.not_nil!["cause"]}") if @error
    raise Error.new("Unexpected response from 'libyamlstar'") unless resp.has_key?("data")

    resp["data"]
  end
end
