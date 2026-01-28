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
#include <graal_isolate.h>
#include <stdlib.h>
*/
import "C"

import (
	"encoding/json"
	"errors"
	"fmt"
	"runtime"
	"unsafe"
)

// Version is the version of the yamlstar library this binding works with.
const Version = "0.1.3"

// ErrNotInitialized is returned when the library failed to initialize.
var ErrNotInitialized = errors.New("yamlstar: library not initialized")

// ErrNullResponse is returned when the C function returns a null pointer.
var ErrNullResponse = errors.New("yamlstar: received null response from library")

// GraalVM isolate - shared across all calls
var (
	isolate *C.graal_isolate_t
	initErr error
)

// YAMLError represents an error returned from the yamlstar library.
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

// response represents the JSON response from libyamlstar.
type response struct {
	Data  any        `json:"data"`
	Error *YAMLError `json:"error"`
}

// init initializes the GraalVM isolate.
// This is called automatically when the package is imported.
func init() {
	// Create the isolate without creating an initial thread
	// We'll attach threads as needed per-call
	rc := C.graal_create_isolate(nil, &isolate, nil)
	if rc != 0 {
		initErr = fmt.Errorf("yamlstar: failed to create GraalVM isolate (code %d)", int(rc))
	}
}

// ensureInitialized checks that the library was initialized successfully.
func ensureInitialized() error {
	if initErr != nil {
		return initErr
	}
	if isolate == nil {
		return ErrNotInitialized
	}
	return nil
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
	if err := ensureInitialized(); err != nil {
		return nil, err
	}

	// Lock OS thread for GraalVM native image calls
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()

	// Attach a thread for this call
	var thread *C.graal_isolatethread_t
	rc := C.graal_attach_thread(isolate, &thread)
	if rc != 0 {
		return nil, fmt.Errorf("yamlstar: failed to attach thread (code %d)", int(rc))
	}
	defer C.graal_detach_thread(thread)

	// Convert Go string to C string
	cInput := C.CString(input)
	defer C.free(unsafe.Pointer(cInput))

	// Call yamlstar_load function in libyamlstar
	cResult := C.yamlstar_load(
		(C.longlong)(uintptr(unsafe.Pointer(thread))),
		cInput,
	)
	if cResult == nil {
		return nil, ErrNullResponse
	}

	// Convert C string to Go string
	jsonResult := C.GoString(cResult)

	// Parse JSON response
	var resp response
	if err := json.Unmarshal([]byte(jsonResult), &resp); err != nil {
		return nil, fmt.Errorf("yamlstar: failed to parse response: %w", err)
	}

	// Check for error in response
	if resp.Error != nil {
		return nil, resp.Error
	}

	return resp.Data, nil
}

// LoadAll parses a YAML string and returns all documents as a slice of Go values.
//
// Each element in the returned slice follows the same type mapping as Load().
//
// Returns an error if the YAML is malformed or the library is not initialized.
func LoadAll(input string) ([]any, error) {
	if err := ensureInitialized(); err != nil {
		return nil, err
	}

	// Lock OS thread for GraalVM native image calls
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()

	// Attach a thread for this call
	var thread *C.graal_isolatethread_t
	rc := C.graal_attach_thread(isolate, &thread)
	if rc != 0 {
		return nil, fmt.Errorf("yamlstar: failed to attach thread (code %d)", int(rc))
	}
	defer C.graal_detach_thread(thread)

	// Convert Go string to C string
	cInput := C.CString(input)
	defer C.free(unsafe.Pointer(cInput))

	// Call yamlstar_load_all function in libyamlstar
	cResult := C.yamlstar_load_all(
		(C.longlong)(uintptr(unsafe.Pointer(thread))),
		cInput,
	)
	if cResult == nil {
		return nil, ErrNullResponse
	}

	// Convert C string to Go string
	jsonResult := C.GoString(cResult)

	// Parse JSON response
	var resp response
	if err := json.Unmarshal([]byte(jsonResult), &resp); err != nil {
		return nil, fmt.Errorf("yamlstar: failed to parse response: %w", err)
	}

	// Check for error in response
	if resp.Error != nil {
		return nil, resp.Error
	}

	// Convert to []any
	if resp.Data == nil {
		return nil, nil
	}
	docs, ok := resp.Data.([]any)
	if !ok {
		return nil, fmt.Errorf("yamlstar: unexpected response type for load_all: %T", resp.Data)
	}

	return docs, nil
}

// LibVersion returns the version string from the libyamlstar library.
//
// Returns an error if the library is not initialized.
func LibVersion() (string, error) {
	if err := ensureInitialized(); err != nil {
		return "", err
	}

	// Lock OS thread for GraalVM native image calls
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()

	// Attach a thread for this call
	var thread *C.graal_isolatethread_t
	rc := C.graal_attach_thread(isolate, &thread)
	if rc != 0 {
		return "", fmt.Errorf("yamlstar: failed to attach thread (code %d)", int(rc))
	}
	defer C.graal_detach_thread(thread)

	// Call yamlstar_version function in libyamlstar
	cResult := C.yamlstar_version(
		(C.longlong)(uintptr(unsafe.Pointer(thread))),
	)
	if cResult == nil {
		return "", ErrNullResponse
	}

	return C.GoString(cResult), nil
}
