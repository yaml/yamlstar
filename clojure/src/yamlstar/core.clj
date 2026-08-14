(ns yamlstar.core
  "YAMLStar Clojure API - YAML 1.2 loader"
  (:refer-clojure :exclude [load])
  (:require [yamlstar.api :as api]))

(defn load
  "Parse a YAML string and return a Clojure data structure.

  An optional opts map can select a parser plugin:
    (load yaml {:plugin {:parser {:name \"snakeyaml\"}}})"
  ([yaml-str]
   (api/load yaml-str))
  ([yaml-str opts]
   (api/load yaml-str opts)))

(defn load-all
  "Parse a multi-document YAML string and return a sequence of documents.

  An optional opts map can select a parser plugin:
    (load-all yaml {:plugin {:parser {:name \"snakeyaml\"}}})"
  ([yaml-str]
   (api/load-all yaml-str))
  ([yaml-str opts]
   (api/load-all yaml-str opts)))

(defn dump
  "Dump a JSON-compatible Clojure value to a YAML string."
  [value]
  (api/dump value))

(defn dump-all
  "Dump a sequence of JSON-compatible Clojure values to a YAML stream."
  [values]
  (api/dump-all values))

(defn version
  "Return the YAMLStar version string."
  []
  (api/version))
