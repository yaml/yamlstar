(ns yamlstar.parser
  "YAMLStar parser compatibility facade.

  The default parser is the pure Clojure reference parser, registered
  as the \"reference\" parser plugin. Other parsers can be selected per
  call via the opts map (see yamlstar.plugin), globally with the
  YAMLSTAR_PARSER environment variable, or by generated runtimes with
  set-default-parser!."
  (:require [yamlstar.plugin :as plugin]))

(defn register-reference-parser!
  "Register the built-in reference parser plugin.

  Generated runtimes call this after loading their reference parser plugin."
  []
  (plugin/register-parser! (plugin/resolve-parser "reference")))

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
   (let [[pname config]
         (or (plugin/parser-opts opts)
             [(current-default-parser) {}])]
     (plugin/parse-with pname config yaml-str))))
