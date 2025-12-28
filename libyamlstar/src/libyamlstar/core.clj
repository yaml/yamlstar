(ns libyamlstar.core
  "Shared library core - bridges Clojure to C API"
  (:require [clojure.data.json :as json]
            [yamlstar.core :as yaml])
  (:gen-class
   :methods [^:static [loadYaml [String] String]
             ^:static [loadYamlAll [String] String]
             ^:static [version [] String]]))

(declare json-write-str error-map debug)

(defn -loadYaml
  "Load a single YAML document, return JSON string with result or error"
  [^String yaml-str]
  (debug "libyamlstar load - input:" yaml-str)
  (let [resp (try
               (->> yaml-str
                    yaml/load
                    (assoc {} :data)
                    json-write-str)
               (catch Exception e
                 (-> e error-map json-write-str)))]
    (debug "libyamlstar load - response:" resp)
    resp))

(defn -loadYamlAll
  "Load all YAML documents, return JSON string with result or error"
  [^String yaml-str]
  (debug "libyamlstar load-all - input:" yaml-str)
  (let [resp (try
               (->> yaml-str
                    yaml/load-all
                    (assoc {} :data)
                    json-write-str)
               (catch Exception e
                 (-> e error-map json-write-str)))]
    (debug "libyamlstar load-all - response:" resp)
    resp))

(defn -version
  "Return the YAMLStar version string"
  []
  (yaml/version))

(defn -main
  "Entry point for GraalVM native-image (required but not used)"
  [& _args]
  (println "libyamlstar shared library")
  (println "Version:" (-version)))

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
    (seq? x) (map nil-keys->string x)
    :else x))

(defn json-write-str [data]
  (json/write-str (nil-keys->string data)
                  :escape-unicode false
                  :escape-js-separators false
                  :escape-slash false))

(defn error-map [^Exception e]
  (let [err (Throwable->map e)]
    {:error {:cause (:cause err)
             :type (str (get-in err [:via 0 :type]))
             :message (.getMessage e)}}))

(defn debug [& msg]
  (when (System/getenv "YAMLSTAR_DEBUG")
    (binding [*out* *err*]
      (apply println msg))))
