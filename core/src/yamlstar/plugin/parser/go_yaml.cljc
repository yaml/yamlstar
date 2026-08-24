(ns yamlstar.plugin.parser.go-yaml
  "go-yaml parser plugin for YAMLStar.

  Wraps the parser-stage code from github.com/yaml/go-yaml and adapts its
  events to the standard YAMLStar event stream. This plugin is available only
  in the Glojure-generated YAMLStar runtime."
  (:require [clojure.string :as str]
            [yamlstar.plugin :as plugin]
            #?(:glj [ys.json :as json])))

(defn- require-glojure-runtime
  []
  #?(:glj nil
     :clj (throw (ex-info (str "go-yaml parser plugin is only available "
                               "through the Glojure YAMLStar runtime")
                          {:parser "go-yaml"}))))

(defn- keywordize-event-keys
  [x]
  (cond
    (map? x) (into {}
                   (map (fn [[k v]]
                          [(keyword (str/replace (name k) "_" "-"))
                           (keywordize-event-keys v)]))
                   x)
    (vector? x) (mapv keywordize-event-keys x)
    :else x))

(defn parse
  "Parse a YAML string into a YAMLStar event stream using go-yaml."
  [yaml-str _config]
  (require-glojure-runtime)
  #?(:glj
     (let [[events-json err]
           (github.com:yaml:yamlstar:internal:goyamlparser.ParseYAMLStarEvents
             (or yaml-str ""))]
       (if (nil? err)
         (keywordize-event-keys (json/load events-json))
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
