(ns yamlstar.desolver
  "Choose minimal YAML tags and scalar styles for dumped nodes."
  (:require [clojure.string :as str])
  (:refer-clojure :exclude [resolve]))

(defn- implicit-string? [value]
  (or (= value "")
      (re-matches #"null|Null|NULL|~" value)
      (re-matches #"true|True|TRUE|false|False|FALSE" value)
      (re-matches #"[-+]?[0-9]+" value)
      (re-matches #"[-+]?(\.[0-9]+|[0-9]+(\.[0-9]*)?)([eE][-+]?[0-9]+)?" value)
      (re-matches #"[+-]?(\.inf|\.Inf|\.INF)" value)
      (re-matches #"\.nan|\.NaN|\.NAN" value)))

(defn- plain-safe? [value]
  (and (not (implicit-string? value))
       (not (re-find #"[#\[\]\{\},&*?:|>'\"%@`]" value))
       (not (re-find #"^\s|\s$" value))
       (not (re-find #"\r|\n|\t" value))
       (not (re-find #"^[-?](\s|$)" value))))

(defn- core-tag? [tag]
  (contains? #{"!!null" "!!bool" "!!int" "!!float" "!!str"
               "tag:yaml.org,2002:null"
               "tag:yaml.org,2002:bool"
               "tag:yaml.org,2002:int"
               "tag:yaml.org,2002:float"
               "tag:yaml.org,2002:str"}
             tag))

(defn- string-tag? [tag]
  (contains? #{"!!str" "tag:yaml.org,2002:str"} tag))

(defn- scalar-style [value tag]
  (when (= tag "!!str")
    (let [newline-count (count (filter #{\newline} value))]
      (cond
        (plain-safe? value) nil
        (and (= 1 newline-count) (str/ends-with? value "\n")) "double"
        (pos? newline-count) "literal"
        :else "single"))))

(defn desolve-node
  "Remove implicit tags and add style hints where needed."
  [node]
  (when node
    (case (:kind node)
      :scalar
      (let [value (:value node)
            tag (:tag node)]
        (cond-> {:kind :scalar :value value}
          (scalar-style value tag) (assoc :style (scalar-style value tag))
          (and tag (not (string-tag? tag))) (assoc :tag tag)))

      :mapping
      (cond-> {:kind :mapping
               :value (mapv (fn [[k v]] [(desolve-node k) (desolve-node v)])
                            (:value node))}
        (:anchor node) (assoc :anchor (:anchor node))
        (:flow node) (assoc :flow (:flow node))
        (and (:tag node) (not (core-tag? (:tag node)))) (assoc :tag (:tag node)))

      :sequence
      (cond-> {:kind :sequence
               :value (mapv desolve-node (:value node))}
        (:anchor node) (assoc :anchor (:anchor node))
        (:flow node) (assoc :flow (:flow node))
        (and (:tag node) (not (core-tag? (:tag node)))) (assoc :tag (:tag node)))

      node)))

(defn desolve
  "Prepare a represented node tree for serialization."
  [node]
  (desolve-node node))

(defn desolve-all
  "Prepare represented node trees for serialization."
  [nodes]
  (mapv desolve-node nodes))
