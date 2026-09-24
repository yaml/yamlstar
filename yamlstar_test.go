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

func TestPureGoCompactSequenceIndent(t *testing.T) {
	output, err := yamlstar.Dump(map[string]any{
		"foo": []any{"bar"},
	})
	if err != nil {
		t.Fatal(err)
	}
	if want := "foo:\n- bar\n"; output != want {
		t.Fatalf("Dump returned %q, want %q", output, want)
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
		yamlstar.WithPlugin(yamlstar.YAMLParser("reference")),
		yamlstar.WithPlugin(yamlstar.TabIndent()))
	if err == nil || !strings.Contains(
		err.Error(), "requires the native go-yaml parser") {
		t.Fatalf("unexpected error: %v", err)
	}
	value, err := yamlstar.Load(
		"root:\n  value: true\n",
		yamlstar.WithPlugin(yamlstar.YAMLParser("reference")),
		yamlstar.WithPlugin(yamlstar.TabIndent(
			yamlstar.TabIndentConfig{Load: yamlstar.TabIndentLoadSpaces})))
	if err != nil {
		t.Fatal(err)
	}
	want := map[string]any{"root": map[string]any{"value": true}}
	if !reflect.DeepEqual(value, want) {
		t.Fatalf("Load returned %#v, want %#v", value, want)
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
		yamlstar.TabIndentConfig{Auto: yamlstar.TabIndentAutoStream},
	))
	input := "---\nroot:\n\tvalue: true\n---\nroot:\n  value: true\n"
	if _, err := yamlstar.LoadAll(input, stream); err == nil {
		t.Fatal("stream scope accepted mixed document indentation")
	}
	spaces := yamlstar.WithPlugin(yamlstar.TabIndent(
		yamlstar.TabIndentConfig{
			Mode: yamlstar.TabIndentModeTabs,
			Load: yamlstar.TabIndentLoadSpaces,
			Dump: yamlstar.TabIndentDumpSpaces,
			Auto: yamlstar.TabIndentAutoStream,
		},
	))
	output, err := yamlstar.Dump(
		map[string]any{"root": map[string]any{"value": true}}, spaces)
	if err != nil {
		t.Fatal(err)
	}
	if output != "root:\n  value: true\n" {
		t.Fatalf("unexpected space-indented output: %q", output)
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

func BenchmarkDumpEmitter(b *testing.B) {
	value := map[string]any{
		"name":    "yamlstar",
		"enabled": true,
		"plugins": []any{
			map[string]any{"name": "yaml-parser", "default": "go-yaml"},
			map[string]any{"name": "yaml-emitter", "default": "go-yaml"},
		},
		"metadata": map[string]any{
			"version": 1,
			"tags":    []any{"yaml", "plugins", "benchmark"},
		},
	}
	for _, name := range []string{"reference", "go-yaml"} {
		b.Run(name, func(b *testing.B) {
			option := yamlstar.WithPlugin(yamlstar.YAMLEmitter(name))
			if _, err := yamlstar.Dump(value, option); err != nil {
				b.Fatal(err)
			}
			b.ReportAllocs()
			b.ResetTimer()
			for b.Loop() {
				if _, err := yamlstar.Dump(value, option); err != nil {
					b.Fatal(err)
				}
			}
		})
	}
}
