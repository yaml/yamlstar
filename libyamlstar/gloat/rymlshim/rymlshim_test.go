package rymlshim

import (
	"strings"
	"testing"

	"github.com/glojurelang/glojure/pkg/lang"
)

func vectorEvents(t *testing.T, input string) []any {
	t.Helper()
	vec, err := parseEvents(input)
	if err != nil {
		t.Fatalf("parseEvents(%q) error: %v", input, err)
	}
	events := make([]any, vec.Count())
	for i := range events {
		events[i] = vec.Nth(i)
	}
	return events
}

func eventNames(t *testing.T, events []any) []string {
	t.Helper()
	names := make([]string, len(events))
	for i, evt := range events {
		name, ok := kwEvent.Invoke(evt).(string)
		if !ok {
			t.Fatalf("event %d has non-string name %T", i, kwEvent.Invoke(evt))
		}
		names[i] = name
	}
	return names
}

func TestParseEventsSimpleMapping(t *testing.T) {
	events := vectorEvents(t, "key: value")
	got := strings.Join(eventNames(t, events), " ")
	want := "stream_start document_start mapping_start scalar scalar mapping_end document_end stream_end"
	if got != want {
		t.Fatalf("event names:\n got: %s\nwant: %s", got, want)
	}

	key := kwValue.Invoke(events[3])
	value := kwValue.Invoke(events[4])
	if key != "key" || value != "value" {
		t.Fatalf("mapping scalar values: got %q => %q", key, value)
	}
}

func TestParseEventsArenaBackedScalar(t *testing.T) {
	events := vectorEvents(t, "\"\\L\"")
	if len(events) < 3 {
		t.Fatalf("too few events: %d", len(events))
	}
	value := kwValue.Invoke(events[2])
	if value != "\u2028" {
		t.Fatalf("arena-backed scalar value: got %q", value)
	}
	style := kwStyle.Invoke(events[2])
	if style != "double" {
		t.Fatalf("scalar style: got %q", style)
	}
}

func TestParseEventsInvalidYAMLReturnsError(t *testing.T) {
	_, err := parseEvents("[unterminated")
	if err == nil {
		t.Fatal("expected invalid YAML error")
	}
	if !strings.Contains(err.Error(), "missing terminating ]") {
		t.Fatalf("unexpected error: %v", err)
	}
}

func TestParseEventsReturnsGlojureVector(t *testing.T) {
	vec, err := parseEvents("[a, b]")
	if err != nil {
		t.Fatal(err)
	}
	var _ *lang.Vector = vec
	if vec.Count() == 0 {
		t.Fatal("empty event vector")
	}
}
