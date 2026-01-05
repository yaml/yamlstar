(ns yamlstar.constructor
  "Construct native Clojure data from resolved YAML nodes

  The constructor takes nodes with resolved tags and converts them to
  native Clojure data structures using a tag-based constructor lookup.")

(def constructors
  "Constructor functions for YAML core schema tags.

  Each constructor takes a node and returns native Clojure data.
  Supports both short form (!!null) and fully qualified (tag:yaml.org,2002:null) tags."
  (let [null-fn  (fn [_node] nil)
        bool-fn  (fn [node] (Boolean/parseBoolean (:value node)))
        int-fn   (fn [node] (Long/parseLong (:value node)))
        float-fn (fn [node]
                   (let [value (:value node)]
                     (cond
                       (re-matches #"[+-]?\.inf|\.Inf|\.INF" value)
                       (if (= (first value) \-) Double/NEGATIVE_INFINITY Double/POSITIVE_INFINITY)

                       (re-matches #"\.nan|\.NaN|\.NAN" value)
                       Double/NaN

                       :else
                       (Double/parseDouble value))))
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

(defn construct-node
  "Construct native data from a resolved node.

  Args:
    node: A node with resolved tags
    anchors: An atom containing a map of anchor names to constructed values

  Returns:
    Native Clojure data (nil, boolean, number, string, map, or vector)"
  [node anchors]
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
            (let [pairs (:value node)
                  ;; Use reduce for eager evaluation to ensure anchors are stored before aliases are resolved
                  entries (reduce (fn [acc [key-node val-node]]
                                    (conj acc
                                          (construct-node key-node anchors)
                                          (construct-node val-node anchors)))
                                  []
                                  pairs)]
              (apply array-map entries))

            :sequence
            (let [items (:value node)]
              (mapv #(construct-node % anchors) items))

            :alias
            ;; Look up the anchor in the anchors map
            (let [anchor-name (:name node)]
              (if (contains? @anchors anchor-name)
                (get @anchors anchor-name)
                (throw (ex-info (str "Unknown anchor: " anchor-name)
                                {:anchor anchor-name :node node}))))

            ;; Default
            (throw (ex-info (str "Unknown node kind: " (:kind node))
                            {:node node})))]
      ;; If this node has an anchor, store the result
      (when-let [anchor-name (:anchor node)]
        (swap! anchors assoc anchor-name result))
      result)))

(defn construct
  "Construct native data from a resolved node tree.

  Args:
    node: A resolved node tree

  Returns:
    Native Clojure data structure"
  [node]
  (let [anchors (atom {})]
    (construct-node node anchors)))

(defn construct-all
  "Construct native data from multiple resolved node trees.

  Args:
    nodes: Sequence of resolved node trees

  Returns:
    Sequence of native Clojure data structures"
  [nodes]
  (let [anchors (atom {})]
    (map #(construct-node % anchors) nodes)))
