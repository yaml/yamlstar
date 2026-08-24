// Copyright 2024 yaml.org
// MIT License

// Package yamlstar provides a pure-Go YAML 1.2 loader and dumper.
//
// YAMLStar is implemented in Clojure and compiled to Go by Gloat and Glojure.
// The generated runtime is included in this module; using this package does not
// require cgo or the libyamlstar shared library.
package yamlstar

import (
	"encoding/json"
	"errors"
	"fmt"
	"sync"

	_ "github.com/gloathub/gloat/ys/pkg/ys/json"
	"github.com/glojurelang/glojure/pkg/glj"
	"github.com/glojurelang/glojure/pkg/lang"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/libyamlstar"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/core"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/grammar"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/parser"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/prelude"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/receiver"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/api"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/composer"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/constructor"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/desolver"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/emitter"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/numbers"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/parser"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin/parser/go_yaml"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/representer"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/resolver"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/serializer"
)

// Version is the YAMLStar module version.
const Version = "0.1.18"

// ErrNotInitialized is retained for compatibility with the former cgo
// binding. Initialization failures are returned directly.
var ErrNotInitialized = errors.New("yamlstar: runtime not initialized")

// ErrNullResponse is returned when the generated runtime returns no response.
var ErrNullResponse = errors.New("yamlstar: received null response")

// YAMLError represents an error returned by YAMLStar.
type YAMLError struct {
	Cause   string `json:"cause"`
	Type    string `json:"type"`
	Message string `json:"message,omitempty"`
}

func (e *YAMLError) Error() string {
	if e.Message != "" {
		return fmt.Sprintf("yamlstar: %s: %s", e.Type, e.Message)
	}
	return fmt.Sprintf("yamlstar: %s", e.Cause)
}

type response struct {
	Data  any        `json:"data"`
	Error *YAMLError `json:"error"`
}

type options struct {
	parser string
}

// Option configures a YAML load or dump operation.
type Option func(*options)

// Plugin is a YAMLStar plugin option fragment.
type Plugin func(*options)

// Parser selects the parser plugin used for loading.
func Parser(name string) Plugin {
	return func(o *options) {
		o.parser = name
	}
}

// WithPlugin adds a plugin option fragment.
func WithPlugin(plugin Plugin) Option {
	return func(o *options) {
		plugin(o)
	}
}

// WithParser selects the parser plugin used for loading.
//
// Deprecated: use WithPlugin(Parser(name)).
func WithParser(name string) Option {
	return WithPlugin(Parser(name))
}

func applyOptions(opts []Option) options {
	var o options
	for _, opt := range opts {
		opt(&o)
	}
	return o
}

func (o options) json() (string, error) {
	cfg := map[string]any{}
	if o.parser != "" {
		cfg["plugin"] = map[string]any{
			"parser": map[string]any{"name": o.parser},
		}
	}
	data, err := json.Marshal(cfg)
	if err != nil {
		return "", fmt.Errorf("yamlstar: failed to encode options: %w", err)
	}
	return string(data), nil
}

// withParser is retained for internal tests that configure options directly.
func withParser(name string) Option {
	return func(o *options) {
		o.parser = name
	}
}

func optsJSON(opts []Option) (string, error) {
	return applyOptions(opts).json()
}

var (
	initializeOnce sync.Once
	initializeErr  error
)

var namespaces = []string{
	"ys.json",
	"yamlstar.numbers",
	"yaml-parser.prelude",
	"yaml-parser.core",
	"yamlstar.emitter",
	"yaml-parser.grammar",
	"yamlstar.api",
	"yamlstar.desolver",
	"libyamlstar",
	"yamlstar.plugin",
	"yamlstar.plugin.parser.go-yaml",
	"yaml-parser.parser",
	"yamlstar.composer",
	"yamlstar.parser",
	"yaml-parser.receiver",
	"yamlstar.representer",
	"yamlstar.serializer",
	"yamlstar.constructor",
	"yamlstar.resolver",
}

