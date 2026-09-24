(ns yamlstar.plugin.yaml-emitter.snakeyaml
  "SnakeYAML Engine event emitter plugin for YAMLStar."
  (:require [yamlstar.plugin :as plugin])
  (:import (java.util Collections Optional)
           (org.snakeyaml.engine.v2.api DumpSettings)
           (org.snakeyaml.engine.v2.api.lowlevel Present)
           (org.snakeyaml.engine.v2.common
             Anchor FlowStyle ScalarStyle SpecVersion)
           (org.snakeyaml.engine.v2.events
             AliasEvent DocumentEndEvent DocumentStartEvent ImplicitTuple
             MappingEndEvent MappingStartEvent ScalarEvent SequenceEndEvent
             SequenceStartEvent StreamEndEvent StreamStartEvent)))

(defn- optional [value]
  (if (nil? value) (Optional/empty) (Optional/of value)))

(defn- anchor [value]
  (optional (when value (Anchor. value))))

(defn- version [value]
  (optional
   (case value
     "1.1" (SpecVersion. 1 1)
     "1.2" (SpecVersion. 1 2)
     nil)))

(defn- flow-style [event]
  (if (:flow event) FlowStyle/FLOW FlowStyle/BLOCK))

(defn- scalar-style [event]
  (case (:style event)
    "single" ScalarStyle/SINGLE_QUOTED
    "double" ScalarStyle/DOUBLE_QUOTED
    "literal" ScalarStyle/LITERAL
    "folded" ScalarStyle/FOLDED
    ScalarStyle/PLAIN))

(defn- event-object [event multi?]
  (case (:event event)
    "stream_start" (StreamStartEvent.)
    "stream_end" (StreamEndEvent.)
    "document_start"
    (DocumentStartEvent. (or multi? (true? (:explicit event)))
                         (version (:version event))
                         (Collections/emptyMap))
    "document_end" (DocumentEndEvent. (true? (:explicit event)))
    "mapping_start"
    (MappingStartEvent. (anchor (:anchor event))
                        (optional (:tag event))
                        (nil? (:tag event))
                        (flow-style event))
    "mapping_end" (MappingEndEvent.)
    "sequence_start"
    (SequenceStartEvent. (anchor (:anchor event))
                         (optional (:tag event))
                         (nil? (:tag event))
                         (flow-style event))
    "sequence_end" (SequenceEndEvent.)
    "scalar"
    (ScalarEvent. (anchor (:anchor event))
                  (optional (:tag event))
                  (ImplicitTuple. (nil? (:tag event))
                                  (nil? (:tag event)))
                  (or (:value event) "")
                  (scalar-style event))
    "alias" (AliasEvent. (anchor (:name event)))))

(defn emit
  [events multi? _config]
  (let [settings (.build (DumpSettings/builder))
        presenter (Present. settings)
        objects (mapv #(event-object % multi?) events)]
    (.emitToString presenter (.iterator ^java.lang.Iterable objects))))

(def plugin
  {:name "snakeyaml"
   :emit emit
   :default-config {}})

(plugin/register-yaml-emitter! plugin)
