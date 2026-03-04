(ns bench2
  (:require [yamlstar.core :as yaml]))

(def inputs
  [["scalar"  "hello"]
   ["mapping" "foo: 42"]
   ["nested"  "root:\n  child1:\n    key: value\n  child2:\n  - item1\n  - item2\n  - item3"]
   ["types"   "string: hello\ninteger: 42\nfloat: 3.14\nbool: true\nnull_val: null"]])

(defn now-ns []
  (.UnixNano (time.Now)))

(defn fmt-time [ns]
  (let [ms (/ (double ns) 1000000.0)]
    (if (>= ms 1000)
      (fmt.Sprintf "%8.2f s " (/ ms 1000))
      (if (< ms 1)
        (fmt.Sprintf "%7.3f ms" ms)
        (fmt.Sprintf "%7.1f ms" ms)))))

(defn -main []
  ;; Cold call
  (let [t0 (now-ns)]
    (yaml/load "warmup: true")
    (println (str "cold:    " (fmt-time (- (now-ns) t0))
                  "  (first call, includes ns init)")))

  (println)
  (println (fmt.Sprintf "%-12s %12s" "input" "time"))
  (println (apply str (repeat 26 "-")))

  ;; Single call per input
  (let [limit-str (os.Getenv "LIMIT")
        limit-ms (when (not= limit-str "")
                   (parse-long limit-str))]
    (loop [remaining inputs]
      (when (seq remaining)
        (let [[label input] (first remaining)
              t0 (now-ns)
              _ (yaml/load input)
              elapsed-ns (- (now-ns) t0)
              elapsed-ms (/ (double elapsed-ns) 1000000.0)]
          (println (fmt.Sprintf "%-12s %s" label (fmt-time elapsed-ns)))
          (if (and (= label "scalar") limit-ms (> elapsed-ms limit-ms))
            (println (fmt.Sprintf "(skipping remaining — scalar exceeded %d ms limit)" limit-ms))
            (recur (rest remaining)))))))

  ;; Bulk test - adaptive reps
  (let [t0 (now-ns)
        _ (yaml/load "foo: 42")
        probe-ms (/ (double (- (now-ns) t0)) 1000000.0)
        reps (cond (< probe-ms 10) 100
                   (< probe-ms 500) 10
                   :else 3)
        t0 (now-ns)]
    (dotimes [_ reps]
      (yaml/load "foo: 42"))
    (let [total-ns (- (now-ns) t0)
          total-ms (/ (double total-ns) 1000000.0)
          per-ms (/ total-ms reps)]
      (println)
      (println (fmt.Sprintf "%d x 'foo: 42':  %s total,  %s/call"
                 reps (fmt-time total-ns) (fmt-time (long (/ total-ns reps))))))))