func initialize() error {
	initializeOnce.Do(func() {
		defer func() {
			if value := recover(); value != nil {
				initializeErr = panicError("initialize runtime", value)
			}
		}()

		require := glj.Var("clojure.core", "require")
		for _, namespace := range namespaces {
			require.Invoke(lang.NewSymbol(namespace))
		}
		glj.Var("yamlstar.parser", "register-reference-parser!").Invoke()
		glj.Var("yamlstar.parser", "set-default-parser!").Invoke("go-yaml")
	})
	return initializeErr
}

func invoke(name string, args ...any) (result any, err error) {
	if err := initialize(); err != nil {
		return nil, err
	}

	defer func() {
		if value := recover(); value != nil {
			result = nil
			err = panicError(name, value)
		}
	}()

	return glj.Var("libyamlstar", name).Invoke(args...), nil
}

func panicError(operation string, value any) error {
	if err, ok := value.(error); ok {
		return fmt.Errorf("yamlstar: %s: %w", operation, err)
	}
	return fmt.Errorf("yamlstar: %s: %v", operation, value)
}

func call(name string, input string, opts []Option) (*response, error) {
	optsStr, err := optsJSON(opts)
	if err != nil {
		return nil, err
	}

	value, err := invoke(name, int64(0), input, optsStr)
	if err != nil {
		return nil, err
	}
	if value == nil {
		return nil, ErrNullResponse
	}

	text, ok := value.(string)
	if !ok {
		return nil, fmt.Errorf(
			"yamlstar: unexpected response type from %s: %T", name, value)
	}

	var resp response
	if err := json.Unmarshal([]byte(text), &resp); err != nil {
		return nil, fmt.Errorf("yamlstar: failed to parse response: %w", err)
	}
	if resp.Error != nil {
		return nil, resp.Error
	}
	return &resp, nil
}

// Load parses a YAML string and returns its first document as a Go value.
//
// Values are represented as nil, bool, float64, string, []any, and
// map[string]any.
func Load(input string, opts ...Option) (any, error) {
	resp, err := call("yamlstar-load", input, opts)
	if err != nil {
		return nil, err
	}
	return resp.Data, nil
}

// LoadAll parses a YAML stream and returns all its documents.
func LoadAll(input string, opts ...Option) ([]any, error) {
	resp, err := call("yamlstar-load-all", input, opts)
	if err != nil {
		return nil, err
	}
	if resp.Data == nil {
		return nil, nil
	}
	documents, ok := resp.Data.([]any)
	if !ok {
		return nil, fmt.Errorf(
			"yamlstar: unexpected response type for load-all: %T", resp.Data)
	}
	return documents, nil
}

// Dump serializes a JSON-compatible Go value as YAML.
func Dump(value any, opts ...Option) (string, error) {
	data, err := json.Marshal(value)
	if err != nil {
		return "", fmt.Errorf("yamlstar: failed to encode dump input: %w", err)
	}
	return dumpJSON(data, false, opts)
}

// DumpAll serializes JSON-compatible Go values as a YAML stream.
func DumpAll(values []any, opts ...Option) (string, error) {
	data, err := json.Marshal(values)
	if err != nil {
		return "", fmt.Errorf(
			"yamlstar: failed to encode dump-all input: %w", err)
	}
	return dumpJSON(data, true, opts)
}

func dumpJSON(data []byte, all bool, opts []Option) (string, error) {
	name := "yamlstar-dump"
	if all {
		name = "yamlstar-dump-all"
	}
	resp, err := call(name, string(data), opts)
	if err != nil {
		return "", err
	}
	text, ok := resp.Data.(string)
	if !ok {
		return "", fmt.Errorf(
			"yamlstar: unexpected response type for dump: %T", resp.Data)
	}
	return text, nil
}

// LibVersion returns the version reported by the generated YAMLStar runtime.
//
// The name is retained for compatibility with the former shared-library
// binding.
func LibVersion() (string, error) {
	value, err := invoke("yamlstar-version", int64(0))
	if err != nil {
		return "", err
	}
	if value == nil {
		return "", ErrNullResponse
	}
	version, ok := value.(string)
	if !ok {
		return "", fmt.Errorf(
			"yamlstar: unexpected version response type: %T", value)
	}
	return version, nil
}
