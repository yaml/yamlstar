package goyamlparser

import (
	"reflect"
	"testing"

	"github.com/glojurelang/glojure/pkg/lang"
)

func TestColonPrefixedFlowKeys(t *testing.T) {
	for _, test := range []struct {
		input string
		want  []string
	}{
		{"{:foo}", []string{":foo", ""}},
		{"{:foo: bar}", []string{":foo", "bar"}},
		{"{? :foo}", []string{":foo", ""}},
		{"{? :foo : bar}", []string{":foo", "bar"}},
		{"{? :foo: bar}", []string{":foo", "bar"}},
	} {
		t.Run(test.input, func(t *testing.T) {
			events, err := ParseYAMLStarEvents(test.input, "", "")
			if err != nil {
				t.Fatal(err)
			}
			var got []string
			for items := lang.Seq(events); items != nil; items = items.Next() {
				event := items.First()
				if lang.Get(event, kwEvent) == "scalar" {
					got = append(got, lang.Get(event, kwValue).(string))
				}
			}
			if !reflect.DeepEqual(got, test.want) {
				t.Fatalf("scalar values = %#v, want %#v", got, test.want)
			}
		})
	}
}
