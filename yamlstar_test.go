// Copyright 2024 yaml.org
// MIT License

package yamlstar_test

import (
	"errors"
	"os"
	"path/filepath"
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

func TestPureGoAliasData(t *testing.T) {
	got, err := yamlstar.Load(
		"<<: *defaults\nsize: 5\n",
		yamlstar.WithPlugin(yamlstar.AliasData(yamlstar.AliasDataConfig{
			Data: map[string]any{
				"defaults": map[string]any{"color": "blue", "size": 3},
			}})),
	)
	if err != nil {
		t.Fatal(err)
	}
	want := map[string]any{"color": "blue", "size": float64(5)}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("got %#v, want %#v", got, want)
	}
}

func TestPureGoAliasDataSources(t *testing.T) {
	directory := t.TempDir()
	path := filepath.Join(directory, "aliases.yaml")
	if err := os.WriteFile(path, []byte("from_file: {x: 1}\n"), 0o600); err != nil {
		t.Fatal(err)
	}
	t.Setenv("YAMLSTAR_ALIAS_ENV", "environment")
	got, err := yamlstar.Load(
		"file: *from_file\nenv: *YAMLSTAR_ALIAS_ENV\n",
		yamlstar.WithPlugin(yamlstar.AliasData(yamlstar.AliasDataConfig{
			File: path,
			Env:  "YAMLSTAR_ALIAS_*",
		})),
	)
	if err != nil {
		t.Fatal(err)
	}
	want := map[string]any{
		"file": map[string]any{"x": float64(1)},
		"env":  "environment",
	}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("got %#v, want %#v", got, want)
	}
}

func TestPureGoAliasDataStream(t *testing.T) {
	input := "--- &saved {x: 1}\n---\ncopy: *saved\n"
	if _, err := yamlstar.LoadAll(input); err == nil {
		t.Fatal("expected document-scoped anchors by default")
	}
	got, err := yamlstar.LoadAll(
		input, yamlstar.WithPlugin(yamlstar.AliasData()))
	if err != nil {
		t.Fatal(err)
	}
	if len(got) != 2 {
		t.Fatalf("got %#v", got)
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
			yamlstar.IndentConfig{
				LoadStyle: yamlstar.IndentStyleSpaces,
			})))
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
		yamlstar.IndentConfig{Mode: yamlstar.IndentModeTabs},
	))
	if _, err := yamlstar.Load("root:\n  value: true\n", tabsOnly); err == nil {
		t.Fatal("tabs mode accepted space indentation")
	}
	stream := yamlstar.WithPlugin(yamlstar.TabIndent(
		yamlstar.IndentConfig{Scope: yamlstar.IndentScopeStream},
	))
	input := "---\nroot:\n\tvalue: true\n---\nroot:\n  value: true\n"
	if _, err := yamlstar.LoadAll(input, stream); err == nil {
		t.Fatal("stream scope accepted mixed document indentation")
	}
	spaces := yamlstar.WithPlugin(yamlstar.TabIndent(
		yamlstar.IndentConfig{
			Mode:      yamlstar.IndentModeTabs,
			LoadStyle: yamlstar.IndentStyleSpaces,
			DumpStyle: yamlstar.IndentStyleSpaces,
			Scope:     yamlstar.IndentScopeStream,
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

func TestTabIndentFlowWhitespace(t *testing.T) {
	tests := []struct {
		name   string
		input  string
		config yamlstar.IndentConfig
	}{
		{
			name: "spaces with tab mode",
			input: "items: [\n  one,\n  two\n]\n" +
				"root:\n\tvalue: true\n",
			config: yamlstar.IndentConfig{Mode: yamlstar.IndentModeTabs},
		},
		{
			name: "tabs with space loading",
			input: "items: [\n\tone,\n\ttwo\n]\n" +
				"root:\n  value: true\n",
			config: yamlstar.IndentConfig{
				LoadStyle: yamlstar.IndentStyleSpaces,
			},
		},
		{
			name: "spaces before automatic tabs",
			input: "items: [\n  one,\n  two\n]\n" +
				"root:\n\tvalue: true\n",
		},
		{
			name: "tabs before automatic spaces",
			input: "items: [\n\tone,\n\ttwo\n]\n" +
				"root:\n  value: true\n",
		},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			_, err := yamlstar.Load(
				test.input,
				yamlstar.WithPlugin(yamlstar.TabIndent(test.config)))
			if err != nil {
				t.Fatal(err)
			}
		})
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
			map[string]any{"name": "parser", "default": "go-yaml"},
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
