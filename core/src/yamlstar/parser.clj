(ns yamlstar.parser
  "YAMLStar parser compatibility facade.

  The default parser is the pure Clojure reference parser, registered
  as the \"reference\" parser plugin. Other parsers can be selected per
  call via the opts map (see yamlstar.plugin), or by generated runtimes with
  set-default-parser!."
  (:require [yamlstar.plugin :as plugin]))

(defn register-parsers!
  "Register the named parser plugins whose namespaces are already loaded.

  Each plugin namespace self-registers with a top-level form, but AOT
  compiled runtimes (Glojure) drop top-level side effects, so generated
  runtimes call this explicitly after loading their plugin namespaces.
  Returns the registered plugin maps in the given order."
  [& names]
  (mapv #(plugin/register-parser!
          (plugin/resolve-parser %)) names))

(defn register-reference-parser!
  "Register the built-in reference parser plugin.

  Generated runtimes call this after loading their reference parser plugin."
  []
  (first (register-parsers! "reference")))

(def ^:private fallback-default-parser (atom "reference"))

(defn set-default-parser!
  "Set the runtime fallback parser name.

  Per-call options have precedence over this fallback."
  [name]
  (reset! fallback-default-parser name)
  name)

(defn- current-default-parser
  []
  (or @fallback-default-parser
      "reference"))

(defn parse
  "Parse a YAML string into an event stream.

  Args:
    yaml-str: A string containing YAML content
    opts: (optional) Options map with an optional :parser plugin

  Returns:
    A sequence of event maps representing the YAML structure"
  ([yaml-str]
   (parse yaml-str nil))
  ([yaml-str opts]
   (let [yaml-str (if-let [comments (plugin/json-comments-opts opts)]
                    (plugin/sanitize-with comments yaml-str)
                    yaml-str)
         tabs (plugin/tab-indent-config opts)
         native-tabs (and tabs
                          (contains? #{"auto" "tabs"} (:load tabs)))
         [pname config]
         (or (plugin/parser-opts opts)
             [(current-default-parser) {}])]
     (when (and native-tabs (not= pname "go-yaml"))
       (throw
        (ex-info
         "tab-indent loading requires the native go-yaml parser"
         {:parser pname})))
     (plugin/parse-with
      pname (cond-> config tabs (assoc :tab-indent tabs)) yaml-str
      (true? (:plugin-install opts))))))
