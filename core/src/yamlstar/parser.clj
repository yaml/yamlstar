(ns yamlstar.parser
  "YAMLStar parser compatibility facade.

  The default parser is the pure Clojure reference parser, registered
  as the \"reference\" parser plugin. Other parsers can be selected per
  call via the opts map (see yamlstar.plugin), globally with the
  YAMLSTAR_PARSER environment variable, or by generated runtimes with
  set-default-parser!."
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

(def ^:private fallback-default-parser (atom "reference"))

(defn set-default-parser!
  "Set the runtime fallback parser name.

  YAMLSTAR_PARSER still has precedence over this fallback, and per-call
  options still have precedence over both."
  [name]
  (reset! fallback-default-parser name)
  name)

(defn- current-default-parser
  []
  (or (System/getenv "YAMLSTAR_PARSER")
      @fallback-default-parser
      "reference"))

(defn parse
  "Parse a YAML string into an event stream.

  Args:
    yaml-str: A string containing YAML content
    opts: (optional) Options map; {:plugin {:parser {:name \"name\"}}}
          selects a parser plugin

  Returns:
    A sequence of event maps representing the YAML structure

  Example event:
    {:event \"scalar\" :value \"hello\" :style \"plain\"}
    {:event \"mapping_start\" :flow false}"
  ([yaml-str]
   (parse yaml-str nil))
  ([yaml-str opts]
   (let [default-parser (current-default-parser)]
     (if-let [[pname config]
              (or (plugin/parser-opts opts)
                  (when (not= default-parser "reference")
                    [default-parser {}]))]
       (plugin/parse-with pname config yaml-str)
       (ref-parser/parse yaml-str)))))
