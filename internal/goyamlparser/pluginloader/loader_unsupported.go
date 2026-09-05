//go:build !darwin && !freebsd && !linux

// Package pluginloader loads YAMLStar event-source shared libraries.
package pluginloader

import "fmt"

// Manifest reports that shared plugins are not supported on this platform.
func Manifest(api, name string) (string, error) {
	return "", unsupported(api, name)
}

// Parse reports that shared plugins are not supported on this platform.
func Parse(api, name, input, options string) (string, int64, error) {
	return "", 2, unsupported(api, name)
}

func unsupported(api, name string) error {
	return fmt.Errorf(
		"YAMLStar shared plugin %s=%s is not supported on this platform",
		api, name)
}
