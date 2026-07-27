package main

import (
	"encoding/json"
	"fmt"
	"os"

	"github.com/yaml/yamlstar"
)

func main() {
	yamlFile := "../sample.yaml"
	if len(os.Args) > 1 {
		yamlFile = os.Args[1]
	}

	fmt.Printf("YAMLStar Example - Loading %s and outputting JSON\n\n", yamlFile)

	yamlContent, err := os.ReadFile(yamlFile)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error reading file: %v\n", err)
		os.Exit(1)
	}

	fmt.Println("Input YAML:")
	fmt.Println(string(yamlContent))
	fmt.Print("\n---\n\n")

	data, err := yamlstar.Load(string(yamlContent))
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error parsing YAML: %v\n", err)
		os.Exit(1)
	}

	jsonOutput, err := json.MarshalIndent(data, "", "  ")
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error encoding JSON: %v\n", err)
		os.Exit(1)
	}

	fmt.Println("Output JSON:")
	fmt.Println(string(jsonOutput))
}
