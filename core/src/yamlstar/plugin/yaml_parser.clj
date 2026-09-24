(ns yamlstar.plugin.yaml-parser
  "Helpers for building YAML parser plugin options."
  (:refer-clojure :exclude [name]))

(defn name
  "Select a YAML parser plugin by name."
  [parser-name]
  {:yaml-parser {:name parser-name}})

(defn reference [] (name "reference"))
(defn snakeyaml [] (name "snakeyaml"))
(defn go-yaml [] (name "go-yaml"))
