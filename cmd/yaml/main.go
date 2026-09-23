package main

import (
	_ "github.com/gloathub/ys-v0-glj/clojure/data/json"
	_ "github.com/gloathub/ys-v0-glj/ys/json"
	_ "github.com/gloathub/ys-v0-glj/ys/v0/json"
	_ "github.com/gloathub/ys-v0-glj/ys/v0/util"
	"github.com/glojurelang/glojure/pkg/glj"
	"github.com/glojurelang/glojure/pkg/lang"
	_ "github.com/glojurelang/glojure/pkg/stdlib/clojure/string"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/libyamlstar"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/core"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/grammar"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/parser"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/prelude"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yaml_parser/receiver"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/api"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/cli"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/cli_default"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/cli_options"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/composer"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/constructor"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/contract"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/desolver"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/emitter"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/numbers"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/options"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/parser"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin/parser"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin/parser/go_yaml"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin/parser/reference"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin/shared"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/plugin/shared_host"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/representer"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/resolver"
	_ "github.com/yaml/yamlstar/internal/glojure/pkg/yamlstar/serializer"
	"os"
	"strings"
)

func main() {
	require := glj.Var("clojure.core", "require")
	require.Invoke(lang.NewSymbol("clojure.string"))
	require.Invoke(lang.NewSymbol("clojure.data.json"))
	require.Invoke(lang.NewSymbol("ys.v0.util"))
	require.Invoke(lang.NewSymbol("ys.v0.json"))
	require.Invoke(lang.NewSymbol("ys.json"))
	require.Invoke(lang.NewSymbol("libyamlstar"))
	require.Invoke(lang.NewSymbol("yaml-parser.core"))
	require.Invoke(lang.NewSymbol("yaml-parser.grammar"))
	require.Invoke(lang.NewSymbol("yaml-parser.parser"))
	require.Invoke(lang.NewSymbol("yaml-parser.prelude"))
	require.Invoke(lang.NewSymbol("yaml-parser.receiver"))
	require.Invoke(lang.NewSymbol("yamlstar.api"))
	require.Invoke(lang.NewSymbol("yamlstar.cli-default"))
	require.Invoke(lang.NewSymbol("yamlstar.cli-options"))
	require.Invoke(lang.NewSymbol("yamlstar.composer"))
	require.Invoke(lang.NewSymbol("yamlstar.constructor"))
	require.Invoke(lang.NewSymbol("yamlstar.contract"))
	require.Invoke(lang.NewSymbol("yamlstar.desolver"))
	require.Invoke(lang.NewSymbol("yamlstar.emitter"))
	require.Invoke(lang.NewSymbol("yamlstar.numbers"))
	require.Invoke(lang.NewSymbol("yamlstar.options"))
	require.Invoke(lang.NewSymbol("yamlstar.parser"))
	require.Invoke(lang.NewSymbol("yamlstar.plugin"))
	require.Invoke(lang.NewSymbol("yamlstar.plugin.parser"))
	require.Invoke(lang.NewSymbol("yamlstar.plugin.parser.go-yaml"))
	require.Invoke(lang.NewSymbol("yamlstar.plugin.parser.reference"))
	require.Invoke(lang.NewSymbol("yamlstar.plugin.shared"))
	require.Invoke(lang.NewSymbol("yamlstar.plugin.shared-host"))
	require.Invoke(lang.NewSymbol("yamlstar.representer"))
	require.Invoke(lang.NewSymbol("yamlstar.resolver"))
	require.Invoke(lang.NewSymbol("yamlstar.serializer"))
	require.Invoke(lang.NewSymbol("yamlstar.cli"))

	// Set up dynamic variables
	alterVarRoot := glj.Var("clojure.core", "alter-var-root")
	constantly := glj.Var("clojure.core", "constantly")

	// Set *ns* to the user's namespace using thread bindings
	nsObj := lang.FindOrCreateNamespace(lang.NewSymbol("yamlstar.cli"))
	nsStarVar := glj.Var("clojure.core", "*ns*")
	pushBindings := glj.Var("clojure.core", "push-thread-bindings")
	bindings := lang.NewMap(nsStarVar, nsObj)
	pushBindings.Invoke(bindings)

	args := os.Args[1:]
	anyArgs := make([]any, len(args))
	for i, arg := range args {
		anyArgs[i] = arg
	}

	// The YS globals exist only when the program links the YS runtime.
	if lang.FindNamespace(lang.NewSymbol("ys.v0.global")) != nil {
		// ENV: map of all environment variables
		environ := os.Environ()
		envPairs := make([]any, 0, len(environ)*2)
		for _, e := range environ {
			if idx := strings.IndexByte(e, '='); idx >= 0 {
				envPairs = append(envPairs, e[:idx], e[idx+1:])
			}
		}
		envVar := glj.Var("ys.v0.global", "ENV")
		alterVarRoot.Invoke(envVar, constantly.Invoke(lang.NewMap(envPairs...)))

		// CWD: current working directory
		cwd, _ := os.Getwd()
		cwdVar := glj.Var("ys.v0.global", "CWD")
		alterVarRoot.Invoke(cwdVar, constantly.Invoke(cwd))

		// RUN: runtime metadata map (includes args and pid)
		argsVec := lang.NewVector(anyArgs...)
		runMap := lang.NewMap(
			lang.NewKeyword("args"), argsVec,
			lang.NewKeyword("pid"), int64(os.Getpid()),
		)
		runVar := glj.Var("ys.v0.global", "RUN")
		alterVarRoot.Invoke(runVar, constantly.Invoke(runMap))
	}
	if lang.FindNamespace(lang.NewSymbol("ys.v0")) != nil {
		// NS: the user's namespace object
		nsVar := glj.Var("ys.v0", "NS")
		alterVarRoot.Invoke(nsVar, constantly.Invoke(nsObj))
	}

	// Load dependencies requested by portable use forms.

	// ARGV and ARGS are set in -main function itself
	// Call -main with args
	myMain := glj.Var("yamlstar.cli", "-main")
	myMain.Invoke(anyArgs...)
}
