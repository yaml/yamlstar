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

(defn- quote-single [s]
  (str "'" (str/replace s "'" "''") "'"))

(defn- core-tag? [tag]
  (contains? #{"!!null" "!!bool" "!!int" "!!float" "!!str"
               "!!map" "!!seq"
               "tag:yaml.org,2002:null"
               "tag:yaml.org,2002:bool"
               "tag:yaml.org,2002:int"
               "tag:yaml.org,2002:float"
               "tag:yaml.org,2002:str"
               "tag:yaml.org,2002:map"
               "tag:yaml.org,2002:seq"}
             tag))

(defn- format-tag [tag]
  (cond
    (nil? tag) nil
    (core-tag? tag) nil
    (str/starts-with? tag "!!") tag
    (str/starts-with? tag "!") tag
    (str/starts-with? tag "tag:yaml.org,2002:")
    (str "!!" (subs tag (count "tag:yaml.org,2002:")))
    (re-find #":" tag) (str "!<" tag ">")
    :else (str "!" tag)))

(defn- node-properties [event]
  (str/join " "
            (remove nil?
                    [(when-let [anchor (:anchor event)] (str "&" anchor))
                     (format-tag (:tag event))])))

(defn- with-properties [event text]
  (let [props (node-properties event)]
    (str/join " " (remove str/blank? [props text]))))

(defn- implicit-string? [value]
  (or (= value "")
      (re-matches #"null|Null|NULL|~" value)
      (re-matches #"true|True|TRUE|false|False|FALSE" value)
      (re-matches #"[-+]?[0-9]+" value)
      (re-matches #"[-+]?(\.[0-9]+|[0-9]+(\.[0-9]*)?)([eE][-+]?[0-9]+)?" value)
      (re-matches #"[+-]?(\.inf|\.Inf|\.INF)" value)
      (re-matches #"\.nan|\.NaN|\.NAN" value)))

(defn- plain-safe?
  ([value] (plain-safe? value nil))
  ([value tag]
   (and (not (str/blank? value))
        (or tag (not (implicit-string? value)))
        (not (re-find #"[#\[\]\{\},&*?:|>'\"%@`]" value))
        (not (re-find #"^\s|\s$" value))
        (not (re-find #"\r|\n|\t" value))
        (not (re-find #"^[-?](\s|$)" value)))))

(defn- trailing-newline-only? [value]
  (and (str/ends-with? value "\n")
       (= 1 (count (filter #{\newline} value)))))

(defn- legal-style? [style value tag]
  (case style
    "plain" (plain-safe? value tag)
    "single" (not (re-find #"\r|\n|\t" value))
    "double" true
    "literal" (str/includes? value "\n")
    "folded" (str/includes? value "\n")
    false))

(defn- scalar-style [event]
  (let [value (:value event)
        requested (or (:style event) "plain")]
    (cond
      (legal-style? requested value (:tag event)) requested
      (plain-safe? value (:tag event)) "plain"
      (trailing-newline-only? value) "double"
      (str/includes? value "\n") "literal"
      :else "single")))

(defn- chomped-lines [value]
  (str/split (if (str/ends-with? value "\n")
               (subs value 0 (dec (count value)))
               value)
             #"\n"
             -1))

(defn- block-scalar-body [value level]
  (apply str
         (map (fn [line] (str (indent level) line "\n"))
              (chomped-lines value))))

(defn- scalar-text [event]
  (let [value (:value event)]
    (case (scalar-style event)
      "plain" value
      "single" (quote-single value)
      "double" (quote-double value)
      "literal" "|"
      "folded" ">"
      (quote-single value))))

(defn- block-scalar? [event]
  (contains? #{"literal" "folded"} (scalar-style event)))

(defn- scalar-inline-text [event]
  (when-not (block-scalar? event)
    (with-properties event (scalar-text event))))

(defn- scalar-header [event]
  (with-properties event (scalar-text event)))

(defn- consume-node [events]
  (let [event-type (:event (first events))]
    (if (contains? #{"scalar" "alias"} event-type)
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

(defn- emit-scalar [event level prefix]
  (let [line-prefix (or prefix (indent level))]
    (if (block-scalar? event)
      (str line-prefix (scalar-header event) "\n"
           (block-scalar-body (:value event) (+ level 2)))
      (str line-prefix (scalar-header event) "\n"))))

(defn- emit-alias [event level prefix]
  (str (or prefix (indent level)) "*" (:name event) "\n"))

(defn- emit-key [events]
  (let [event (first events)]
    (if (and (= 1 (count events)) (= "scalar" (:event event)))
      (or (scalar-inline-text event) (quote-single (:value event)))
      "?")))

(defn- inline-text [events]
  (let [event (first events)]
    (cond
      (and (= 1 (count events)) (= "scalar" (:event event)))
      (scalar-inline-text event)

      (and (= 1 (count events)) (= "alias" (:event event)))
      (str "*" (:name event))

      (and (= 2 (count events))
           (= "mapping_start" (:event (first events)))
           (= "mapping_end" (:event (second events))))
      (with-properties event "{}")

      (and (= 2 (count events))
           (= "sequence_start" (:event (first events)))
           (= "sequence_end" (:event (second events))))
      (with-properties event "[]")

      :else nil)))

(defn- emit-collection-header [event level prefix]
  (let [props (node-properties event)]
    (when-not (str/blank? props)
      (str (or prefix (indent level)) props "\n"))))

(defn- emit-mapping [events level prefix]
  (let [start (first events)
        body (butlast (rest events))]
    (if (empty? body)
      (str (or prefix (indent level)) (with-properties start "{}") "\n")
      (let [props-header (emit-collection-header start level prefix)
            compact-prefix (when (and prefix (not props-header)) prefix)
            child-level (cond
                          props-header (+ level 2)
                          compact-prefix (+ level 2)
                          :else level)]
        (str props-header
             (loop [pairs (mapping-pairs body)
                    first? true
                    out []]
               (if (empty? pairs)
                 (apply str out)
                 (let [[[key-events val-events] & more] pairs
                       key-line-prefix (if (and first? compact-prefix)
                                         compact-prefix
                                         (indent child-level))
                       key-prefix (str key-line-prefix (emit-key key-events) ": ")
                       rendered
                        (cond
                          (inline-text val-events)
                          (str key-prefix (inline-text val-events) "\n")

                          (= "scalar" (:event (first val-events)))
                          (emit-node val-events child-level key-prefix)

                          (= "alias" (:event (first val-events)))
                          (emit-node val-events child-level key-prefix)

                          (= "sequence_start" (:event (first val-events)))
                          (str (indent child-level) (emit-key key-events) ":\n"
                               (emit-node val-events child-level nil))

                          :else
                          (str key-line-prefix (emit-key key-events) ":\n"
                               (emit-node val-events (+ child-level 2) nil)))]
                   (recur more false (conj out rendered))))))))))

(defn- emit-sequence [events level prefix]
  (let [start (first events)
        items (split-nodes (butlast (rest events)))]
    (if (empty? items)
      (str (or prefix (indent level)) (with-properties start "[]") "\n")
      (let [props-header (emit-collection-header start level prefix)
            child-level (cond
                          props-header (+ level 2)
                          prefix (count prefix)
                          :else level)
            first-prefix (when-not props-header prefix)]
        (loop [remaining items
               first? true
               out []]
          (if (empty? remaining)
            (apply str (cons props-header out))
            (let [item-events (first remaining)
                  item-prefix (if (and first? first-prefix)
                                (str first-prefix "- ")
                                (str (indent child-level) "- "))
                  next-level (if (and first? first-prefix)
                               (+ level (count first-prefix))
                               child-level)
                  rendered (if-let [value (inline-text item-events)]
                             (str item-prefix value "\n")
                             (emit-node item-events next-level item-prefix))]
              (recur (rest remaining) false (conj out rendered)))))))))

(defn- emit-node
  ([events level] (emit-node events level nil))
  ([events level prefix]
  (case (:event (first events))
    "scalar" (emit-scalar (first events) level prefix)
    "alias" (emit-alias (first events) level prefix)
    "mapping_start" (emit-mapping events level prefix)
    "sequence_start" (emit-sequence events level prefix))))

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
