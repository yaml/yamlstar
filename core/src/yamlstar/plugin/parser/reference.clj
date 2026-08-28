(ns yamlstar.plugin.parser.reference
  "Reference parser plugin for YAMLStar."
  (:require [yaml-parser.core :as ref-parser]
            [yamlstar.plugin :as plugin]))

(def plugin
  {:name "reference"
   :parse (fn [yaml-str _config] (ref-parser/parse yaml-str))
   :default-config {}})

(plugin/register-parser! plugin)
