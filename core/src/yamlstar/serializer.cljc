(ns yamlstar.serializer
  "Serialize YAMLStar nodes into YAML events."
  (:refer-clojure :exclude [serialize]))

(defn- serialize-node [node]
  (case (:kind node)
    :scalar
    [(cond-> {:event "scalar" :value (:value node)}
       (:anchor node) (assoc :anchor (:anchor node))
       (:tag node) (assoc :tag (:tag node))
       (:style node) (assoc :style (:style node)))]

    :mapping
    (vec (concat
          [(cond-> {:event "mapping_start"}
             (:anchor node) (assoc :anchor (:anchor node))
             (:tag node) (assoc :tag (:tag node))
             (:flow node) (assoc :flow (:flow node)))]
          (mapcat (fn [[k v]]
                    (concat (serialize-node k) (serialize-node v)))
                  (:value node))
          [{:event "mapping_end"}]))

    :sequence
    (vec (concat
          [(cond-> {:event "sequence_start"}
             (:anchor node) (assoc :anchor (:anchor node))
             (:tag node) (assoc :tag (:tag node))
             (:flow node) (assoc :flow (:flow node)))]
          (mapcat serialize-node (:value node))
          [{:event "sequence_end"}]))

    :alias
    [{:event "alias" :name (:name node)}]))

(defn serialize
  "Serialize one YAML node tree to an event stream."
  [node]
  (vec (concat
        [{:event "stream_start"} {:event "document_start"}]
        (when node (serialize-node node))
        [{:event "document_end"} {:event "stream_end"}])))

(defn serialize-all
  "Serialize multiple YAML node trees to an event stream."
  [nodes]
  (vec (concat
        [{:event "stream_start"}]
        (mapcat (fn [node]
                  (concat [{:event "document_start"}]
                          (when node (serialize-node node))
                          [{:event "document_end"}]))
                nodes)
        [{:event "stream_end"}])))
