package goyamlparser

import (
	"fmt"
	"io"
	"strings"

	"github.com/glojurelang/glojure/pkg/lang"
)

var (
	kwEvent    = lang.NewKeyword("event")
	kwExplicit = lang.NewKeyword("explicit")
	kwVersion  = lang.NewKeyword("version")
	kwName     = lang.NewKeyword("name")
	kwValue    = lang.NewKeyword("value")
	kwStyle    = lang.NewKeyword("style")
	kwAnchor   = lang.NewKeyword("anchor")
	kwTag      = lang.NewKeyword("tag")
	kwFlow     = lang.NewKeyword("flow")
)

// Shape masks for node start events. The key order of every event map is
// event, anchor, tag, then value and style for scalars or flow for
// collections, so one shape per present-key combination keeps the maps
// keyword-shaped (one allocation, keyword lookups hit the shape cache in
// the composer) without adding nil-valued keys.
const (
	maskAnchor = 1
	maskTag    = 2
	maskExtra  = 4
)

var (
	scalarShapes     = makeShapes("value", "style")
	collectionShapes = makeShapes("", "flow")

	streamStartEvent   = lang.NewMap(kwEvent, "stream_start")
	streamEndEvent     = lang.NewMap(kwEvent, "stream_end")
	documentStartEvent = lang.NewMap(kwEvent, "document_start")
	documentEndEvent   = lang.NewMap(kwEvent, "document_end")
	mappingEndEvent    = lang.NewMap(kwEvent, "mapping_end")
	sequenceEndEvent   = lang.NewMap(kwEvent, "sequence_end")
	aliasShape         = lang.NewKeywordMapShape("event", "name")
)

// makeShapes builds the eight shapes for one event kind. The fixed key is
// always present after the optional anchor and tag; the extra key is
// present when maskExtra is set.
func makeShapes(fixed, extra string) [8]*lang.KeywordMapShape {
	var shapes [8]*lang.KeywordMapShape
	for mask := range shapes {
		names := []string{"event"}
		if mask&maskAnchor != 0 {
			names = append(names, "anchor")
		}
		if mask&maskTag != 0 {
			names = append(names, "tag")
		}
		if fixed != "" {
			names = append(names, fixed)
		}
		if mask&maskExtra != 0 {
			names = append(names, extra)
		}
		shapes[mask] = lang.NewKeywordMapShape(names...)
	}
	return shapes
}

func ParseYAMLStarEvents(input, load, auto string) (any, error) {
	if input == "" {
		input = "\n"
	} else if !strings.HasSuffix(input, "\n") {
		input += "\n"
	}

	parser := NewParser()
	if load != "" || auto != "" {
		if load == "" {
			load = string(TabIndentLoadAuto)
		}
		if auto == "" {
			auto = string(TabIndentAutoDocument)
		}
		if load != string(TabIndentLoadAuto) &&
			load != string(TabIndentLoadSpaces) &&
			load != string(TabIndentLoadTabs) {
			return nil, fmt.Errorf("invalid tab-indent load %q", load)
		}
		if auto != string(TabIndentAutoDocument) &&
			auto != string(TabIndentAutoStream) {
			return nil, fmt.Errorf("invalid tab-indent auto %q", auto)
		}
		parser.SetTabIndent(&TabIndentConfig{
			Load: TabIndentLoad(load), Auto: TabIndentAuto(auto),
		})
	}
	parser.SetInputString([]byte(input))
	defer parser.Delete()

	events := make([]any, 0, len(input)/8+8)
	for {
		var event Event
		err := parser.Parse(&event)
		if err == io.EOF {
			break
		}
		if err != nil {
			return nil, err
		}
		if item := yamlstarEvent(&event); item != nil {
			events = append(events, item)
		}
		if event.Type == STREAM_END_EVENT {
			break
		}
	}

	return lang.NewVector(events...), nil
}

