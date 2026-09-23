// Copyright 2024 yaml.org
// MIT License

package yamlstar_test

import (
	"errors"
	"reflect"
	"strings"
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

func TestPureGoTabIndentPlugin(t *testing.T) {
	option := yamlstar.WithPlugin(yamlstar.TabIndent())
	value, err := yamlstar.Load(
		"root:\n\tchild:\n\t\tvalue: true\n", option)
	if err != nil {
		t.Fatal(err)
	}
	want := map[string]any{
		"root": map[string]any{
			"child": map[string]any{"value": true},
		},
	}
	if !reflect.DeepEqual(value, want) {
		t.Fatalf("Load returned %#v, want %#v", value, want)
	}
	output, err := yamlstar.Dump(want, option)
	if err != nil {
		t.Fatal(err)
	}
	if strings.Contains(output, "\n ") ||
		!strings.Contains(output, "\n\tchild:") ||
		!strings.Contains(output, "\n\t\tvalue: true") {
		t.Fatalf("unexpected tab-indented output:\n%s", output)
	}
}

func TestTabIndentNestedRoundTrip(t *testing.T) {
	option := yamlstar.WithPlugin(yamlstar.TabIndent())
	want := map[string]any{
		"steps": []any{
			map[string]any{
				"name": "checkout",
				"with": map[string]any{"ref": "main"},
			},
		},
	}
	output, err := yamlstar.Dump(want, option)
	if err != nil {
		t.Fatal(err)
	}
	if strings.Contains(output, "\n ") {
		t.Fatalf("found space indentation:\n%s", output)
	}
	got, err := yamlstar.Load(output, option)
	if err != nil {
		t.Fatalf("failed to reload:\n%s\n%v", output, err)
	}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("Load returned %#v, want %#v", got, want)
	}
}

func TestTabIndentRejectsReferenceParser(t *testing.T) {
	_, err := yamlstar.Load(
		"root:\n\tvalue: true\n",
		yamlstar.WithPlugin(yamlstar.Parser("reference")),
		yamlstar.WithPlugin(yamlstar.TabIndent()))
	if err == nil || !strings.Contains(
		err.Error(), "requires the native go-yaml parser") {
		t.Fatalf("unexpected error: %v", err)
	}
}

func TestTabIndentConfiguration(t *testing.T) {
	tabsOnly := yamlstar.WithPlugin(yamlstar.TabIndent(
		yamlstar.TabIndentConfig{Mode: yamlstar.TabIndentModeTabs},
	))
	if _, err := yamlstar.Load("root:\n  value: true\n", tabsOnly); err == nil {
		t.Fatal("tabs mode accepted space indentation")
	}
	stream := yamlstar.WithPlugin(yamlstar.TabIndent(
		yamlstar.TabIndentConfig{Scope: yamlstar.TabIndentScopeStream},
	))
	input := "---\nroot:\n\tvalue: true\n---\nroot:\n  value: true\n"
	if _, err := yamlstar.LoadAll(input, stream); err == nil {
		t.Fatal("stream scope accepted mixed document indentation")
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
