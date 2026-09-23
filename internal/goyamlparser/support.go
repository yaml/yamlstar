package goyamlparser

import "fmt"

type Style uint32

type DepthKind string

const (
	DepthKindFlow  DepthKind = "flow"
	DepthKindBlock DepthKind = "block"
)

type DepthContext struct {
	Kind DepthKind
}

type TabIndentMode string

const (
	TabIndentModeAuto TabIndentMode = "auto"
	TabIndentModeTabs TabIndentMode = "tabs"
)

type TabIndentLoad string

const (
	TabIndentLoadTabs   TabIndentLoad = "tabs"
	TabIndentLoadSpaces TabIndentLoad = "spaces"
	TabIndentLoadAuto   TabIndentLoad = "auto"
)

type TabIndentDump string

const (
	TabIndentDumpTabs   TabIndentDump = "tabs"
	TabIndentDumpSpaces TabIndentDump = "spaces"
)

type TabIndentAuto string

const (
	TabIndentAutoDocument TabIndentAuto = "document"
	TabIndentAutoStream   TabIndentAuto = "stream"
)

type TabIndentConfig struct {
	Mode TabIndentMode
	Load TabIndentLoad
	Dump TabIndentDump
	Auto TabIndentAuto
}

func DefaultDepthCheck(depth int, ctx *DepthContext) error {
	const maxDepth = 10000
	if depth > maxDepth {
		return fmt.Errorf("exceeded max depth of %d", maxDepth)
	}
	return nil
}

type Composer struct {
	Parser Parser
}

func NewComposer(in []byte, options any) *Composer {
	parser := NewParser()
	parser.SetInputString(in)
	return &Composer{Parser: parser}
}

func (c *Composer) Destroy() {}

func (e *Event) Delete() {}
