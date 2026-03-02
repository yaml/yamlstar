// Copyright 2024 yaml.org
// MIT License

// Package yamlstar provides Go bindings for the libyamlstar shared library,
// a pure YAML 1.2 loader implemented in Clojure.
//
// Basic usage:
//
//	data, err := yamlstar.Load("key: value")
//	if err != nil {
//	    log.Fatal(err)
//	}
//	// data is map[string]any{"key": "value"}
//
// Multi-document support:
//
//	docs, err := yamlstar.LoadAll("---\ndoc1\n---\ndoc2")
//	if err != nil {
//	    log.Fatal(err)
//	}
//	// docs is []any{"doc1", "doc2"}
package yamlstar

/*
#cgo LDFLAGS: -lyamlstar

#include <libyamlstar.h>
#include <stdlib.h>
*/
import "C"

import (
	"encoding/json"
	"errors"
	"fmt"
	"unsafe"
)

// Version is the version of the yamlstar library this binding works with.
const Version = "0.1.3"

// ErrNullResponse is returned when the C function returns a null pointer.
var ErrNullResponse = errors.New("yamlstar: received null response from library")

// YAMLError represents an error returned from the yamlstar library.
type YAMLError struct {
	Cause string `json:"cause"`
	Type  string `json:"type"`
}

func (e *YAMLError) Error() string {
	if e.Cause != "" {
		return fmt.Sprintf("yamlstar: %s", e.Cause)
	}
	return fmt.Sprintf("yamlstar: %s", e.Type)
}

// response represents the JSON response from libyamlstar.
type response struct {
	Data  any        `json:"data"`
	Error *YAMLError `json:"error"`
}

// callResult calls a libyamlstar C function and parses the JSON result.
func callResult(cResult *C.char) (any, error) {
	if cResult == nil {
		return nil, ErrNullResponse
	}
	jsonResult := C.GoString(cResult)

	var resp response
	if err := json.Unmarshal([]byte(jsonResult), &resp); err != nil {
		return nil, fmt.Errorf("yamlstar: failed to parse response: %w", err)
	}

	if resp.Error != nil {
		return nil, resp.Error
	}

	return resp.Data, nil
}

// Load parses a YAML string and returns the first document as a Go value.
//
// The returned value will be one of:
//   - nil (for YAML null)
//   - bool (for YAML boolean)
//   - float64 (for YAML numbers - integers are also returned as float64)
//   - string (for YAML strings)
//   - []any (for YAML sequences)
//   - map[string]any (for YAML mappings)
//
// Returns an error if the YAML is malformed or the library is not initialized.
func Load(input string) (any, error) {
	cInput := C.CString(input)
	defer C.free(unsafe.Pointer(cInput))

	cOpts := C.CString("{}")
	defer C.free(unsafe.Pointer(cOpts))

	cResult := C.yamlstar_load(cInput, cOpts)
	return callResult(cResult)
}

// LoadAll parses a YAML string and returns all documents as a slice of Go values.
//
// Each element in the returned slice follows the same type mapping as Load().
//
// Returns an error if the YAML is malformed or the library is not initialized.
func LoadAll(input string) ([]any, error) {
	cInput := C.CString(input)
	defer C.free(unsafe.Pointer(cInput))

	cOpts := C.CString("{}")
	defer C.free(unsafe.Pointer(cOpts))

	cResult := C.yamlstar_load_all(cInput, cOpts)
	data, err := callResult(cResult)
	if err != nil {
		return nil, err
	}

	if data == nil {
		return nil, nil
	}
	docs, ok := data.([]any)
	if !ok {
		return nil, fmt.Errorf("yamlstar: unexpected response type for load_all: %T", data)
	}

	return docs, nil
}

// LibVersion returns the version string from the libyamlstar library.
func LibVersion() (string, error) {
	cResult := C.yamlstar_version()
	if cResult == nil {
		return "", ErrNullResponse
	}
	return C.GoString(cResult), nil
}
