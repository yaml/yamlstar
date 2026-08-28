(ns yamlstar.plugin.parser.go-yaml
  "go-yaml parser plugin for YAMLStar.

  Wraps the parser-stage code from github.com/yaml/go-yaml and adapts its
  events to the standard YAMLStar event stream. This plugin is available only
  in the Glojure-generated YAMLStar runtime."
  (:require [yamlstar.plugin :as plugin]))

(defn- require-glojure-runtime
  []
  #?(:glj nil
     :clj (throw (ex-info (str "go-yaml parser plugin is only available "
                               "through the Glojure YAMLStar runtime")
                          {:parser "go-yaml"}))))

(defn parse
  "Parse a YAML string into a YAMLStar event stream using go-yaml."
  [yaml-str _config]
  (require-glojure-runtime)
  #?(:glj
     (let [[events err]
           (github.com:yaml:yamlstar:internal:goyamlparser.ParseYAMLStarEvents
             (or yaml-str ""))]
       (if (nil? err)
         events
         (throw err)))
     :clj
     (throw (ex-info (str "go-yaml parser plugin is only available through "
                          "the Glojure YAMLStar runtime")
                     {:parser "go-yaml"}))))

(def plugin
  {:name "go-yaml"
   :parse parse
   :default-config {}})

(plugin/register-parser! plugin)
