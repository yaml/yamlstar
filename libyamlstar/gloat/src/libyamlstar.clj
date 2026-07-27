(ns libyamlstar
  "Shared library bridge - Gloat EXPORT-based C API for YAMLStar"
  (:require [clojure.string :as str]
            [yamlstar.api :as yaml]
            [ys.json :as json]))

(def EXPORT
  {"graal-create-isolate"     [:int :int :int :int]
   "graal-tear-down-isolate"  [:int :int]
   "graal-attach-thread"      [:int :int :int]
   "graal-detach-thread"      [:int :int]
   "graal-get-current-thread" [:int :int]
   "graal-get-isolate"        [:int :int]
   "yamlstar-load"            [:int :str :str]
   "yamlstar-load-all"        [:int :str :str]
   "yamlstar-dump"            [:int :str :str]
   "yamlstar-dump-all"        [:int :str :str]
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

(defn yamlstar-load
  "Load YAML string, return JSON string with {:data ...} or {:error ...}"
  [_thread yaml-str]
  (try
    (let [result (yaml/load yaml-str)]
      (json/dump {:data (nil-keys->string result)}))
    (catch #?(:glj go/any :lg Exception) e
      (json/dump
       {:error
        #?(:glj {:cause (fmt.Sprintf "%v" e)
                 :type (fmt.Sprintf "%T" e)}
           :lg {:cause (str e)
                :type "Exception"})}))))

(defn yamlstar-load-all
  "Load all YAML documents, return JSON string with {:data [...]} or {:error ...}"
  [_thread yaml-str]
  (try
    (let [result (yaml/load-all yaml-str)]
      (json/dump {:data (nil-keys->string result)}))
    (catch #?(:glj go/any :lg Exception) e
      (json/dump {:error {:cause (str e)
                          :type "Exception"
                          :message (str e)}}))))

(defn yamlstar-dump
  "Dump one JSON-encoded value to YAML, return JSON string with {:data ...} or {:error ...}"
  [_thread data-json]
  (try
    (let [result (yaml/dump (json/load data-json))]
      (json/dump {:data result}))
    (catch #?(:glj go/any :lg Exception) e
      (json/dump
       {:error
        #?(:glj {:cause (fmt.Sprintf "%v" e)
                 :type (fmt.Sprintf "%T" e)}
           :lg {:cause (str e)
                :type "Exception"})}))))

(defn yamlstar-dump-all
  "Dump JSON-encoded documents to YAML, return JSON string with {:data ...} or {:error ...}"
  [_thread data-json]
  (try
    (let [result (yaml/dump-all (json/load data-json))]
      (json/dump {:data result}))
    (catch #?(:glj go/any :lg Exception) e
      (json/dump {:error {:cause (str e)
                          :type "Exception"
                          :message (str e)}}))))

(defn yamlstar-version
  "Return the YAMLStar version string"
  [_thread]
  (yaml/version))
