(ns yamlstar.plugin.parser.snakeyaml
  "SnakeYAML parser plugin for YAMLStar.

  Wraps snakeyaml-engine's low level event parser and adapts its Java
  event objects to the standard YAMLStar event stream, so it can be
  selected with:

    (yamlstar.api/load yaml {:plugin {:parser {:name \"snakeyaml\"}}})"
  (:require [yamlstar.plugin :as plugin])
  (:import (java.util Optional)
           (org.snakeyaml.engine.v2.api LoadSettings)
           (org.snakeyaml.engine.v2.api.lowlevel Parse)
           (org.snakeyaml.engine.v2.common ScalarStyle)
           (org.snakeyaml.engine.v2.events
             Event
             AliasEvent
             ScalarEvent
             CollectionStartEvent
             DocumentStartEvent
             DocumentEndEvent
             MappingStartEvent
             MappingEndEvent
             SequenceStartEvent
             SequenceEndEvent
             StreamStartEvent
             StreamEndEvent)))

(defn- in-graalvm-native-image?
  []
  (= "runtime" (System/getProperty "org.graalvm.nativeimage.imagecode")))

(defn- require-graalvm-shared-lib
  "Fail unless this call is running inside a GraalVM native image."
  []
  (when-not (in-graalvm-native-image?)
    (throw (ex-info (str "SnakeYAML parser plugin is only available through "
                         "the GraalVM libyamlstar shared library")
                    {:parser "snakeyaml"}))))

(defn- optional-str
  "Return the string form of a non-empty Optional value, else nil."
  [^Optional o]
  (when-let [v (.orElse o nil)]
    (let [s (str v)]
      (when-not (= "" s) s))))

(defn- scalar-style
  "YAMLStar style string for a snakeyaml ScalarStyle, nil for plain."
  [^ScalarStyle style]
  (condp = style
    ScalarStyle/PLAIN nil
    ScalarStyle/SINGLE_QUOTED "single"
    ScalarStyle/DOUBLE_QUOTED "double"
    ScalarStyle/LITERAL "literal"
    ScalarStyle/FOLDED "folded"
    nil))

(defn- node-keys
  "Add :anchor and :tag to a start event map when present."
  [event-map anchor tag]
  (cond-> event-map
    anchor (assoc :anchor anchor)
    tag (assoc :tag tag)))

(defn- coll-start
  "Build a mapping_start or sequence_start event map."
  [name ^CollectionStartEvent e]
  (node-keys {:event name :flow (.isFlow e)}
             (optional-str (.getAnchor e))
             (optional-str (.getTag e))))

(defn- document-start [^DocumentStartEvent e]
  (cond-> {:event "document_start"}
    (.isExplicit e) (assoc :explicit true)
    (.isPresent (.getSpecVersion e))
    (assoc :version (let [v (.get (.getSpecVersion e))]
                      (str (.getMajor v) "." (.getMinor v))))))

(defn- scalar [^ScalarEvent e]
  (cond-> (node-keys {:event "scalar" :value (.getValue e)}
                     (optional-str (.getAnchor e))
                     (optional-str (.getTag e)))
    (scalar-style (.getScalarStyle e))
    (assoc :style (scalar-style (.getScalarStyle e)))))

(defn- event->map
  "Convert a snakeyaml event object to a YAMLStar event map.

  Returns nil for events with no YAMLStar equivalent (comments)."
  [^Event e]
  (condp instance? e
    StreamStartEvent {:event "stream_start"}
    StreamEndEvent {:event "stream_end"}
    DocumentStartEvent (document-start e)
    DocumentEndEvent (cond-> {:event "document_end"}
                       (.isExplicit ^DocumentEndEvent e)
                       (assoc :explicit true))
    MappingStartEvent (coll-start "mapping_start" e)
    MappingEndEvent {:event "mapping_end"}
    SequenceStartEvent (coll-start "sequence_start" e)
    SequenceEndEvent {:event "sequence_end"}
    ScalarEvent (scalar e)
    AliasEvent {:event "alias"
                :name (str (.getAlias ^AliasEvent e))}
    nil))

(defn parse
  "Parse a YAML string into a YAMLStar event stream using snakeyaml.

  The input is normalized to end with a newline, matching the reference
  parser, so both parsers produce identical event streams."
  [yaml-str _config]
  (require-graalvm-shared-lib)
  (let [yaml-str (or yaml-str "")
        yaml-str (if (or (= "" yaml-str)
                         (.endsWith ^String yaml-str "\n"))
                   yaml-str
                   (str yaml-str "\n"))
        parser (Parse. (.build (LoadSettings/builder)))]
    (into [] (keep event->map) (.parseString parser yaml-str))))

(def plugin
  {:name "snakeyaml"
   :parse parse
   :default-config {}})

(plugin/register-parser! plugin)
