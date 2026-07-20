(ns yamlstar.parser
  "YAMLStar parser compatibility facade.

  The default parser is the pure Clojure reference parser, registered
  as the \"reference\" parser plugin. Other parsers can be selected per
  call via the opts map (see yamlstar.plugin) or globally with the
  YAMLSTAR_PARSER environment variable."
  (:require [yaml-parser.core :as ref-parser]
            [yamlstar.plugin :as plugin]))

(def reference-plugin
  "The pure Clojure reference parser plugin (the default)."
  {:name "reference"
   :parse (fn [yaml-str _config] (ref-parser/parse yaml-str))
   :default-config {}})

(defn register-reference-parser!
  "Register the built-in reference parser plugin.

  This is called at namespace load time on Clojure/JVM. Generated runtimes
  that do not preserve top-level side effects can call it explicitly after
  requiring this namespace."
  []
  (plugin/register-parser! reference-plugin))

(register-reference-parser!)

(def ^:private env-default-parser
  (delay (or (System/getenv "YAMLSTAR_PARSER") "reference")))

(defn parse
  "Parse a YAML string into an event stream.

  Args:
    yaml-str: A string containing YAML content
    opts: (optional) Options map; {:plugin {:parser {:use \"name\"}}}
          selects a parser plugin

  Returns:
    A sequence of event maps representing the YAML structure

  Example event:
    {:event \"scalar\" :value \"hello\" :style \"plain\"}
    {:event \"mapping_start\" :flow false}"
  ([yaml-str]
   (parse yaml-str nil))
  ([yaml-str opts]
   (if-let [[pname config]
            (or (plugin/parser-opts opts)
                (when (not= @env-default-parser "reference")
                  [@env-default-parser {}]))]
     (plugin/parse-with pname config yaml-str)
     (ref-parser/parse yaml-str))))
