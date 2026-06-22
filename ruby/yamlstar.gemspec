# frozen_string_literal: true

require_relative "lib/yamlstar/version"

Gem::Specification.new do |spec|
  spec.name = "yamlstar"
  spec.version = YAMLStar::VERSION
  spec.authors = ["Ingy dot Net"]
  spec.email = ["ingy@ingy.net"]

  spec.summary = "Ruby binding for YAMLStar"
  spec.description = "Ruby binding for YAMLStar, a YAML 1.2 load/dump framework."
  spec.homepage = "https://yamlstar.org"
  spec.license = "MIT"
  spec.required_ruby_version = ">= 2.7.0"

  spec.metadata["homepage_uri"] = spec.homepage
  spec.metadata["source_code_uri"] = "https://github.com/yaml/yamlstar"

  spec.files = Dir.chdir(File.expand_path(__dir__)) do
    Dir["lib/**/*.rb", "ReadMe.md", "Gemfile", "yamlstar.gemspec"]
  end
  spec.require_paths = ["lib"]
end
