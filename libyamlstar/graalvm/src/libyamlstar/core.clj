(ns libyamlstar.core
  "Shared library core - bridges Clojure to C API"
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [yamlstar.api :as yaml]
            [yamlstar.plugin.parser.snakeyaml])
  (:gen-class
   :methods [^:static [loadYaml [String String] String]
             ^:static [loadYamlAll [String String] String]
             ^:static [dumpYaml [String String] String]
             ^:static [dumpYamlAll [String String] String]
             ^:static [version [] String]]))

(declare json-write-str error-map debug)

(defn normalize-keys
  "Keywordize map keys recursively, converting snake_case to kebab-case.

  Only keys are rewritten; values are never touched."
  [x]
  (cond
    (map? x) (into {}
                   (map (fn [[k v]]
                          [(keyword (str/replace (name k) "_" "-"))
                           (normalize-keys v)]))
                   x)
    (vector? x) (mapv normalize-keys x)
    :else x))

(defn parse-opts
  "Parse a JSON options string into a normalized opts map.

  Returns nil for nil/blank/empty opts (the fast path)."
  [opts-json]
  (when-not (or (nil? opts-json) (str/blank? opts-json))
    (not-empty (normalize-keys (json/read-str opts-json)))))

(defn -loadYaml
  "Load a single YAML document, return JSON string with result or error"
  [^String yaml-str ^String opts-json]
  (debug "libyamlstar load - input:" yaml-str)
  (debug "libyamlstar load - options:" opts-json)
  (let [resp (try
               (-> yaml-str
                   (yaml/load (parse-opts opts-json))
                   (->> (assoc {} :data))
                   json-write-str)
               (catch Exception e
                 (-> e error-map json-write-str)))]
    (debug "libyamlstar load - response:" resp)
    resp))

(defn -loadYamlAll
  "Load all YAML documents, return JSON string with result or error"
  [^String yaml-str ^String opts-json]
  (debug "libyamlstar load-all - input:" yaml-str)
  (debug "libyamlstar load-all - options:" opts-json)
  (let [resp (try
               (-> yaml-str
                   (yaml/load-all (parse-opts opts-json))
                   (->> (assoc {} :data))
                   json-write-str)
               (catch Exception e
                 (-> e error-map json-write-str)))]
    (debug "libyamlstar load-all - response:" resp)
    resp))

(defn -dumpYaml
  "Dump one JSON-encoded value to YAML, return JSON string with result or error

  The options argument is parsed for validity but otherwise reserved."
  [^String data-json ^String opts-json]
  (debug "libyamlstar dump - input:" data-json)
  (debug "libyamlstar dump - options:" opts-json)
  (let [resp (try
               (parse-opts opts-json)
               (->> (json/read-str data-json)
                    yaml/dump
                    (assoc {} :data)
                    json-write-str)
               (catch Exception e
                 (-> e error-map json-write-str)))]
    (debug "libyamlstar dump - response:" resp)
    resp))

(defn -dumpYamlAll
  "Dump JSON-encoded documents to YAML, return JSON string with result or error

  The options argument is parsed for validity but otherwise reserved."
  [^String data-json ^String opts-json]
  (debug "libyamlstar dump-all - input:" data-json)
  (debug "libyamlstar dump-all - options:" opts-json)
  (let [resp (try
               (parse-opts opts-json)
               (->> (json/read-str data-json)
                    yaml/dump-all
                    (assoc {} :data)
                    json-write-str)
               (catch Exception e
                 (-> e error-map json-write-str)))]
    (debug "libyamlstar dump-all - response:" resp)
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
    (map? x) (apply array-map
                    (mapcat (fn [[k v]]
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
