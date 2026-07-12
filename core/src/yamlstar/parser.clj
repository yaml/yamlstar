(ns yamlstar.parser
  "YAMLStar parser compatibility facade."
  (:require [yaml-parser.core :as parser]))

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
  (parser/parse yaml-str))