func yamlstarEvent(event *Event) any {
	switch event.Type {
	case STREAM_START_EVENT:
		return streamStartEvent
	case STREAM_END_EVENT:
		return streamEndEvent
	case DOCUMENT_START_EVENT:
		version := event.GetVersionDirective()
		if event.Implicit && version == nil {
			return documentStartEvent
		}
		values := []any{kwEvent, "document_start"}
		if !event.Implicit {
			values = append(values, kwExplicit, true)
		}
		if version != nil {
			values = append(
				values,
				kwVersion,
				fmt.Sprintf("%d.%d", version.Major(), version.Minor()),
			)
		}
		return lang.NewMap(values...)
	case DOCUMENT_END_EVENT:
		if event.Implicit {
			return documentEndEvent
		}
		return lang.NewMap(kwEvent, "document_end", kwExplicit, true)
	case MAPPING_START_EVENT:
		flow := event.MappingStyle() == FLOW_MAPPING_STYLE
		return nodeStartEvent("mapping_start", event, flow)
	case MAPPING_END_EVENT:
		return mappingEndEvent
	case SEQUENCE_START_EVENT:
		flow := event.SequenceStyle() == FLOW_SEQUENCE_STYLE
		return nodeStartEvent("sequence_start", event, flow)
	case SEQUENCE_END_EVENT:
		return sequenceEndEvent
	case SCALAR_EVENT:
		return scalarEvent(event)
	case ALIAS_EVENT:
		return lang.NewStaticKeywordMap(
			aliasShape, "alias", string(event.Anchor))
	default:
		return nil
	}
}

// nodeValues starts the value slice of a node event with the event name
// and the optional anchor and tag, returning the shape mask so far.
func nodeValues(name string, event *Event, extra int) ([]any, int) {
	values := make([]any, 0, 3+extra)
	values = append(values, name)
	mask := 0
	if len(event.Anchor) > 0 {
		values = append(values, string(event.Anchor))
		mask |= maskAnchor
	}
	if tag := normalizeTag(string(event.Tag)); tag != "" {
		values = append(values, tag)
		mask |= maskTag
	}
	return values, mask
}

func nodeStartEvent(name string, event *Event, flow bool) any {
	values, mask := nodeValues(name, event, 1)
	if flow {
		values = append(values, true)
		mask |= maskExtra
	}
	return lang.NewStaticKeywordMap(collectionShapes[mask], values...)
}

func scalarEvent(event *Event) any {
	values, mask := nodeValues("scalar", event, 2)
	values = append(values, string(event.Value))
	if style := scalarStyle(event.ScalarStyle()); style != "" {
		values = append(values, style)
		mask |= maskExtra
	}
	return lang.NewStaticKeywordMap(scalarShapes[mask], values...)
}

func scalarStyle(style ScalarStyle) string {
	switch style {
	case SINGLE_QUOTED_SCALAR_STYLE:
		return "single"
	case DOUBLE_QUOTED_SCALAR_STYLE:
		return "double"
	case LITERAL_SCALAR_STYLE:
		return "literal"
	case FOLDED_SCALAR_STYLE:
		return "folded"
	default:
		return ""
	}
}

func normalizeTag(tag string) string {
	const prefix = "tag:yaml.org,2002:"
	if strings.HasPrefix(tag, prefix) {
		return "!!" + strings.TrimPrefix(tag, prefix)
	}
	return tag
}

func EmitYAMLStarEvents(events any, multi bool, dump string) (string, error) {
	if dump != "" && dump != string(TabIndentDumpSpaces) &&
		dump != string(TabIndentDumpTabs) {
		return "", fmt.Errorf("invalid tab-indent dump %q", dump)
	}
	var output []byte
	emitter := NewEmitter()
	emitter.SetOutputString(&output)
	emitter.SetUnicode(true)
	emitter.SetWidth(-1)
	emitter.BestIndent = 2
	emitter.CompactSequenceIndent = true
	emitter.tabIndent = dump == string(TabIndentDumpTabs)
	defer emitter.Delete()

	for items := lang.Seq(events); items != nil; items = items.Next() {
		event, err := yamlstarEmitterEvent(items.First(), multi)
		if err != nil {
			return "", err
		}
		if event.Type == STREAM_END_EVENT {
			emitter.OpenEnded = false
		}
		if err := emitter.Emit(&event); err != nil {
			return "", err
		}
	}
	return string(output), nil
}

