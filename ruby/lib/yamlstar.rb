# frozen_string_literal: true

require "fiddle"
require "fiddle/import"
require "json"

require_relative "yamlstar/version"

class YAMLStar
  Error = Class.new(StandardError)

  LIBYAMLSTAR_VERSION = VERSION

  module LibYAMLStar
    extend Fiddle::Importer

    def self.extension
      case RUBY_PLATFORM
      when /darwin/
        "dylib"
      when /linux/
        "so"
      when /mswin|mingw|cygwin/
        "dll"
      else
        raise Error, "Unsupported platform #{RUBY_PLATFORM} for yamlstar."
      end
    end

    def self.library_names
      base = "libyamlstar.#{extension}"
      [base, "#{base}.#{LIBYAMLSTAR_VERSION}"]
    end

    def self.library_paths
      paths = []
      dev_path = File.expand_path("../../libyamlstar/lib", __dir__)
      paths << dev_path
      env_var = extension == "dll" ? "PATH" : "LD_LIBRARY_PATH"
      paths.concat(ENV.fetch(env_var, "").split(File::PATH_SEPARATOR))
      paths << "/usr/local/lib" unless extension == "dll"
      paths << File.join(ENV.fetch("HOME", ""), ".local", "lib")
      paths.reject(&:empty?)
    end

    def self.find_library
      library_paths.each do |dir|
        library_names.each do |name|
          path = File.join(dir, name)
          return path if File.exist?(path)
        end
      end

      raise Error, <<~ERROR
        Shared library file `libyamlstar.#{extension}` not found
        Search paths: #{library_paths.join(File::PATH_SEPARATOR)}
        Try: curl -sSL https://yamlstar.org/install | LIB=1 bash
      ERROR
    end

    dlload find_library

    extern "int graal_create_isolate(void* params, void** isolate, void** thread)"
    extern "int graal_tear_down_isolate(void* thread)"
    extern "char* yamlstar_load(void* thread, char* yaml)"
    extern "char* yamlstar_load_all(void* thread, char* yaml)"
    extern "char* yamlstar_dump(void* thread, char* json)"
    extern "char* yamlstar_dump_all(void* thread, char* json)"
    extern "char* yamlstar_version(void* thread)"
  end

  def self.load(yaml)
    new.load(yaml)
  end

  def self.load_all(yaml)
    new.load_all(yaml)
  end

  def self.dump(value)
    new.dump(value)
  end

  def self.dump_all(values)
    new.dump_all(values)
  end

  attr_reader :error

  def initialize
    @isolate = Fiddle::Pointer.malloc(Fiddle::SIZEOF_VOIDP)
    thread = Fiddle::Pointer.malloc(Fiddle::SIZEOF_VOIDP)
    rc = LibYAMLStar.graal_create_isolate(nil, @isolate.ref, thread.ref)
    raise Error, "Failed to create GraalVM isolate" unless rc.zero?

    @thread = thread
    @closed = false
  end

  def load(yaml)
    call_yaml(:yamlstar_load, yaml)
  end

  def load_all(yaml)
    call_yaml(:yamlstar_load_all, yaml)
  end

  def dump(value)
    call_json(:yamlstar_dump, value)
  end

  def dump_all(values)
    call_json(:yamlstar_dump_all, values)
  end

  def version
    LibYAMLStar.yamlstar_version(@thread).to_s
  end

  def close
    return if @closed

    rc = LibYAMLStar.graal_tear_down_isolate(@thread)
    raise Error, "Failed to tear down GraalVM isolate" unless rc.zero?

    @closed = true
  end

  private

  def call_yaml(function, input)
    handle_response(LibYAMLStar.public_send(function, @thread, input.to_s).to_s)
  end

  def call_json(function, value)
    handle_response(LibYAMLStar.public_send(function, @thread, JSON.generate(value)).to_s)
  end

  def handle_response(json)
    resp = JSON.parse(json)
    @error = resp["error"]
    raise Error, "libyamlstar: #{@error["cause"]}" if @error
    raise Error, "Unexpected response from 'libyamlstar'" unless resp.key?("data")

    resp["data"]
  end
end
