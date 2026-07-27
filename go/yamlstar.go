// Copyright 2024 yaml.org
// MIT License

// Package yamlstar provides the compatibility import path for YAMLStar's
// pure-Go YAML 1.2 loader and dumper.
package yamlstar

import core "github.com/yaml/yamlstar"

// Version is the YAMLStar module version.
const Version = core.Version

// ErrNotInitialized is retained for compatibility with older releases.
var ErrNotInitialized = core.ErrNotInitialized

// ErrNullResponse is retained for compatibility with older releases.
var ErrNullResponse = core.ErrNullResponse

// YAMLError represents an error returned by YAMLStar.
type YAMLError = core.YAMLError

// Load parses a YAML string and returns its first document as a Go value.
func Load(input string) (any, error) {
	return core.Load(input)
}

// LoadAll parses a YAML stream and returns all its documents.
func LoadAll(input string) ([]any, error) {
	return core.LoadAll(input)
}

// Dump serializes a JSON-compatible Go value as YAML.
func Dump(value any) (string, error) {
	return core.Dump(value)
}

// DumpAll serializes JSON-compatible Go values as a YAML stream.
func DumpAll(values []any) (string, error) {
	return core.DumpAll(values)
}

// LibVersion returns the version reported by the YAMLStar runtime.
func LibVersion() (string, error) {
	return core.LibVersion()
}
