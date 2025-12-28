(ns yamlstar.parser
  "YAML 1.2 event-based parser

  This namespace integrates the pure Clojure YAML parser from
  https://github.com/yaml/yaml-reference-parser/tree/main/parser-1.2/clojure

  The parser emits a stream of events representing the YAML document structure:
  - stream_start, stream_end
  - document_start, document_end
  - mapping_start, mapping_end
  - sequence_start, sequence_end
  - scalar
  - alias

  Events include metadata like anchors, tags, styles, etc."
  (:require [clojure.string :as str]
            [yamlstar.parser.parser :as p]
            [yamlstar.parser.grammar :as grammar]
            [yamlstar.parser.receiver :as r]))

(defn parse
  "Parse a YAML string into an event stream.

  Args:
    yaml-str: A string containing YAML content

  Returns:
    A sequence of event maps representing the YAML structure

  Example event:
    {:event \"scalar\" :value \"hello\" :style \"plain\"}
    {:event \"mapping_start\" :flow false}"
  [yaml-str]
  (let [receiver (r/make-receiver-with-callbacks)
        parser (p/make-parser receiver)
        ;; Normalize input - ensure it ends with newline
        input (if (or (empty? yaml-str) (str/ends-with? yaml-str "\n"))
                yaml-str
                (str yaml-str "\n"))]
    ;; Reset parser state
    (reset! (:input parser) input)
    (reset! (:end parser) (count input))
    (reset! (:pos parser) 0)
    (reset! (:state parser) [])

    (when p/TRACE
      (reset! (:trace-on parser) (not (p/trace-start parser))))

    ;; Parse using grammar - no requiring-resolve needed!
    (try
      (let [ok (p/call parser grammar/TOP)]
        (p/trace-flush parser)
        (when-not ok
          (throw (ex-info "Parser failed" {})))
        (when (< @(:pos parser) @(:end parser))
          (throw (ex-info "Parser finished before end of input" {})))
        @(:events receiver))
      (catch Exception e
        (p/trace-flush parser)
        (throw e)))))
