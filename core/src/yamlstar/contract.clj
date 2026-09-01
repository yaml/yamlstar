(ns yamlstar.contract
  "The go-yaml/YAMLStar CLI interchange contract."
  (:require [clojure.string :as str]
            [yamlstar.api :as yaml]
            [yamlstar.composer :as composer]
            [yamlstar.emitter :as emitter]
            [yamlstar.parser :as parser]
            [yamlstar.resolver :as resolver]
            [yamlstar.serializer :as serializer]))

(def stages {:token 1 :event 2 :node 3 :yaml 4})

(defn parse-stage [value]
  (let [value (some-> value str/lower-case)]
    (cond
      (contains? #{nil "" "y" "yaml"} value) :yaml
      (contains? #{"t" "token" "tokens"} value) :token
      (contains? #{"e" "event" "events"} value) :event
      (contains? #{"n" "node" "nodes"} value) :node
      :else (throw (ex-info (str "unknown input stage '" value
                                 "' (use token, event, node, or yaml)") {})))))

(defn field [mapping name]
  (if (contains? mapping name)
    (get mapping name)
    (get mapping (keyword name))))

(defn has-field? [mapping name]
  (or (contains? mapping name) (contains? mapping (keyword name))))

(defn contract-stream? [value discriminator start end]
  (and (sequential? value)
       (<= 2 (count value))
       (every? map? value)
       (= start (field (first value) discriminator))
       (= end (field (last value) discriminator))))

(defn token-contract? [value]
  (contract-stream? value "token" "STREAM-START" "STREAM-END"))

(defn event-contract? [value]
  (contract-stream? value "event" "STREAM-START" "STREAM-END"))

(def node-shapes
  #{"mapping" "sequence" "plain" "double" "single"
    "literal" "folded" "alias" "stream"})

(defn compact-node? [value]
  (and (map? value)
       (= 1 (count (filter #(has-field? value %) node-shapes)))))

(defn detailed-node? [value]
  (and (map? value)
       (contains? #{"Document" "Mapping" "Sequence" "Scalar" "Alias" "Stream"}
                  (field value "kind"))))

(defn node-contract? [value]
  (or (compact-node? value)
      (detailed-node? value)
      (and (sequential? value)
           (seq value)
           (every? #(or (compact-node? %) (detailed-node? %)) value))))

(defn read-contract
  ([source forced]
   (read-contract source forced nil))
  ([source forced opts]
   (let [stage (when forced (parse-stage forced))
         value (when (or (nil? stage) (not= stage :yaml))
                 (yaml/load source opts))
         detected (cond
                    stage stage
                    (token-contract? value) :token
                    (event-contract? value) :event
                    (node-contract? value) :node
                    :else :yaml)]
     (when (and (= detected :token) (not (token-contract? value)))
       (throw (ex-info "invalid token input: stream boundaries are required" {})))
     (when (and (= detected :event) (not (event-contract? value)))
       (throw (ex-info "invalid event input: stream boundaries are required" {})))
     (when (and (= detected :node) (not (node-contract? value)))
       (throw (ex-info "invalid node input" {})))
     {:stage detected :value value :source source})))

(defn title-style [style]
  (when style
    (str/capitalize (name style))))

(defn lower-style [style]
  (when style
    (str/lower-case (name style))))

(defn internal-event->contract [event]
  (let [type (:event event)
        style (:style event)
        implicit (not (:explicit event))]
    (cond-> (array-map "event" (str/upper-case (str/replace type "_" "-")))
      (= type "stream_start") (assoc "encoding" "UTF-8")
      (= type "document_start") (assoc "implicit" implicit)
      (= type "document_end") (assoc "implicit" implicit)
      (:version event) (assoc "version" (:version event))
      (:value event) (assoc "value" (:value event))
      style (assoc "style" (title-style style))
      (and (contains? #{"mapping_start" "sequence_start"} type) (:flow event))
      (assoc "style" "Flow")
      (:tag event) (assoc "tag" (:tag event))
      (:anchor event) (assoc "anchor" (:anchor event))
      (= type "alias") (assoc "anchor" (:name event))
      (contains? #{"mapping_start" "sequence_start" "scalar"} type)
      (assoc "implicit" (nil? (:tag event)))
      (= type "scalar")
      (assoc "quoted-implicit" (contains? #{"single" "double"} style))
      (:head event) (assoc "head" (:head event))
      (:line event) (assoc "line" (:line event))
      (:foot event) (assoc "foot" (:foot event))
      (:tail event) (assoc "tail" (:tail event)))))

(def event-types
  #{"stream_start" "stream_end" "document_start" "document_end"
    "mapping_start" "mapping_end" "sequence_start" "sequence_end"
    "scalar" "alias" "tail_comment"})

(defn contract-event->internal [event]
  (let [type (-> (field event "event") str/lower-case (str/replace "-" "_"))
        style (lower-style (field event "style"))
        implicit (if (has-field? event "implicit")
                   (boolean (field event "implicit"))
                   true)]
    (when-not (contains? event-types type)
      (throw (ex-info (str "unknown event '" (field event "event") "'") {})))
    (cond-> {:event type}
      (contains? #{"document_start" "document_end"} type)
      (assoc :explicit (not implicit))
      (field event "version") (assoc :version (field event "version"))
      (has-field? event "value") (assoc :value (str (field event "value")))
      (and style (not= style "flow")) (assoc :style style)
      (and (= style "flow") (contains? #{"mapping_start" "sequence_start"} type))
      (assoc :flow true)
      (field event "tag") (assoc :tag (field event "tag"))
      (and (field event "anchor") (not= type "alias"))
      (assoc :anchor (field event "anchor"))
      (= type "alias") (assoc :name (field event "anchor"))
      (field event "head") (assoc :head (field event "head"))
      (field event "line") (assoc :line (field event "line"))
      (field event "foot") (assoc :foot (field event "foot"))
      (field event "tail") (assoc :tail (field event "tail")))))

(defn contract-events [events]
  (mapv contract-event->internal events))

(defn event-contract [events]
  (mapv internal-event->contract events))

(defn node-style [node]
  (cond
    (:flow node) "Flow"
    (:style node) (title-style (:style node))
    :else "Plain"))

(declare internal-node->detailed internal-node->compact)

(defn internal-node->detailed [node]
  (let [kind (:kind node)
        content (case kind
                  :mapping (vec (mapcat identity (:value node)))
                  :sequence (:value node)
                  nil)]
    (cond-> (array-map "kind" (str/capitalize (name kind)))
      (contains? #{:mapping :sequence :scalar} kind)
      (assoc "style" (node-style node))
      (:anchor node) (assoc "anchor" (:anchor node))
      (:tag node) (assoc "tag" (:tag node))
      (= kind :alias) (assoc "value" (:name node))
      (= kind :scalar) (assoc "value" (:value node))
      content (assoc "content" (mapv internal-node->detailed content)))))

(defn wrap-detailed-document [node]
  (array-map "kind" "Document" "content" [(internal-node->detailed node)]))

(defn internal-node->compact [node]
  (let [base (cond-> (array-map)
               (:anchor node) (assoc "anchor" (:anchor node))
               (:tag node) (assoc "tag" (:tag node)))]
    (case (:kind node)
      :mapping (assoc base "mapping"
                      (mapv internal-node->compact (mapcat identity (:value node))))
      :sequence (assoc base "sequence" (mapv internal-node->compact (:value node)))
      :scalar (assoc base (or (:style node) "plain") (:value node))
      :alias (assoc base "alias" (:name node)))))

(defn node-contract [nodes profuse]
  (let [values (mapv #(if profuse
                        (wrap-detailed-document (resolver/resolve %))
                        (internal-node->compact %))
                     nodes)]
    (if (= 1 (count values)) (first values) values)))

(defn pairs [values]
  (when (odd? (count values))
    (throw (ex-info "mapping node content must contain key/value pairs" {})))
  (mapv vec (partition 2 values)))

(declare detailed->internal compact->internal)

(defn detailed->internal [node]
  (let [kind (some-> (field node "kind") str/lower-case keyword)
        content (mapv detailed->internal (or (field node "content") []))
        base (cond-> {:kind kind}
               (field node "anchor") (assoc :anchor (field node "anchor"))
               (field node "tag") (assoc :tag (field node "tag")))]
    (case kind
      :document (first content)
      :stream (first content)
      :mapping (assoc base :value (pairs content)
                      :flow (= "flow" (lower-style (field node "style"))))
      :sequence (assoc base :value content
                       :flow (= "flow" (lower-style (field node "style"))))
      :scalar (cond-> (assoc base :value (str (or (field node "value") "")))
                (and (field node "style")
                     (not= "plain" (lower-style (field node "style"))))
                (assoc :style (lower-style (field node "style"))))
      :alias {:kind :alias :name (field node "value")}
      (throw (ex-info (str "unknown node kind '" (field node "kind") "'") {})))))

(defn compact->internal [node]
  (let [shape (first (filter #(has-field? node %) node-shapes))
        value (field node shape)
        base (cond-> {}
               (field node "anchor") (assoc :anchor (field node "anchor"))
               (field node "tag") (assoc :tag (field node "tag")))]
    (case shape
      "mapping" (assoc base :kind :mapping
                       :value (pairs (mapv compact->internal value)))
      "sequence" (assoc base :kind :sequence
                        :value (mapv compact->internal value))
      "alias" (assoc base :kind :alias :name value)
      "stream" nil
      (if (contains? #{"plain" "double" "single" "literal" "folded"} shape)
        (cond-> (assoc base :kind :scalar :value (str (or value "")))
          (not= shape "plain") (assoc :style shape))
        (throw (ex-info "invalid compact node" {}))))))

(defn contract-nodes [value]
  (let [values (if (and (sequential? value) (not (map? value))) value [value])]
    (->> values
         (map #(if (detailed-node? %)
                 (detailed->internal %)
                 (compact->internal %)))
         (remove nil?)
         (mapv resolver/resolve))))

(defn yaml-events
  ([source]
   (yaml-events source nil))
  ([source opts]
   (parser/parse source opts)))

(defn events-nodes [events]
  (vec (composer/compose-all events)))

(defn events-yaml [events]
  (let [documents (count (filter #(= "document_start" (:event %)) events))]
    (emitter/emit events (> documents 1))))

(defn nodes-yaml [nodes]
  (emitter/emit (serializer/serialize-all nodes) (> (count nodes) 1)))

(defn yaml-value
  ([source stream?]
   (yaml-value source stream? nil))
  ([source stream? opts]
   (if stream? (yaml/load-all source opts) (yaml/load source opts))))

(defn yaml-output
  ([source preserve? stream?]
   (yaml-output source preserve? stream? nil))
  ([source preserve? stream? opts]
   (if preserve?
     (events-yaml (yaml-events source opts))
     (if stream?
       (yaml/dump-all (yaml/load-all source opts))
       (yaml/dump (yaml/load source opts))))))

(defn check-forward! [from to]
  (when (< (stages to) (stages from))
    (throw (ex-info (str "cannot convert " (name from)
                         " input backward to " (name to) " output") {}))))
