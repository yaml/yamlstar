// Copyright 2024 yaml.org
// MIT License

package yamlstar_test

import (
	"errors"
	"reflect"
	"sync"
	"testing"

	"github.com/yaml/yamlstar"
)

func TestPureGoAPI(t *testing.T) {
	value, err := yamlstar.Load("name: Alice\nage: 42\n")
	if err != nil {
		t.Fatal(err)
	}
	want := map[string]any{
		"name": "Alice",
		"age":  float64(42),
	}
	if !reflect.DeepEqual(value, want) {
		t.Fatalf("Load returned %#v, want %#v", value, want)
	}

	documents, err := yamlstar.LoadAll("---\none\n---\ntwo\n")
	if err != nil {
		t.Fatal(err)
	}
	if want := []any{"one", "two"}; !reflect.DeepEqual(documents, want) {
		t.Fatalf("LoadAll returned %#v, want %#v", documents, want)
	}

	output, err := yamlstar.Dump(map[string]any{"key": "value"})
	if err != nil {
		t.Fatal(err)
	}
	if want := "key: value\n"; output != want {
		t.Fatalf("Dump returned %q, want %q", output, want)
	}

	stream, err := yamlstar.DumpAll([]any{"one", "two"})
	if err != nil {
		t.Fatal(err)
	}
	if want := "---\none\n---\ntwo\n"; stream != want {
		t.Fatalf("DumpAll returned %q, want %q", stream, want)
	}
}

func TestPureGoError(t *testing.T) {
	_, err := yamlstar.Load("[")
	if err == nil {
		t.Fatal("Load succeeded, want error")
	}
	var yamlErr *yamlstar.YAMLError
	if !errors.As(err, &yamlErr) {
		t.Fatalf("Load error is %T, want *yamlstar.YAMLError", err)
	}
}

func TestPureGoVersion(t *testing.T) {
	version, err := yamlstar.LibVersion()
	if err != nil {
		t.Fatal(err)
	}
	if version == "" {
		t.Fatal("LibVersion returned an empty version")
	}
}

func TestPureGoConcurrentLoads(t *testing.T) {
	const count = 16
	var wait sync.WaitGroup
	errs := make(chan error, count)

	for range count {
		wait.Add(1)
		go func() {
			defer wait.Done()
			value, err := yamlstar.Load("key: value")
			if err == nil && !reflect.DeepEqual(
				value, map[string]any{"key": "value"}) {
				err = errors.New("Load returned unexpected value")
			}
			errs <- err
		}()
	}

	wait.Wait()
	close(errs)
	for err := range errs {
		if err != nil {
			t.Error(err)
		}
	}
}
