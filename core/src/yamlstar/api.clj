(ns yamlstar.api
  "YAMLStar shared API implementation - YAML 1.2 loader

  This is the main entry point for YAMLStar. It provides simple functions
  for loading YAML documents into Clojure data structures.

  Example:
    (load \"key: value\")
    ;=> {\"key\" \"value\"}

    (load-all \"---\\ndoc1\\n---\\ndoc2\")
    ;=> [\"doc1\" \"doc2\"]"
  (:refer-clojure :exclude [load])
  (:require [yamlstar.parser :as parser]
            [yamlstar.composer :as composer]
            [yamlstar.resolver :as resolver]
            [yamlstar.constructor :as constructor]
            [yamlstar.representer :as representer]
            [yamlstar.desolver :as desolver]
            [yamlstar.serializer :as serializer]
            [yamlstar.emitter :as emitter]
            [yamlstar.plugin :as plugin]))

(defn- load-alias-data-file
  [path opts]
  (let [opts (update opts :plugin dissoc :alias-data)
        nodes (-> (slurp path)
                  (parser/parse opts)
                  composer/compose-all
                  resolver/resolve-all)]
    (when-not (= 1 (count nodes))
      (throw (ex-info "Alias-Data file must contain exactly one document"
                      {:file path :documents (count nodes)})))
    (let [value (constructor/construct (first nodes))]
      (when-not (map? value)
        (throw (ex-info "Alias-Data file root must be a mapping"
                        {:file path :value value})))
      value)))

(defn- alias-data-context
  [opts]
  (plugin/alias-data-context
   opts
   {:load-file #(load-alias-data-file % opts)}))

(defn load
  "Parse a YAML string and return a Clojure data structure.

  Supports YAML 1.2 core schema with standard types:
  - Scalars: strings, integers, floats, booleans, null
  - Collections: maps (mappings) and vectors (sequences)
  - Anchors and aliases

  Args:
    yaml-str: A string containing YAML content
    opts: (optional) Options map; {:plugin {:parser {:name \"name\"}}}
          selects a YAML parser plugin

  Returns:
    A Clojure data structure representing the YAML document

  Throws:
    Exception if the YAML is malformed"
  ([yaml-str]
   (load yaml-str nil))
  ([yaml-str opts]
   (when yaml-str
     (let [context (alias-data-context opts)]
       (-> (parser/parse yaml-str opts)
           composer/compose
           resolver/resolve
           (constructor/construct context))))))

(defn load-all
  "Parse a multi-document YAML string and return a sequence of documents.

  YAML files can contain multiple documents separated by '---'.
  This function returns all documents as a sequence.

  Args:
    yaml-str: A string containing one or more YAML documents
    opts: (optional) Options map; {:plugin {:parser {:name \"name\"}}}
          selects a YAML parser plugin

  Returns:
    A sequence of Clojure data structures, one per YAML document

  Throws:
    Exception if the YAML is malformed"
  ([yaml-str]
   (load-all yaml-str nil))
  ([yaml-str opts]
   (when yaml-str
     (let [context (alias-data-context opts)]
       (-> (parser/parse yaml-str opts)
           composer/compose-all
           resolver/resolve-all
           (constructor/construct-all context))))))

(defn dump
  "Dump a supported native value to a YAML string.

  The optional opts map may select a :yaml-emitter plugin."
  ([value]
   (dump value nil))
  ([value opts]
   (-> value
       representer/represent
       desolver/desolve
       serializer/serialize
       (emitter/emit-with-options opts))))

(defn dump-all
  "Dump a sequence of supported native values to a YAML stream.

  The optional opts map may select a :yaml-emitter plugin."
  ([values]
   (dump-all values nil))
  ([values opts]
   (-> (mapv representer/represent values)
       desolver/desolve-all
       serializer/serialize-all
       (emitter/emit-with-options true opts))))

(defn version
  "Return the YAMLStar version string"
  []
  "0.1.22-SNAPSHOT")
