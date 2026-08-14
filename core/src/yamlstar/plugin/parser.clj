(ns yamlstar.plugin.parser
  "Helpers for building parser plugin options.")

(defn name
  "Select a parser plugin by name."
  [parser-name]
  {:parser {:name parser-name}})

(defn reference
  "Select the built-in reference parser plugin."
  []
  (name "reference"))

(defn snakeyaml
  "Select the SnakeYAML parser plugin."
  []
  (name "snakeyaml"))
