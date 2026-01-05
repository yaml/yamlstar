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
  [{:keys [value style anchor tag] :or {style "plain"}}]
  (cond-> {:kind :scalar}
    (not= style "plain") (assoc :style style)
    anchor (assoc :anchor anchor)
    tag (assoc :tag tag)
    true (assoc :value value)))

(defn make-mapping-node
  "Create a mapping node"
  [pairs anchor tag flow]
  (cond-> {:kind :mapping}
    anchor (assoc :anchor anchor)
    tag (assoc :tag tag)
    flow (assoc :flow flow)
    true (assoc :value pairs)))

(defn make-sequence-node
  "Create a sequence node"
  [items anchor tag flow]
  (cond-> {:kind :sequence}
    anchor (assoc :anchor anchor)
    tag (assoc :tag tag)
    flow (assoc :flow flow)
    true (assoc :value items)))

(defn make-alias-node
  "Create an alias node (reference to an anchor)"
  [name]
  {:kind :alias
   :name name})

(defn compose-events
  "Compose events into a node tree using a stack-based approach.

  The algorithm maintains:
  - node-stack: stack of nodes being constructed
  - anchor-stack: current anchor/tag properties
  - documents: completed document nodes"
  [events]
  (loop [events events
         node-stack []
         anchor-stack []
         current-anchor nil
         current-tag nil
         documents []
         in-document false]
    (if (empty? events)
      ;; End of events - collect any remaining document on stack
      (if (and in-document (seq node-stack))
        (conj documents (peek node-stack))
        documents)

      (let [event (first events)
            event-type (:event event)
            rest-events (rest events)]

        (case event-type
          ;; Stream markers
          "stream_start"
          (recur rest-events node-stack anchor-stack current-anchor current-tag documents in-document)

          "stream_end"
          ;; If we're in a document and have content, collect it
          (let [final-docs (if (and in-document (seq node-stack))
                            (conj documents (peek node-stack))
                            documents)]
            (recur rest-events [] anchor-stack nil nil final-docs false))

          ;; Document markers
          "document_start"
          (recur rest-events node-stack anchor-stack current-anchor current-tag documents true)

          "document_end"
          ;; Pop the completed document from stack
          (let [doc-node (peek node-stack)
                new-stack (pop node-stack)]
            (recur rest-events new-stack anchor-stack nil nil (conj documents doc-node) false))

          ;; Scalars
          "scalar"
          (let [node (make-scalar-node (assoc event
                                              :anchor (or current-anchor (:anchor event))
                                              :tag (or current-tag (:tag event))))
                new-stack (conj node-stack node)]
            (recur rest-events new-stack anchor-stack nil nil documents in-document))

          ;; Aliases
          "alias"
          (let [node (make-alias-node (:name event))
                new-stack (conj node-stack node)]
            (recur rest-events new-stack anchor-stack nil nil documents in-document))

          ;; Mappings
          "mapping_start"
          ;; Push a mapping marker with properties
          (let [marker {:kind :mapping-start
                        :anchor (or current-anchor (:anchor event))
                        :tag (or current-tag (:tag event))
                        :flow (or (:flow event) false)}
                new-stack (conj node-stack marker)]
            (recur rest-events new-stack anchor-stack nil nil documents in-document))

          "mapping_end"
          ;; Pop nodes until we find mapping-start, then create mapping node
          (let [[pairs final-stack]
                (loop [pairs []
                       stack node-stack]
                  (let [top (peek stack)]
                    (if (= (:kind top) :mapping-start)
                      ;; Found the start marker - return pairs and stack
                      [pairs stack]
                      ;; Collect key-value pair (values are pushed in reverse order)
                      (let [value (peek stack)
                            key (peek (pop stack))
                            new-pairs (conj pairs [key value])
                            new-stack (pop (pop stack))]
                        (recur new-pairs new-stack)))))
                ;; Create mapping node from collected pairs
                marker (peek final-stack)
                mapping (make-mapping-node (vec (reverse pairs))
                                          (:anchor marker)
                                          (:tag marker)
                                          (:flow marker))
                new-stack (conj (pop final-stack) mapping)]
            (recur rest-events new-stack anchor-stack nil nil documents in-document))

          ;; Sequences
          "sequence_start"
          ;; Push a sequence marker
          (let [marker {:kind :sequence-start
                        :anchor (or current-anchor (:anchor event))
                        :tag (or current-tag (:tag event))
                        :flow (or (:flow event) false)}
                new-stack (conj node-stack marker)]
            (recur rest-events new-stack anchor-stack nil nil documents in-document))

          "sequence_end"
          ;; Pop nodes until we find sequence-start, then create sequence node
          (let [[items final-stack]
                (loop [items []
                       stack node-stack]
                  (let [top (peek stack)]
                    (if (= (:kind top) :sequence-start)
                      ;; Found the start marker - return items and stack
                      [items stack]
                      ;; Collect item
                      (let [new-items (conj items top)
                            new-stack (pop stack)]
                        (recur new-items new-stack)))))
                ;; Create sequence node from collected items
                marker (peek final-stack)
                sequence (make-sequence-node (vec (reverse items))
                                            (:anchor marker)
                                            (:tag marker)
                                            (:flow marker))
                new-stack (conj (pop final-stack) sequence)]
            (recur rest-events new-stack anchor-stack nil nil documents in-document))

          ;; Default - skip unknown events
          (recur rest-events node-stack anchor-stack current-anchor current-tag documents in-document))))))

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
