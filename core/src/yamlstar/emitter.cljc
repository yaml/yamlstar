(ns yamlstar.emitter
  "Emit YAML events as a YAML string."
  (:require [clojure.string :as str]))

(declare emit-node)

(defn- indent [n]
  (apply str (repeat n " ")))

(defn- quote-double [s]
  (str "\""
       (str/escape s {\\ "\\\\"
                      \" "\\\""
                      \newline "\\n"
                      \return "\\r"
                      \tab "\\t"})
       "\""))

(defn- scalar-text [event]
  (if (= (:style event) "double")
    (quote-double (:value event))
    (:value event)))

(defn- consume-node [events]
  (let [event-type (:event (first events))]
    (if (= "scalar" event-type)
      [(take 1 events) (drop 1 events)]
      (let [[start-event end-event] (case event-type
                                      "mapping_start" ["mapping_start" "mapping_end"]
                                      "sequence_start" ["sequence_start" "sequence_end"])
            [node-events remaining]
            (loop [remaining events
                   depth 0
                   current []]
              (let [event (first remaining)
                    event-type (:event event)
                    depth' (cond
                             (= event-type start-event) (inc depth)
                             (= event-type end-event) (dec depth)
                             :else depth)
                    current' (conj current event)]
                (if (zero? depth')
                  [current' (rest remaining)]
                  (recur (rest remaining) depth' current'))))]
        [node-events remaining]))))

(defn- split-nodes [events]
  (loop [remaining events
         groups []]
    (if (empty? remaining)
      groups
      (let [[node remaining] (consume-node remaining)]
        (recur remaining (conj groups (vec node)))))))

(defn- mapping-pairs [events]
  (loop [remaining events
         pairs []]
    (if (empty? remaining)
      pairs
      (let [[key-events remaining] (consume-node remaining)
            [val-events remaining] (consume-node remaining)]
        (recur remaining (conj pairs [key-events val-events]))))))

(defn- emit-scalar-line [event level]
  (str (indent level) (scalar-text event) "\n"))

(defn- emit-key [events]
  (let [event (first events)]
    (if (and (= 1 (count events)) (= "scalar" (:event event)))
      (scalar-text event)
      "?")))

(defn- inline-text [events]
  (let [event (first events)]
    (cond
      (and (= 1 (count events)) (= "scalar" (:event event)))
      (scalar-text event)

      (and (= 2 (count events))
           (= "mapping_start" (:event (first events)))
           (= "mapping_end" (:event (second events))))
      "{}"

      (and (= 2 (count events))
           (= "sequence_start" (:event (first events)))
           (= "sequence_end" (:event (second events))))
      "[]"

      :else nil)))

(defn- emit-mapping [events level]
  (let [body (butlast (rest events))]
    (if (empty? body)
      (str (indent level) "{}\n")
      (apply str
             (for [[key-events val-events] (mapping-pairs body)]
               (if-let [value (inline-text val-events)]
                 (str (indent level) (emit-key key-events) ": " value "\n")
                 (str (indent level) (emit-key key-events) ":\n"
                      (emit-node val-events (+ level 2)))))))))

(defn- emit-sequence [events level]
  (let [items (split-nodes (butlast (rest events)))]
    (if (empty? items)
      (str (indent level) "[]\n")
      (apply str
             (for [item-events items]
               (if-let [value (inline-text item-events)]
                 (str (indent level) "- " value "\n")
                 (str (indent level) "-\n" (emit-node item-events (+ level 2)))))))))

(defn- emit-node [events level]
  (case (:event (first events))
    "scalar" (emit-scalar-line (first events) level)
    "mapping_start" (emit-mapping events level)
    "sequence_start" (emit-sequence events level)))

(defn- document-event-groups [events]
  (loop [remaining events
         groups []]
    (if (empty? remaining)
      groups
      (let [event (first remaining)]
        (case (:event event)
          "stream_start" (recur (rest remaining) groups)
          "stream_end" groups
          "document_start"
          (let [doc-events (take-while #(not= "document_end" (:event %)) (rest remaining))
                remaining (drop (+ 2 (count doc-events)) remaining)]
            (recur remaining (conj groups (vec doc-events))))
          (recur (rest remaining) groups))))))

(defn emit
  "Emit one or more serialized documents as YAML."
  ([events] (emit events false))
  ([events multi?]
   (let [docs (document-event-groups events)]
     (if multi?
       (apply str
              (map (fn [doc]
                     (str "---\n" (if (seq doc) (emit-node doc 0) "null\n")))
                   docs))
       (if-let [doc (first docs)]
         (if (seq doc) (emit-node doc 0) "null\n")
         "null\n")))))
