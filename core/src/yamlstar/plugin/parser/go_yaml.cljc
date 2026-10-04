(ns yamlstar.plugin.parser.go-yaml
  "go-yaml parser plugin for YAMLStar."
  (:require [yamlstar.plugin :as plugin]))

(defn parse
  [yaml-str config]
  #?(:glj
     (let [tabs (:tab-indent config)
           load (or (:load tabs) "")
           auto (or (:auto tabs) "")
           [events err]
           (github.com:yaml:yamlstar:internal:goyamlparser.ParseYAMLStarEvents
             (or yaml-str "") load auto)]
       (if (nil? err) events (throw err)))
     :clj
     (throw
      (ex-info
       "go-yaml parser plugin requires the Glojure YAMLStar runtime"
       {:parser "go-yaml"}))))

(def plugin
  {:name "go-yaml"
   :parse parse
   :default-config {}})

(plugin/register-parser! plugin)
