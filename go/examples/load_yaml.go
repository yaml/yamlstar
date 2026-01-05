// Copyright 2024 yaml.org
// MIT License

// Example demonstrating basic usage of the yamlstar library.
package main

import (
	"fmt"
	"log"

	"github.com/yaml/yamlstar/go"
)

func main() {
	// Example 1: Load simple key-value
	fmt.Println("Example 1: Load simple mapping")
	data, err := yamlstar.Load("key: value")
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("  Result: %v\n", data)

	// Example 2: Load structured data
	fmt.Println("\nExample 2: Load structured config")
	config, err := yamlstar.Load(`
host: localhost
port: 8080
debug: true
`)
	if err != nil {
		log.Fatal(err)
	}
	m := config.(map[string]any)
	fmt.Printf("  Host: %s\n", m["host"])
	fmt.Printf("  Port: %v\n", m["port"])
	fmt.Printf("  Debug: %v\n", m["debug"])

	// Example 3: Load sequence
	fmt.Println("\nExample 3: Load sequence")
	items, err := yamlstar.Load("- apple\n- banana\n- cherry")
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("  Items: %v\n", items)

	// Example 4: Load all documents
	fmt.Println("\nExample 4: Load multiple documents")
	docs, err := yamlstar.LoadAll("---\nfirst\n---\nsecond\n---\nthird")
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("  Documents: %v\n", docs)

	// Example 5: Get library version
	fmt.Println("\nExample 5: Library version")
	version, err := yamlstar.LibVersion()
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("  YAMLStar version: %s\n", version)
	fmt.Printf("  Go binding version: %s\n", yamlstar.Version)

	// Example 6: Error handling
	fmt.Println("\nExample 6: Error handling")
	_, err = yamlstar.Load(`key: "unclosed`)
	if err != nil {
		fmt.Printf("  Error (expected): %v\n", err)
	}
}
