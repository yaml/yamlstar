(ns yamlstar.plugin.yaml-emitter.go-yaml
  "go-yaml emitter plugin for YAMLStar."
  (:require [yamlstar.plugin :as plugin]))

(defn emit
  [events multi? config]
  #?(:glj
     (let [tabs (:tab-indent config)
           dump (or (:dump tabs) "")
           [output err]
           (github.com:yaml:yamlstar:internal:goyamlparser.EmitYAMLStarEvents
             events multi? dump)]
       (if (nil? err) output (throw err)))
     :clj
     (throw
      (ex-info
       "go-yaml emitter plugin requires the Glojure YAMLStar runtime"
       {:yaml-emitter "go-yaml"}))))

(def plugin
  {:name "go-yaml"
   :emit emit
   :default-config {}})

(plugin/register-yaml-emitter! plugin)
