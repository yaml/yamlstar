(ns yamlstar.options
  "Helpers for building YAMLStar options maps.")

(defn options
  "Return an empty YAMLStar options map."
  []
  {})

(defn add
  "Shallow-merge m into options."
  [options m]
  (merge options m))

(defn plugin
  "Add a plugin option fragment under :plugin."
  [options plugin-options]
  (update options :plugin merge plugin-options))
