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

func TestFindInstallerOverride(t *testing.T) {
	installer := filepath.Join(t.TempDir(), "yamlstar-plugin")
	if err := os.WriteFile(installer, []byte("#!/bin/sh\nexit 0\n"),
		0755); err != nil {
		t.Fatal(err)
	}
	t.Setenv("YAMLSTAR_PLUGIN_INSTALLER", installer)
	found, err := findInstaller()
	if err != nil {
		t.Fatal(err)
	}
	if found != installer {
		t.Fatalf("found installer %q, expected %q", found, installer)
	}
}

func TestFindInstallerRejectsBadOverride(t *testing.T) {
	installer := filepath.Join(t.TempDir(), "missing")
	t.Setenv("YAMLSTAR_PLUGIN_INSTALLER", installer)
	if _, err := findInstaller(); err == nil {
		t.Fatal("expected missing installer error")
	}
}

func TestPluginPathOverridesDefaults(t *testing.T) {
	t.Setenv("YAMLSTAR_LIBRARY_PATH", "first:second")
	directories := searchDirectories()
	if len(directories) != 2 || directories[0] != "first" ||
		directories[1] != "second" {
		t.Fatalf("unexpected plugin directories: %v", directories)
	}
}

func TestDefaultPathExcludesOverride(t *testing.T) {
	t.Setenv("YAMLSTAR_LIBRARY_PATH", "override")
	if path := DefaultPath(); strings.Contains(path, "override") {
		t.Fatalf("default path contains override: %s", path)
	}
}
