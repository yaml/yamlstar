(ns yamlstar.plugin.yaml-emitter
  "Helpers for building YAML emitter plugin options."
  (:refer-clojure :exclude [name]))

(defn name
  "Select a YAML emitter plugin by name."
  [emitter-name]
  {:yaml-emitter {:name emitter-name}})

(defn reference [] (name "reference"))
(defn snakeyaml [] (name "snakeyaml"))
(defn go-yaml [] (name "go-yaml"))
