(ns yamlstar.desolver
  "Choose minimal YAML tags and scalar styles for dumped nodes."
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

(defn desolve-node
  "Remove implicit tags and add style hints where needed."
  [node]
  (when node
    (case (:kind node)
      :scalar
      (let [value (:value node)
            tag (:tag node)]
        (cond-> {:kind :scalar :value value}
          (= tag "!!str") (assoc :tag tag)
          (and (= tag "!!str") (not (plain-safe? value))) (assoc :style "double")
          (not= tag "!!str") (assoc :tag tag)))

      :mapping
      {:kind :mapping
       :value (mapv (fn [[k v]] [(desolve-node k) (desolve-node v)])
                    (:value node))}

      :sequence
      {:kind :sequence
       :value (mapv desolve-node (:value node))}

      node)))

(defn desolve
  "Prepare a represented node tree for serialization."
  [node]
  (desolve-node node))

(defn desolve-all
  "Prepare represented node trees for serialization."
  [nodes]
  (mapv desolve-node nodes))