func yamlstarEmitterEvent(value any, multi bool) (Event, error) {
	name, _ := lang.Get(value, kwEvent).(string)
	anchor, _ := lang.Get(value, kwAnchor).(string)
	tag, _ := lang.Get(value, kwTag).(string)
	text, _ := lang.Get(value, kwValue).(string)
	style, _ := lang.Get(value, kwStyle).(string)
	flow, _ := lang.Get(value, kwFlow).(bool)
	explicit, _ := lang.Get(value, kwExplicit).(bool)

	switch name {
	case "stream_start":
		return Event{Type: STREAM_START_EVENT, encoding: UTF8_ENCODING}, nil
	case "stream_end":
		return Event{Type: STREAM_END_EVENT}, nil
	case "document_start":
		var version *VersionDirective
		if text, ok := lang.Get(value, kwVersion).(string); ok {
			switch text {
			case "1.1":
				version = NewVersionDirective(1, 1)
			case "1.2":
				version = NewVersionDirective(1, 2)
			default:
				return Event{}, fmt.Errorf("unsupported YAML version %q", text)
			}
		}
		return Event{Type: DOCUMENT_START_EVENT,
			versionDirective: version, Implicit: !(multi || explicit)}, nil
	case "document_end":
		return Event{Type: DOCUMENT_END_EVENT, Implicit: !explicit}, nil
	case "mapping_start":
		style := BLOCK_MAPPING_STYLE
		if flow {
			style = FLOW_MAPPING_STYLE
		}
		return Event{Type: MAPPING_START_EVENT, Anchor: []byte(anchor),
			Tag: emitterTag(tag), Implicit: implicitTag(tag),
			Style: Style(style)}, nil
	case "mapping_end":
		return Event{Type: MAPPING_END_EVENT}, nil
	case "sequence_start":
		style := BLOCK_SEQUENCE_STYLE
		if flow {
			style = FLOW_SEQUENCE_STYLE
		}
		return Event{Type: SEQUENCE_START_EVENT, Anchor: []byte(anchor),
			Tag: emitterTag(tag), Implicit: implicitTag(tag),
			Style: Style(style)}, nil
	case "sequence_end":
		return Event{Type: SEQUENCE_END_EVENT}, nil
	case "scalar":
		implicit := implicitTag(tag)
		return Event{Type: SCALAR_EVENT, Anchor: []byte(anchor),
			Tag: emitterTag(tag), Value: []byte(text), Implicit: implicit,
			quoted_implicit: implicit,
			Style:           Style(emitterScalarStyle(style))}, nil
	case "alias":
		name, _ := lang.Get(value, kwName).(string)
		return Event{Type: ALIAS_EVENT, Anchor: []byte(name)}, nil
	default:
		return Event{}, fmt.Errorf("unknown YAMLStar event %q", name)
	}
}

func emitterScalarStyle(style string) ScalarStyle {
	switch style {
	case "single":
		return SINGLE_QUOTED_SCALAR_STYLE
	case "double":
		return DOUBLE_QUOTED_SCALAR_STYLE
	case "literal":
		return LITERAL_SCALAR_STYLE
	case "folded":
		return FOLDED_SCALAR_STYLE
	default:
		return PLAIN_SCALAR_STYLE
	}
}

func implicitTag(tag string) bool {
	return tag == "" || strings.HasPrefix(tag, "!!") ||
		strings.HasPrefix(tag, "tag:yaml.org,2002:")
}

func emitterTag(tag string) []byte {
	if strings.HasPrefix(tag, "!!") {
		return []byte("tag:yaml.org,2002:" + strings.TrimPrefix(tag, "!!"))
	}
	return []byte(tag)
}
