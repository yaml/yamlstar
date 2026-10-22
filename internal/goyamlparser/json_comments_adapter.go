package goyamlparser

import jsoncomments "github.com/yamlstar/yamlstar-plugin-parser-json-comments/parser"

// ParseJSONCommentsEvents returns events from the grammar extension.
func ParseJSONCommentsEvents(input string) (any, error) {
	events, err := jsoncomments.Parse([]byte(input))
	if err != nil {
		return nil, err
	}
	return referenceEvents(events), nil
}
