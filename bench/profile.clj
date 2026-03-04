(ns profile
  (:require [yamlstar.core :as yaml]))

(def inputs
  [["scalar"  "hello"]
   ["mapping" "foo: 42"]
   ["nested"  "root:\n  child1:\n    key: value\n  child2:\n  - item1\n  - item2\n  - item3"]
   ["types"   "string: hello\ninteger: 42\nfloat: 3.14\nbool: true\nnull_val: null"]])

(defn now-ns []
  (.UnixNano (time.Now)))

(defn -main []
  ;; Warmup — initialize all namespaces before profiling starts.
  ;; The Go-side profiler wrapper starts AFTER this returns.
  (yaml/load "warmup: true")

  (let [reps-str (os.Getenv "REPS")
        reps (if (not= reps-str "")
               (parse-long reps-str)
               5)]
    (println (fmt.Sprintf "Running %d iterations across 4 inputs (%d total parses)..."
               reps (* reps (count inputs))))

    (doseq [[label input] inputs]
      (let [t0 (now-ns)]
        (dotimes [_ reps]
          (yaml/load input))
        (let [elapsed-ms (/ (double (- (now-ns) t0)) 1000000.0)
              per-ms (/ elapsed-ms reps)]
          (println (fmt.Sprintf "  %-12s %7.1f ms total  %7.1f ms/call"
                     label elapsed-ms per-ms)))))

    (println "Done.")))
