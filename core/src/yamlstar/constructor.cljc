(ns yamlstar.constructor
  "Construct native Clojure data from resolved YAML nodes

  The constructor takes nodes with resolved tags and converts them to
  native Clojure data structures using a tag-based constructor lookup."
  (:require [yamlstar.numbers :as numbers]
            [yamlstar.plugin.alias-data :as alias-data]))

(def constructors
  "Constructor functions for YAML core schema tags.

  Each constructor takes a node and returns native Clojure data.
  Supports both short form (!!null) and fully qualified (tag:yaml.org,2002:null) tags."
  (let [null-fn  (fn [_node] nil)
        bool-fn  (fn [node]
                   (contains? #{"true" "True" "TRUE"} (:value node)))
        int-fn   (fn [node]
                   (numbers/parse-safe-integer (:value node)))
        float-fn (fn [node]
                   (let [value (:value node)]
                     (cond
                       (re-matches #"[+-]?\.inf|\.Inf|\.INF" value)
                       #?(:clj (if (= (first value) \-)
                                 Double/NEGATIVE_INFINITY
                                 Double/POSITIVE_INFINITY)
                          :glj (if (= (first value) \-) (math.Inf -1) (math.Inf 1))
                          :lg (if (= (first value) \-) ##-Inf ##Inf))

                       (re-matches #"\.nan|\.NaN|\.NAN" value)
                       #?(:clj Double/NaN
                          :glj (math.NaN)
                          :lg ##NaN)

                       :else
                       #?(:clj (Double/parseDouble value)
                          :glj (let [[f _] (strconv.ParseFloat value 64)]
                                 f)
                          :lg (read-string value)))))
        str-fn   (fn [node] (:value node))]
    {"!!null"                  null-fn
     "tag:yaml.org,2002:null"  null-fn
     "!!bool"                  bool-fn
     "tag:yaml.org,2002:bool"  bool-fn
     "!!int"                   int-fn
     "tag:yaml.org,2002:int"   int-fn
     "!!float"                 float-fn
     "tag:yaml.org,2002:float" float-fn
     "!!str"                   str-fn
     "tag:yaml.org,2002:str"   str-fn}))

(declare construct-node)

(defn- merge-key?
  [node]
  (and (= :scalar (:kind node))
       (= "<<" (:value node))
       (or (= "!!merge" (:tag node))
           (and (= "!!str" (:tag node))
                (nil? (:style node))))))

(defn- merge-value
  [value]
  (cond
    (map? value) value
    (sequential? value)
    (reduce (fn [result mapping]
              (when-not (map? mapping)
                (throw (ex-info
                        "Map merge requires a map or sequence of maps"
                        {:value value})))
              (merge result mapping))
            {}
            (reverse value))
    :else
    (throw (ex-info "Map merge requires a map or sequence of maps"
                    {:value value}))))

(defn- construct-mapping
  [pairs context]
  (let [{:keys [explicit merges]}
        (reduce (fn [result [key-node value-node]]
                  (if (merge-key? key-node)
                    (update result :merges conj
                            (construct-node value-node context))
                    (update result :explicit conj
                            [(construct-node key-node context)
                             (construct-node value-node context)])))
                {:explicit [] :merges []}
                pairs)
        merged (reduce (fn [result value]
                         (merge result (merge-value value)))
                       {}
                       merges)
        explicit-map (apply array-map (mapcat identity explicit))]
    (if (empty? merges)
      explicit-map
      (apply array-map
             (mapcat identity
                     (concat (remove (fn [[key _]]
                                       (contains? explicit-map key))
                                     merged)
                             explicit))))))

(defn construct-node
  "Construct native data from a resolved node.

  Args:
    node: A node with resolved tags
    context: The operation-local Alias-Data context

  Returns:
    Native Clojure data (nil, boolean, number, string, map, or vector)"
  [node context]
  (when node
    (let [result
          (case (:kind node)
            :scalar
            (let [tag (:tag node)
                  constructor (get constructors tag)]
              (if constructor
                (constructor node)
                (throw (ex-info (str "Unknown tag: " tag)
                                {:tag tag :node node}))))

            :mapping
            (construct-mapping (:value node) context)

            :sequence
            (let [items (:value node)]
              (mapv #(construct-node % context) items))

            :alias
            (let [anchor-name (:name node)
                  [found value]
                  (alias-data/resolve-alias context anchor-name)]
              (if found
                value
                (throw (ex-info (str "Unknown anchor: " anchor-name)
                                {:anchor anchor-name :node node}))))

            ;; Default
            (throw (ex-info (str "Unknown node kind: " (:kind node))
                            {:node node})))]
      ;; If this node has an anchor, store the result
      (when-let [anchor-name (:anchor node)]
        (alias-data/define-anchor context anchor-name result))
      result)))

(defn construct
  "Construct native data from a resolved node tree.

  Args:
    node: A resolved node tree

  Returns:
    Native Clojure data structure"
  ([node]
   (construct node (alias-data/default-context)))
  ([node context]
   (alias-data/begin-stream context)
   (try
     (alias-data/begin-document context)
     (let [result (construct-node node context)]
       (alias-data/end-document context)
       result)
     (finally
       (alias-data/end-stream context)))))

(defn construct-all
  "Construct native data from multiple resolved node trees.

  Args:
    nodes: Sequence of resolved node trees

  Returns:
    Sequence of native Clojure data structures"
  ([nodes]
   (construct-all nodes (alias-data/default-context)))
  ([nodes context]
   (alias-data/begin-stream context)
   (try
     (mapv (fn [node]
             (alias-data/begin-document context)
             (let [result (construct-node node context)]
               (alias-data/end-document context)
               result))
           nodes)
     (finally
       (alias-data/end-stream context)))))
