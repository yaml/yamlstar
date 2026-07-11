(ns yamlstar.core
  "YAMLStar Clojure API - YAML 1.2 loader"
  (:refer-clojure :exclude [load])
  (:require [yamlstar.api :as api]))

(defn load
  "Parse a YAML string and return a Clojure data structure."
  [yaml-str]
  (api/load yaml-str))

(defn load-all
  "Parse a multi-document YAML string and return a sequence of documents."
  [yaml-str]
  (api/load-all yaml-str))

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
