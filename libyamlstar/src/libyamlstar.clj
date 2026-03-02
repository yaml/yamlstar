(ns libyamlstar
  "Shared library bridge - Gloat EXPORT-based C API for YAMLStar"
  (:require [clojure.string :as str]
            [yamlstar.core :as yaml]
            [ys.json :as json]))

(def EXPORT
  {"yamlstar-load"    [:str :str :str]
   "yamlstar-version" [:str]})

(defn nil-keys->string
  "Replace nil keys with string 'null' for JSON serialization.
  JSON allows null values but not null keys."
  [x]
  (cond
    (map? x) (into {} (map (fn [[k v]]
                              [(if (nil? k) "null" (nil-keys->string k))
                               (nil-keys->string v)])
                            x))
    (vector? x) (mapv nil-keys->string x)
    (sequential? x) (map nil-keys->string x)
    :else x))

(defn yamlstar-load
  "Load YAML string, return JSON string with {:data ...} or {:error ...}"
  [yaml-str opts-json]
  (try
    (let [result (yaml/load yaml-str)]
      (json/dump {:data (nil-keys->string result)}))
    (catch go/any e
      (json/dump {:error {:cause (str e)
                          :type "Exception"
                          :message (str e)}}))))

(defn yamlstar-version
  "Return the YAMLStar version string"
  []
  (yaml/version))
