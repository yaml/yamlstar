(ns yamlstar.resolver
  "Resolve tags for YAML nodes

  The resolver applies YAML 1.2 core schema type inference:
  - Infer tags for untagged scalar nodes based on value patterns
  - Add !!map and !!seq tags to collection nodes
  - Output: Node tree with all tags resolved"
  (:refer-clojure :exclude [resolve]))

(defn infer-scalar-tag
  "Infer the tag for an untagged scalar based on YAML 1.2 core schema.

  Args:
    value: The string value of the scalar

  Returns:
    A tag string (!!null, !!bool, !!int, !!float, or !!str)"
  [value]
  (cond
    ;; null values
    (re-matches #"null|Null|NULL|~" value) "!!null"

    ;; booleans
    (re-matches #"true|True|TRUE|false|False|FALSE" value) "!!bool"

    ;; integers (decimal only for now)
    (re-matches #"[-+]?[0-9]+" value) "!!int"

    ;; floats (including special values)
    (or (re-matches #"[-+]?(\.[0-9]+|[0-9]+(\.[0-9]*)?)([eE][-+]?[0-9]+)?" value)
        (re-matches #"[+-]?(\.inf|\.Inf|\.INF)" value)
        (re-matches #"\.nan|\.NaN|\.NAN" value))
    "!!float"

    ;; default to string
    :else "!!str"))

(defn resolve-node
  "Add resolved tag to a node.

  For untagged nodes, infers the tag based on YAML 1.2 core schema.
  For already-tagged nodes, leaves the tag unchanged.
  Recursively processes child nodes.

  Args:
    node: A node from the composer

  Returns:
    The node with :tag field populated"
  [node]
  (when node
    (case (:kind node)
      :scalar
      (let [tag (or (:tag node) (infer-scalar-tag (:value node)))]
        (assoc node :tag tag))

      :mapping
      (let [tag (or (:tag node) "!!map")
            pairs (:value node)
            resolved-pairs (mapv (fn [[k v]]
                                   [(resolve-node k) (resolve-node v)])
                                 pairs)]
        (assoc node
               :tag tag
               :value resolved-pairs))

      :sequence
      (let [tag (or (:tag node) "!!seq")
            items (:value node)
            resolved-items (mapv resolve-node items)]
        (assoc node
               :tag tag
               :value resolved-items))

      :alias
      ;; Aliases remain unchanged - will be resolved in constructor
      node

      ;; Default - return node unchanged
      node)))

(defn resolve
  "Resolve tags for a node tree.

  Walks the entire tree and adds tags to all untagged nodes.

  Args:
    node: A node tree from composer

  Returns:
    The same node tree structure with all tags resolved"
  [node]
  (resolve-node node))

(defn resolve-all
  "Resolve tags for multiple node trees.

  Args:
    nodes: Sequence of node trees from composer

  Returns:
    Sequence of node trees with tags resolved"
  [nodes]
  (map resolve-node nodes))
