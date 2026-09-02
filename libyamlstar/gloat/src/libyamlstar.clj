(ns libyamlstar
  "Shared library bridge - Gloat EXPORT-based C API for YAMLStar"
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [yamlstar.api :as yaml]
            [yamlstar.parser :as parser]
            [yamlstar.plugin.parser.reference]
            [yamlstar.plugin.parser.go-yaml]))

(parser/register-reference-parser!)
(parser/set-default-parser! "go-yaml")

(def EXPORT
  {"graal-create-isolate"     [:int :int :int :int]
   "graal-tear-down-isolate"  [:int :int]
   "graal-attach-thread"      [:int :int :int]
   "graal-detach-thread"      [:int :int]
   "graal-get-current-thread" [:int :int]
   "graal-get-isolate"        [:int :int]
   "yamlstar-load"            [:int :str :str :str]
   "yamlstar-load-all"        [:int :str :str :str]
   "yamlstar-dump"            [:int :str :str :str]
   "yamlstar-dump-all"        [:int :str :str :str]
   "yamlstar-version"         [:int :str]})

;; The public C API historically exposed GraalVM isolate lifecycle functions.
;; Glojure runs inside Go's process-wide runtime and needs no isolate, so these
;; compatibility exports accept and ignore the opaque handles.
(defn graal-create-isolate [_params _isolate _thread] 0)
(defn graal-tear-down-isolate [_thread] 0)
(defn graal-attach-thread [_isolate _thread] 0)
(defn graal-detach-thread [_thread] 0)
(defn graal-get-current-thread [_isolate] 0)
(defn graal-get-isolate [_thread] 0)

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
    (sequential? x) (map nil-keys->string x)
    :else x))

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

(defn yamlstar-load
  "Load YAML string, return JSON string with {:data ...} or {:error ...}"
  [_thread yaml-str opts-json]
  (try
    (let [result (yaml/load yaml-str (parse-opts opts-json))]
      (json/write-str {:data (nil-keys->string result)}))
    (catch #?(:glj go/any :lg Exception) e
      (json/write-str
       {:error
        #?(:glj {:cause (fmt.Sprintf "%v" e)
                 :type (fmt.Sprintf "%T" e)}
           :lg {:cause (str e)
                :type "Exception"})}))))

(defn yamlstar-load-all
  "Load all YAML documents, return JSON string with {:data [...]} or {:error ...}"
  [_thread yaml-str opts-json]
  (try
    (let [result (yaml/load-all yaml-str (parse-opts opts-json))]
      (json/write-str {:data (nil-keys->string result)}))
    (catch #?(:glj go/any :lg Exception) e
      (json/write-str {:error {:cause (str e)
                               :type "Exception"
                               :message (str e)}}))))

(defn yamlstar-dump
  "Dump one JSON-encoded value to YAML, return JSON string with {:data ...} or {:error ...}"
  [_thread data-json opts-json]
  (try
    (let [_ (parse-opts opts-json)
          result (yaml/dump (json/read-str data-json))]
      (json/write-str {:data result}))
    (catch #?(:glj go/any :lg Exception) e
      (json/write-str
       {:error
        #?(:glj {:cause (fmt.Sprintf "%v" e)
                 :type (fmt.Sprintf "%T" e)}
           :lg {:cause (str e)
                :type "Exception"})}))))

(defn yamlstar-dump-all
  "Dump JSON-encoded documents to YAML, return JSON string with {:data ...} or {:error ...}"
  [_thread data-json opts-json]
  (try
    (let [_ (parse-opts opts-json)
          result (yaml/dump-all (json/read-str data-json))]
      (json/write-str {:data result}))
    (catch #?(:glj go/any :lg Exception) e
      (json/write-str {:error {:cause (str e)
                               :type "Exception"
                               :message (str e)}}))))

(defn yamlstar-version
  "Return the YAMLStar version string"
  [_thread]
  (yaml/version))
