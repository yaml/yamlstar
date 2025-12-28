(ns yamlstar.parser.core
  (:require [clojure.string :as str]
            [yamlstar.parser.parser :as p]
            [yamlstar.parser.receiver :as r]
            [yamlstar.parser.test-receiver :as tr]
            [yamlstar.parser.grammar :as g]))

(defn parse-yaml
  "Parse a YAML string and return events."
  [yaml-str]
  (let [receiver (tr/make-test-receiver)
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
      (let [ok (p/call parser g/TOP)]
        (p/trace-flush parser)
        (when-not ok
          (throw (ex-info "Parser failed" {})))
        (when (< @(:pos parser) @(:end parser))
          (throw (ex-info "Parser finished before end of input" {})))
        (tr/output receiver))
      (catch Exception e
        (p/trace-flush parser)
        (throw e)))))

(defn -main [& args]
  (let [input (slurp *in*)]
    (println (parse-yaml input))))
