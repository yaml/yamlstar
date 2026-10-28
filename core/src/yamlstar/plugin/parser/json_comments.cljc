(ns yamlstar.plugin.parser.json-comments
  "JSON-comments parser plugin for YAMLStar."
  (:require #?(:clj [yamlstar-plugin.parser.json-comments :as ref-parser])
            [yamlstar.plugin :as plugin]))

(defn parse
  "Parse YAML with JSON-style comments."
  [yaml-str _config]
  #?(:clj (ref-parser/parse yaml-str)
     :glj
     (let [[events error]
           (github.com:yaml:yamlstar:internal:goyamlparser.ParseJSONCommentsEvents
            (or yaml-str ""))]
       (if (nil? error)
         events
         (throw error)))))

(def plugin
  {:name "json-comments"
   :version "0.1.1"
   :parse parse
   :default-config {}})

(plugin/register-parser! plugin)
