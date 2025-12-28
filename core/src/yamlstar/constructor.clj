(ns yamlstar.constructor
  "Construct native Clojure data from resolved YAML nodes

  The constructor takes nodes with resolved tags and converts them to
  native Clojure data structures using a tag-based constructor lookup.")

(def constructors
  "Constructor functions for YAML core schema tags.

  Each constructor takes a node and returns native Clojure data."
  {"!!null"  (fn [_node] nil)
   "!!bool"  (fn [node] (Boolean/parseBoolean (:value node)))
   "!!int"   (fn [node] (Long/parseLong (:value node)))
   "!!float" (fn [node]
               (let [value (:value node)]
                 (cond
                   (re-matches #"[+-]?\.inf|\.Inf|\.INF" value)
                   (if (= (first value) \-) Double/NEGATIVE_INFINITY Double/POSITIVE_INFINITY)

                   (re-matches #"\.nan|\.NaN|\.NAN" value)
                   Double/NaN

                   :else
                   (Double/parseDouble value))))
   "!!str"   (fn [node] (:value node))})

(defn construct-node
  "Construct native data from a resolved node.

  Args:
    node: A node with resolved tags

  Returns:
    Native Clojure data (nil, boolean, number, string, map, or vector)"
  [node]
  (when node
    (case (:kind node)
      :scalar
      (let [tag (:tag node)
            constructor (get constructors tag)]
        (if constructor
          (constructor node)
          (throw (ex-info (str "Unknown tag: " tag)
                          {:tag tag :node node}))))

      :mapping
      (let [pairs (:value node)
            entries (mapcat (fn [[key-node val-node]]
                              [(construct-node key-node)
                               (construct-node val-node)])
                            pairs)]
        (apply array-map entries))

      :sequence
      (let [items (:value node)]
        (mapv construct-node items))

      :alias
      ;; Aliases not yet supported - will be added later
      (throw (ex-info "Aliases not yet supported"
                      {:node node}))

      ;; Default
      (throw (ex-info (str "Unknown node kind: " (:kind node))
                      {:node node})))))

(defn construct
  "Construct native data from a resolved node tree.

  Args:
    node: A resolved node tree

  Returns:
    Native Clojure data structure"
  [node]
  (construct-node node))

(defn construct-all
  "Construct native data from multiple resolved node trees.

  Args:
    nodes: Sequence of resolved node trees

  Returns:
    Sequence of native Clojure data structures"
  [nodes]
  (map construct-node nodes))
