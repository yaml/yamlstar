(ns yamlstar.cli-options
  "CLI helpers for building YAMLStar runtime options."
  (:require [clojure.string :as str]
            [yamlstar.api :as yaml]))

(def reference-parser-options
  {:plugin {:parser {:name "reference"}}})

(defn normalize-key
  [k]
  (-> k name (str/replace "_" "-") keyword))

(defn normalize-keys
  [x]
  (cond
    (map? x) (into {}
                   (map (fn [[k v]]
                          [(normalize-key k) (normalize-keys v)]))
                   x)
    (vector? x) (mapv normalize-keys x)
    :else x))

(defn deep-merge
  [& maps]
  (letfn [(merge-values [a b]
            (if (and (map? a) (map? b))
              (merge-with merge-values a b)
              b))]
    (let [maps (remove nil? maps)]
      (if (seq maps)
        (apply merge-with merge-values maps)
        {}))))

(defn file-exists?
  [path]
  #?(:clj (.exists (java.io.File. path))
     :glj (let [[_ err] (os.Stat path)]
            (nil? err))))

(defn config-text
  [config]
  (if (file-exists? config)
    (slurp config)
    config))

(defn config-options
  [config]
  (if (str/blank? config)
    {}
    (let [text (config-text config)]
      (if (str/blank? text)
        {}
        (let [opts (yaml/load text reference-parser-options)]
          (when-not (map? opts)
            (throw (ex-info "YAMLStar CLI config must be a mapping"
                            {:config config
                             :value opts})))
          (normalize-keys opts))))))

(defn parser-options
  [name]
  (if (str/blank? name)
    {}
    {:plugin {:parser {:name name}}}))

(defn plugin-options
  [spec]
  (let [[api name extra] (str/split spec #"=" 3)]
    (when (or (str/blank? api) (str/blank? name) extra)
      (throw (ex-info "Plugin option must be API=NAME"
                      {:plugin spec})))
    {:plugin {(normalize-key api) {:name name}}}))

(defn cli-plugin-options
  [opts]
  (apply deep-merge
         (concat
           (map plugin-options (:plugin opts))
           [(parser-options (:parser opts))])))

(defn env-options
  ([]
   (env-options #(System/getenv %)))
  ([getenv]
   (deep-merge
     (parser-options (getenv "YAMLSTAR_PARSER"))
     (config-options (getenv "YAMLSTAR_CONFIG")))))

(defn runtime-options
  ([opts]
   (runtime-options opts #(System/getenv %)))
  ([opts getenv]
   (deep-merge
     (env-options getenv)
     (config-options (:config opts))
     (cli-plugin-options opts))))
