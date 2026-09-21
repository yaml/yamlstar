package goyamlparser

import (
	"github.com/glojurelang/glojure/pkg/lang"
	reference "github.com/yamlstar/yamlstar-plugin-parser-reference/parser"
)

// ParseReferenceEvents parses YAML with the external reference parser and
// returns events in YAMLStar's persistent data representation.
func ParseReferenceEvents(input string) (any, error) {
	events, err := reference.Parse([]byte(input))
	if err != nil {
		return nil, err
	}
	items := make([]any, 0, len(events))
	for _, event := range events {
		items = append(items, referenceEvent(event))
	}
	return lang.NewVector(items...), nil
}

func referenceEvent(event reference.Event) any {
	values := []any{lang.NewKeyword("event"), event.Type}
	add := func(key, value string) {
		if value != "" {
			values = append(values, lang.NewKeyword(key), value)
		}
	}
	add("value", event.Value)
	add("anchor", event.Anchor)
	add("tag", event.Tag)
	add("style", event.Style)
	add("name", event.Name)
	add("version", event.Version)
	if event.Type == "mapping_start" || event.Type == "sequence_start" {
		values = append(values, lang.NewKeyword("flow"), event.Flow)
	}
	if event.Explicit {
		values = append(values, lang.NewKeyword("explicit"), true)
	}
	return lang.NewMap(values...)
}
