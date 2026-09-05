//go:build darwin || freebsd || linux

package pluginloader

import (
	"os"
	"path/filepath"
	"strings"
	"testing"
)

func TestLibraryFilename(t *testing.T) {
	name := libraryFilename("json-comments")
	if !strings.HasPrefix(name,
		"libyamlstar-plugin-json-comments.") {
		t.Fatalf("unexpected filename: %s", name)
	}
}

func TestSearchPathDoesNotIncludeWorkingDirectory(t *testing.T) {
	working, err := os.Getwd()
	if err != nil {
		t.Fatal(err)
	}
	for _, directory := range searchDirectories() {
		if directory == filepath.Clean(working) {
			t.Fatalf("search path includes working directory: %s", working)
		}
	}
}

func TestInvalidName(t *testing.T) {
	if err := validateName("../bad"); err == nil {
		t.Fatal("expected invalid name error")
	}
}
