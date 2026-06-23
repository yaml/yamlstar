package rymlshim

/*
#cgo CXXFLAGS: -std=c++11 -I${SRCDIR}/../../../rapidyaml/src -I${SRCDIR}/../../../rapidyaml/src_extra -I${SRCDIR}/../../../rapidyaml/ext/c4core.src
#cgo LDFLAGS: -lstdc++
#include <stdlib.h>
#include "rymlshim.h"
*/
import "C"

import (
	"errors"
	"fmt"
	"unsafe"

	"github.com/glojurelang/glojure/pkg/glj"
	"github.com/glojurelang/glojure/pkg/lang"
)

var (
	kwEvent  = lang.NewKeyword("event")
	kwValue  = lang.NewKeyword("value")
	kwStyle  = lang.NewKeyword("style")
	kwFlow   = lang.NewKeyword("flow")
	kwAnchor = lang.NewKeyword("anchor")
	kwTag    = lang.NewKeyword("tag")
	kwName   = lang.NewKeyword("name")
)

// Install replaces yamlstar.parser/parse in the Gloat shared-library runtime.
func Install() {
	alterVarRoot := glj.Var("clojure.core", "alter-var-root")
	parseVar := glj.Var("yamlstar.parser", "parse")
	parseFn := lang.NewFnFunc1(func(input any) any {
		s, ok := input.(string)
		if !ok {
			panic(fmt.Errorf("yamlstar.parser/parse expected string, got %T", input))
		}
		events, err := parseEvents(s)
		if err != nil {
			panic(err)
		}
		return events
	})
	alterVarRoot.Invoke(parseVar, lang.NewFnFunc1(func(any) any {
		return parseFn
	}))
}

func parseEvents(input string) (*lang.Vector, error) {
	var ptr unsafe.Pointer
	if len(input) > 0 {
		ptr = C.CBytes([]byte(input))
		defer C.free(ptr)
	}

	result := C.yamlstar_ryml_parse_events((*C.char)(ptr), C.size_t(len(input)))
	defer C.yamlstar_ryml_free_result(&result)

	if result.ok == 0 {
		if result.error != nil {
			return nil, errors.New(C.GoString(result.error))
		}
		return nil, errors.New("unknown rapidyaml parse error")
	}

	events := unsafe.Slice((*int32)(unsafe.Pointer(result.events)), int(result.events_len))
	source := unsafe.Slice((*byte)(unsafe.Pointer(result.source)), int(result.source_len))
	arena := unsafe.Slice((*byte)(unsafe.Pointer(result.arena)), int(result.arena_len))

	out := make([]any, 0, len(events))
	var pendingAnchor any
	var pendingTag any

	takePending := func(kvs []any) []any {
		if pendingAnchor != nil {
			kvs = append(kvs, kwAnchor, pendingAnchor)
			pendingAnchor = nil
		}
		if pendingTag != nil {
			kvs = append(kvs, kwTag, pendingTag)
			pendingTag = nil
		}
		return kvs
	}

	for i := 0; i < len(events); i++ {
		evt := events[i]
		text := ""
		if evt&C.RYML_WSTR != 0 {
			if i+2 >= len(events) {
				return nil, fmt.Errorf("malformed rapidyaml event buffer at %d", i)
			}
			offset := int(events[i+1])
			length := int(events[i+2])
			region := source
			if evt&C.RYML_AREN != 0 {
				region = arena
			}
			if offset < 0 || length < 0 || offset > len(region) || length > len(region)-offset {
				return nil, fmt.Errorf("rapidyaml string slice out of bounds at %d", i)
			}
			text = string(region[offset : offset+length])
			i += 2
		}

		switch {
		case hasAll(evt, C.RYML_BSTR):
			out = append(out, event("stream_start"))
		case hasAll(evt, C.RYML_ESTR):
			out = append(out, event("stream_end"))
		case hasAll(evt, C.RYML_BDOC):
			out = append(out, event("document_start"))
		case hasAll(evt, C.RYML_EDOC):
			out = append(out, event("document_end"))
		case hasAll(evt, C.RYML_BMAP):
			kvs := []any{}
			if evt&C.RYML_FLOW != 0 {
				kvs = append(kvs, kwFlow, true)
			}
			out = append(out, event("mapping_start", takePending(kvs)...))
		case hasAll(evt, C.RYML_EMAP):
			out = append(out, event("mapping_end"))
		case hasAll(evt, C.RYML_BSEQ):
			kvs := []any{}
			if evt&C.RYML_FLOW != 0 {
				kvs = append(kvs, kwFlow, true)
			}
			out = append(out, event("sequence_start", takePending(kvs)...))
		case hasAll(evt, C.RYML_ESEQ):
			out = append(out, event("sequence_end"))
		case evt&C.RYML_ANCH != 0:
			pendingAnchor = text
		case evt&C.RYML_TAG != 0:
			pendingTag = text
		case evt&C.RYML_ALIA != 0:
			out = append(out, event("alias", kwName, text))
		case evt&C.RYML_SCLR != 0:
			kvs := []any{kwValue, text}
			if style := scalarStyle(evt); style != "" {
				kvs = append(kvs, kwStyle, style)
			}
			out = append(out, event("scalar", takePending(kvs)...))
		}
	}

	return lang.NewVector(out...), nil
}

func hasAll(evt int32, flags C.int) bool {
	f := int32(flags)
	return evt&f == f
}

func scalarStyle(evt int32) string {
	switch {
	case evt&C.RYML_SQUO != 0:
		return "single"
	case evt&C.RYML_DQUO != 0:
		return "double"
	case evt&C.RYML_LITL != 0:
		return "literal"
	case evt&C.RYML_FOLD != 0:
		return "folded"
	default:
		return ""
	}
}

func event(name string, kvs ...any) any {
	items := make([]any, 0, len(kvs)+2)
	items = append(items, kwEvent, name)
	items = append(items, kvs...)
	return lang.NewMap(items...)
}
