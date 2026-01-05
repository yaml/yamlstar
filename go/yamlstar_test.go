// Copyright 2024 yaml.org
// MIT License

package yamlstar_test

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/yaml/yamlstar/go"
)

func TestLoadSimpleScalar(t *testing.T) {
	data, err := yamlstar.Load("hello")
	require.NoError(t, err)
	assert.Equal(t, "hello", data)
}

func TestLoadInteger(t *testing.T) {
	data, err := yamlstar.Load("42")
	require.NoError(t, err)
	// JSON numbers are always float64 in Go
	assert.Equal(t, float64(42), data)
}

func TestLoadFloat(t *testing.T) {
	data, err := yamlstar.Load("3.14")
	require.NoError(t, err)
	assert.Equal(t, 3.14, data)
}

func TestLoadBooleanTrue(t *testing.T) {
	data, err := yamlstar.Load("true")
	require.NoError(t, err)
	assert.Equal(t, true, data)
}

func TestLoadBooleanFalse(t *testing.T) {
	data, err := yamlstar.Load("false")
	require.NoError(t, err)
	assert.Equal(t, false, data)
}

func TestLoadNull(t *testing.T) {
	data, err := yamlstar.Load("null")
	require.NoError(t, err)
	assert.Nil(t, data)
}

func TestLoadSimpleMapping(t *testing.T) {
	data, err := yamlstar.Load("key: value")
	require.NoError(t, err)
	assert.Equal(t, map[string]any{"key": "value"}, data)
}

func TestLoadNestedMapping(t *testing.T) {
	yaml := `
outer:
  inner: value
`
	data, err := yamlstar.Load(yaml)
	require.NoError(t, err)
	expected := map[string]any{
		"outer": map[string]any{
			"inner": "value",
		},
	}
	assert.Equal(t, expected, data)
}

func TestLoadMappingMultipleKeys(t *testing.T) {
	yaml := `
key1: value1
key2: value2
key3: value3
`
	data, err := yamlstar.Load(yaml)
	require.NoError(t, err)
	expected := map[string]any{
		"key1": "value1",
		"key2": "value2",
		"key3": "value3",
	}
	assert.Equal(t, expected, data)
}

func TestLoadSimpleSequence(t *testing.T) {
	yaml := `
- item1
- item2
- item3
`
	data, err := yamlstar.Load(yaml)
	require.NoError(t, err)
	assert.Equal(t, []any{"item1", "item2", "item3"}, data)
}

func TestLoadFlowSequence(t *testing.T) {
	data, err := yamlstar.Load("[a, b, c]")
	require.NoError(t, err)
	assert.Equal(t, []any{"a", "b", "c"}, data)
}

func TestLoadTypeCoercion(t *testing.T) {
	yaml := `
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
`
	data, err := yamlstar.Load(yaml)
	require.NoError(t, err)

	m, ok := data.(map[string]any)
	require.True(t, ok)

	assert.Equal(t, "hello", m["string"])
	assert.Equal(t, float64(42), m["integer"])
	assert.Equal(t, 3.14, m["float"])
	assert.Equal(t, true, m["bool_true"])
	assert.Equal(t, false, m["bool_false"])
	assert.Nil(t, m["null_value"])
}

func TestLoadSequenceOfMappings(t *testing.T) {
	yaml := `
- name: Alice
  age: 30
- name: Bob
  age: 25
`
	data, err := yamlstar.Load(yaml)
	require.NoError(t, err)
	expected := []any{
		map[string]any{"name": "Alice", "age": float64(30)},
		map[string]any{"name": "Bob", "age": float64(25)},
	}
	assert.Equal(t, expected, data)
}

func TestLoadMappingWithSequenceValues(t *testing.T) {
	yaml := `
fruits:
  - apple
  - banana
colors:
  - red
  - blue
`
	data, err := yamlstar.Load(yaml)
	require.NoError(t, err)
	expected := map[string]any{
		"fruits": []any{"apple", "banana"},
		"colors": []any{"red", "blue"},
	}
	assert.Equal(t, expected, data)
}

func TestLoadAllSingleDocument(t *testing.T) {
	data, err := yamlstar.LoadAll("hello")
	require.NoError(t, err)
	assert.Equal(t, []any{"hello"}, data)
}

func TestLoadAllMultipleDocuments(t *testing.T) {
	yaml := `---
doc1
---
doc2
---
doc3`
	data, err := yamlstar.LoadAll(yaml)
	require.NoError(t, err)
	assert.Equal(t, []any{"doc1", "doc2", "doc3"}, data)
}

func TestLoadAllWithExplicitMarkers(t *testing.T) {
	yaml := `---
a: 1
...
---
b: 2
...`
	data, err := yamlstar.LoadAll(yaml)
	require.NoError(t, err)
	expected := []any{
		map[string]any{"a": float64(1)},
		map[string]any{"b": float64(2)},
	}
	assert.Equal(t, expected, data)
}

func TestLibVersion(t *testing.T) {
	version, err := yamlstar.LibVersion()
	require.NoError(t, err)
	assert.NotEmpty(t, version)
}

func TestErrorHandlingMalformedYAML(t *testing.T) {
	// Unclosed quote is truly malformed
	_, err := yamlstar.Load(`key: "unclosed`)
	assert.Error(t, err)
}

func TestEmptyDocument(t *testing.T) {
	data, err := yamlstar.Load("")
	require.NoError(t, err)
	assert.Nil(t, data)
}

func TestWhitespaceOnly(t *testing.T) {
	data, err := yamlstar.Load("   \n  \n  ")
	require.NoError(t, err)
	assert.Nil(t, data)
}

func TestQuotedStrings(t *testing.T) {
	data1, err := yamlstar.Load("'hello world'")
	require.NoError(t, err)
	assert.Equal(t, "hello world", data1)

	data2, err := yamlstar.Load(`"hello world"`)
	require.NoError(t, err)
	assert.Equal(t, "hello world", data2)
}

func TestPackageVersion(t *testing.T) {
	assert.NotEmpty(t, yamlstar.Version)
}
