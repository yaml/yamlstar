(ns yamlstar.plugin.parser.reference
  "Reference parser plugin for YAMLStar."
  (:require #?(:clj [yaml-parser.core :as ref-parser])
            [yamlstar.plugin :as plugin]))

(defn parse
  "Parse YAML with the YAML reference parser."
  [yaml-str _config]
  #?(:clj (ref-parser/parse yaml-str)
     :glj
     (let [[events error]
           (github.com:yaml:yamlstar:internal:goyamlparser.ParseReferenceEvents
            (or yaml-str ""))]
       (if (nil? error)
         events
         (throw error)))))

(def plugin
  {:name "reference"
   :version "0.2.5"
   :parse parse
   :default-config {}})

(plugin/register-parser! plugin)
