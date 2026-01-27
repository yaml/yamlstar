(ns yamlstar.core
  "YAMLStar core API - YAML 1.2 loader

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
            [yamlstar.constructor :as constructor]))

(defn load
  "Parse a YAML string and return a Clojure data structure.

  Supports YAML 1.2 core schema with standard types:
  - Scalars: strings, integers, floats, booleans, null
  - Collections: maps (mappings) and vectors (sequences)
  - Anchors and aliases

  Args:
    yaml-str: A string containing YAML content

  Returns:
    A Clojure data structure representing the YAML document

  Throws:
    Exception if the YAML is malformed"
  [yaml-str]
  (when yaml-str
    (-> yaml-str
        parser/parse
        composer/compose
        resolver/resolve
        constructor/construct)))

(defn load-all
  "Parse a multi-document YAML string and return a sequence of documents.

  YAML files can contain multiple documents separated by '---'.
  This function returns all documents as a sequence.

  Args:
    yaml-str: A string containing one or more YAML documents

  Returns:
    A sequence of Clojure data structures, one per YAML document

  Throws:
    Exception if the YAML is malformed"
  [yaml-str]
  (when yaml-str
    (-> yaml-str
        parser/parse
        composer/compose-all
        resolver/resolve-all
        constructor/construct-all)))

(defn version
  "Return the YAMLStar version string"
  []
  "0.1.2-SNAPSHOT")
