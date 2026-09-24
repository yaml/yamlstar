(ns yamlstar.plugin.yaml-emitter.reference
  "Reference YAML emitter plugin for YAMLStar."
  (:require [yamlstar.emitter :as emitter]
            [yamlstar.plugin :as plugin]))

(def plugin
  {:name "reference"
   :emit (fn [events multi? _config]
           (emitter/emit events multi?))
   :default-config {}})

(plugin/register-yaml-emitter! plugin)
