(ns yamlstar.composer
  "Convert YAML events to node trees

  The composer takes the flat event stream from the parser and builds
  a hierarchical tree of nodes. Each node represents a YAML construct:
  - Scalar nodes (strings, numbers, etc.)
  - Mapping nodes (key-value pairs)
  - Sequence nodes (lists)

  Nodes also track anchors for later alias resolution.")

(defn make-scalar-node
  "Create a scalar node from event data"
  [{:keys [value style anchor tag]}]
  (if (or style anchor tag)
    (cond-> {:kind :scalar}
      (and style (not= style "plain")) (assoc :style style)
      anchor (assoc :anchor anchor)
      tag (assoc :tag tag)
      true (assoc :value value))
    {:kind :scalar :value value}))

(defn make-mapping-node
  "Create a mapping node"
  [pairs anchor tag flow]
  (if (or anchor tag flow)
    (cond-> {:kind :mapping}
      anchor (assoc :anchor anchor)
      tag (assoc :tag tag)
      flow (assoc :flow flow)
      true (assoc :value pairs))
    {:kind :mapping :value pairs}))

(defn make-sequence-node
  "Create a sequence node"
  [items anchor tag flow]
  (if (or anchor tag flow)
    (cond-> {:kind :sequence}
      anchor (assoc :anchor anchor)
      tag (assoc :tag tag)
      flow (assoc :flow flow)
      true (assoc :value items))
    {:kind :sequence :value items}))

(defn make-alias-node
  "Create an alias node (reference to an anchor)"
  [name]
  {:kind :alias
   :name name})

(defn- pop-children
  "Pop the nodes above index start off the transient stack"
  [stack start]
  (loop [stack stack]
    (if (> (count stack) start)
      (recur (pop! stack))
      stack)))

(defn- end-mapping
  "Replace the mapping start event at index start and the nodes above it
  with the finished mapping node"
  [stack start]
  (let [event (nth stack start)
        n (count stack)
        pairs (loop [i (inc start)
                     pairs (transient [])]
                (if (< i n)
                  (recur (+ i 2)
                         (conj! pairs [(nth stack i) (nth stack (inc i))]))
                  (persistent! pairs)))
        node (make-mapping-node pairs (:anchor event) (:tag event)
                                (:flow event))]
    (conj! (pop-children stack start) node)))

(defn- end-sequence
  "Replace the sequence start event at index start and the nodes above
  it with the finished sequence node"
  [stack start]
  (let [event (nth stack start)
        n (count stack)
        items (loop [i (inc start)
                     items (transient [])]
                (if (< i n)
                  (recur (inc i) (conj! items (nth stack i)))
                  (persistent! items)))
        node (make-sequence-node items (:anchor event) (:tag event)
                                 (:flow event))]
    (conj! (pop-children stack start) node)))

(defn- top
  "The node on top of the transient stack, or nil when it is empty"
  [stack]
  (let [n (count stack)]
    (when (pos? n)
      (nth stack (dec n)))))

(defn compose-events
  "Compose events into a node tree using a stack-based approach.

  The algorithm maintains:
  - stack: transient stack of finished nodes and pending start events
  - marks: indexes into stack of the pending mapping and sequence starts
  - documents: completed document nodes"
  [events]
  (let [events (if (vector? events) events (vec events))
        n (count events)]
    (loop [i 0
           stack (transient [])
           marks []
           documents []
           in-document false]
      (if (= i n)
        ;; End of events - collect any remaining document on stack
        (if-let [node (and in-document (top stack))]
          (conj documents node)
          documents)

        (let [event (nth events i)
              i (inc i)]
          (case (:event event)
            ;; Stream markers
            "stream_start"
            (recur i stack marks documents in-document)

            "stream_end"
            ;; If we're in a document and have content, collect it
            (let [documents (if-let [node (and in-document (top stack))]
                              (conj documents node)
                              documents)]
              (recur i (transient []) marks documents false))

            ;; Document markers
            "document_start"
            (recur i stack marks documents true)

            "document_end"
            ;; Pop the completed document from stack
            (let [node (top stack)]
              (recur i (pop! stack) marks (conj documents node) false))

            ;; Scalars
            "scalar"
            (recur i (conj! stack (make-scalar-node event)) marks
                   documents in-document)

            ;; Aliases
            "alias"
            (recur i (conj! stack (make-alias-node (:name event))) marks
                   documents in-document)

            ;; Collections: remember the stack index of the start event
            ;; (before the push, conj! mutates the stack) and push it
            "mapping_start"
            (let [mark (count stack)]
              (recur i (conj! stack event) (conj marks mark)
                     documents in-document))

            "mapping_end"
            (recur i (end-mapping stack (peek marks)) (pop marks)
                   documents in-document)

            "sequence_start"
            (let [mark (count stack)]
              (recur i (conj! stack event) (conj marks mark)
                     documents in-document))

            "sequence_end"
            (recur i (end-sequence stack (peek marks)) (pop marks)
                   documents in-document)

            ;; Default - skip unknown events
            (recur i stack marks documents in-document)))))))

(defn compose
  "Compose event stream into a single document node tree.

  Args:
    events: Sequence of event maps from parser

  Returns:
    A node tree representing the first YAML document"
  [events]
  (first (compose-events events)))

(defn compose-all
  "Compose event stream into multiple document node trees.

  Args:
    events: Sequence of event maps from parser

  Returns:
    A sequence of node trees, one per YAML document"
  [events]
  (compose-events events))
