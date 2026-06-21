(ns com.yaml.yamlstar
  "YAMLStar Java binding - YAML 1.2 loader for Java"
  (:refer-clojure :exclude [load])
  (:require [yamlstar.core :as yaml])
  (:import [java.util ArrayList HashMap Map List])
  (:gen-class
   :name com.yaml.YAMLStar
   :methods [^:static [load [String] Object]
             ^:static [loadAll [String] java.util.List]
             ^:static [dump [Object] String]
             ^:static [dumpAll [java.util.List] String]
             ^:static [version [] String]]))

(defn- clj->java
  "Convert Clojure persistent collections to Java mutable collections.
  This provides a better experience for Java developers who expect
  standard Java collections."
  [obj]
  (cond
    (map? obj)
    (let [m (HashMap.)]
      (doseq [[k v] obj]
        (.put m (clj->java k) (clj->java v)))
      m)

    (sequential? obj)
    (let [l (ArrayList.)]
      (doseq [item obj]
        (.add l (clj->java item)))
      l)

    :else
    obj))

(defn- java->clj
  "Convert Java collections to Clojure data for dumping."
  [obj]
  (cond
    (instance? Map obj)
    (into {} (map (fn [[k v]] [(java->clj k) (java->clj v)]) obj))

    (instance? List obj)
    (mapv java->clj obj)

    :else
    obj))

(defn -load
  "Load a single YAML document and return it as a Java object.

  YAML mappings are converted to java.util.HashMap.
  YAML sequences are converted to java.util.ArrayList.
  YAML scalars are converted to their corresponding Java types:
    - null -> null
    - boolean -> Boolean
    - integer -> Long
    - float -> Double
    - string -> String

  If the input contains multiple YAML documents, only the first
  document is returned."
  [^String yaml-string]
  (-> yaml-string
      yaml/load
      clj->java))

(defn -loadAll
  "Load all YAML documents from a string and return them as a java.util.List.

  Each document is converted according to the same rules as load().
  Documents are separated by --- markers in the YAML input."
  [^String yaml-string]
  (-> yaml-string
      yaml/load-all
      clj->java))

(defn -dump
  "Dump a Java JSON-compatible value to YAML."
  [value]
  (yaml/dump (java->clj value)))

(defn -dumpAll
  "Dump Java JSON-compatible values to a multi-document YAML stream."
  [values]
  (yaml/dump-all (java->clj values)))

(defn -version
  "Return the YAMLStar version string."
  []
  "0.1.8")
