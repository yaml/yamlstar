(ns yamlstar.core
  "YAMLStar Clojure API - YAML 1.2 loader"
  (:refer-clojure :exclude [load])
  (:require [yamlstar.api :as api]))

(defn load
  "Parse a YAML string and return a Clojure data structure.

  An optional opts map can select a YAML parser plugin:
    (load yaml {:plugin {:yaml-parser {:name \"snakeyaml\"}}})"
  ([yaml-str]
   (api/load yaml-str))
  ([yaml-str opts]
   (api/load yaml-str opts)))

(defn load-all
  "Parse a multi-document YAML string and return a sequence of documents.

  An optional opts map can select a YAML parser plugin:
    (load-all yaml {:plugin {:yaml-parser {:name \"snakeyaml\"}}})"
  ([yaml-str]
   (api/load-all yaml-str))
  ([yaml-str opts]
   (api/load-all yaml-str opts)))

(defn dump
  "Dump a supported Clojure value to a YAML string.

  An optional opts map can select a YAML emitter plugin:
    (dump value {:plugin {:yaml-emitter {:name \"snakeyaml\"}}})"
  ([value]
   (api/dump value))
  ([value opts]
   (api/dump value opts)))

(defn dump-all
  "Dump a sequence of supported Clojure values to a YAML stream.

  An optional opts map can select a YAML emitter plugin."
  ([values]
   (api/dump-all values))
  ([values opts]
   (api/dump-all values opts)))

(defn version
  "Return the YAMLStar version string."
  []
  (api/version))
