(ns yamlstar.yaml-test-suite-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [yamlstar.core :as yaml]))

(def default-suite-dir "../yaml-test-suite")

(def loader-expected-failures-resource
  "expected-fails/load.yaml")

(def roundtrip-expected-failures-resource
  "expected-fails/roundtrip.yaml")

(def emit-expected-failures-resource
  "expected-fails/emit.yaml")

(defn read-expected-failures [resource-name]
  (if-let [resource (io/resource resource-name)]
    (->> (yaml/load (slurp resource))
         (map (fn [[id reason]]
                [id (keyword reason)]))
         (into {}))
    {}))

(defn suite-root []
  (io/file (or (System/getenv "YAMLSTAR_TEST_SUITE_DIR")
               default-suite-dir)))

(defn test-id-dir? [^java.io.File file]
  (and (.isDirectory file)
       (re-matches #"[A-Z0-9]{4}" (.getName file))))

(defn case-dirs [^java.io.File id-dir]
  (let [direct-input (io/file id-dir "in.yaml")
        subtests (->> (.listFiles id-dir)
                      (filter #(.isDirectory ^java.io.File %))
                      (filter #(re-matches #"[0-9]{2}" (.getName ^java.io.File %)))
                      (sort-by #(.getName ^java.io.File %)))]
    (if (.exists direct-input)
      [id-dir]
      subtests)))

(defn relative-case-id [^java.io.File root ^java.io.File case-dir]
  (subs (.getPath case-dir) (inc (count (.getPath root)))))

(defn selected-case-ids []
  (when-let [raw (System/getenv "YAMLSTAR_TEST_SUITE_CASES")]
    (->> (str/split raw #"[,\s]+")
         (remove str/blank?)
         set)))

(defn selected? [selected id]
  (or (nil? selected)
      (contains? selected id)
      (contains? selected (first (str/split id #"/")))))

(defn all-case-dirs [^java.io.File root]
  (->> (.listFiles root)
       (filter test-id-dir?)
       (sort-by #(.getName ^java.io.File %))
       (mapcat case-dirs)))

(defn next-json-document? [^java.io.PushbackReader reader]
  (loop [ch (.read reader)]
    (cond
      (= -1 ch) false
      (Character/isWhitespace (char ch)) (recur (.read reader))
      :else (do (.unread reader ch)
                true))))

(defn json-documents [content]
  (with-open [reader (java.io.PushbackReader.
                      (java.io.StringReader. content)
                      (max 1 (count content)))]
    (loop [docs []]
      (if-not (next-json-document? reader)
        docs
        (recur (conj docs (json/read reader)))))))

(defn document-count [^java.io.File case-dir]
  (let [event-file (io/file case-dir "test.event")]
    (if (.exists event-file)
      (count (re-seq #"(?m)^\+DOC$" (slurp event-file)))
      1)))

(defn expected-result [^java.io.File case-dir]
  (let [json-file (io/file case-dir "in.json")
        error-file (io/file case-dir "error")]
    (cond
      (.exists json-file)
      (let [content (slurp json-file)]
        (if-not (str/blank? content)
          {:kind :ok
           :documents (json-documents content)}
          {:kind :skip
           :reason :blank-json-expectation}))

      (.exists error-file)
      {:kind :error}

      :else
      {:kind :skip
       :reason :no-loader-expectation})))

(defn test-name [^java.io.File case-dir]
  (let [name-file (io/file case-dir "===")]
    (when (.exists name-file)
      (str/trim (slurp name-file)))))

(defn load-documents [input document-count]
  (if (> document-count 1)
    (vec (yaml/load-all input))
    [(yaml/load input)]))

(defn dump-documents [documents]
  (if (> (count documents) 1)
    (yaml/dump-all documents)
    (yaml/dump (first documents))))

(defn normalize-newlines [s]
  (str/replace s #"\r\n?" "\n"))

(defn selected-case-dirs [root selected]
  (->> (all-case-dirs root)
       (filter #(selected? selected (relative-case-id root %)))))

(defn run-case [^java.io.File case-dir expected]
  (if (= :skip (:kind expected))
    {:status :skip
     :reason (:reason expected)}
    (let [input (slurp (io/file case-dir "in.yaml"))
          document-count (document-count case-dir)]
      (try
        (let [actual (load-documents input document-count)]
          (case (:kind expected)
            :ok (if (= (:documents expected) actual)
                  {:status :pass}
                  {:status :fail
                   :reason :mismatch
                   :actual actual
                   :expected (:documents expected)})
            :error {:status :fail
                    :reason :accepted-invalid
                    :actual actual}))
        (catch Throwable t
          (case (:kind expected)
            :ok {:status :fail
                 :reason :rejected-valid
                 :message (.getMessage t)}
            :error {:status :pass}))))))

(defn roundtrip-case [^java.io.File case-dir expected]
  (let [input (slurp (io/file case-dir "in.yaml"))
        document-count (document-count case-dir)]
    (if-not (= :ok (:kind expected))
      {:status :skip
       :reason (or (:reason expected) :not-json-compatible)}
      (try
        (let [documents (load-documents input document-count)
              dumped (dump-documents documents)
              reloaded (load-documents dumped (count documents))]
          (cond
            (not= (:documents expected) documents)
            {:status :fail
             :reason :loader-mismatch}

            (= (:documents expected) reloaded)
            {:status :pass}

            :else
            {:status :fail
             :reason :roundtrip-mismatch
             :actual reloaded
             :expected (:documents expected)}))
        (catch Throwable t
          {:status :fail
           :reason :roundtrip-error
           :message (.getMessage t)})))))

(defn emit-fixture-file [^java.io.File case-dir]
  (let [emit-file (io/file case-dir "emit.yaml")
        out-file (io/file case-dir "out.yaml")]
    (cond
      (.exists emit-file) emit-file
      (.exists out-file) out-file
      :else nil)))

(defn emit-case [^java.io.File case-dir expected]
  (let [fixture-file (emit-fixture-file case-dir)]
    (cond
      (nil? fixture-file)
      {:status :skip
       :reason :no-emit-fixture}

      (not= :ok (:kind expected))
      {:status :skip
       :reason (or (:reason expected) :not-json-compatible)}

      :else
      (try
        (let [actual (normalize-newlines (dump-documents (:documents expected)))
              expected-yaml (normalize-newlines (slurp fixture-file))]
          (if (= expected-yaml actual)
            {:status :pass}
            {:status :fail
             :reason :emit-mismatch
             :actual actual
             :expected expected-yaml}))
        (catch Throwable t
          {:status :fail
           :reason :emit-error
           :message (.getMessage t)})))))

(defn summarize [results expected-failures]
  (let [unexpected-failures (filter #(and (= :fail (:status %))
                                          (not (contains? expected-failures (:id %))))
                                    results)
        expected-failures-seen (filter #(and (= :fail (:status %))
                                             (contains? expected-failures (:id %)))
                                       results)
        stale-expected-failures (filter #(and (= :pass (:status %))
                                              (contains? expected-failures (:id %)))
                                        results)
        skipped (filter #(= :skip (:status %)) results)]
    {:total (count results)
     :passed (count (filter #(= :pass (:status %)) results))
     :skipped (count skipped)
     :expected-failures (count expected-failures-seen)
     :unexpected-failures unexpected-failures
     :stale-expected-failures stale-expected-failures}))

(defn format-result [{:keys [id name reason message]}]
  (str id
       (when name (str " (" name ")"))
       ": " reason
       (when message (str " - " message))))

(defn result-for-case [root run-fn case-dir]
  (let [id (relative-case-id root case-dir)
        expected (expected-result case-dir)]
    (merge {:id id
            :name (test-name case-dir)}
           (run-fn case-dir expected))))

(defn run-suite-test [label expected-failures-resource run-fn]
  (let [root (suite-root)
        selected (selected-case-ids)
        expected-failures (read-expected-failures expected-failures-resource)]
    (testing "yaml-test-suite fixture exists"
      (is (.isDirectory root)
          (str "Missing yaml-test-suite fixture at " root
               ". Run: make -C core test-suite")))
    (when (.isDirectory root)
      (let [case-dirs (selected-case-dirs root selected)
            results (mapv #(result-for-case root run-fn %) case-dirs)
            summary (summarize results expected-failures)]
        (testing "selected cases exist"
          (is (seq results)
              (str "No yaml-test-suite cases matched "
                   (or selected "the suite"))))
        (testing (str label " summary")
          (println (str "yaml-test-suite " label ":")
                   (:total summary) "cases,"
                   (:passed summary) "passed,"
                   (:expected-failures summary) "expected failures,"
                   (:skipped summary) "skipped"))
        (testing (str "no unexpected " label " failures")
          (is (empty? (:unexpected-failures summary))
              (str "Unexpected yaml-test-suite " label " failures:\n"
                   (str/join "\n" (map format-result
                                       (:unexpected-failures summary))))))
        (testing "expected-failure manifest is current"
          (is (empty? (:stale-expected-failures summary))
              (str "These cases now pass; remove them from "
                   expected-failures-resource
                   ":\n"
                   (str/join "\n" (map format-result
                                       (:stale-expected-failures summary))))))))))

(deftest yaml-test-suite-loader-conformance
  (run-suite-test "loader"
                  loader-expected-failures-resource
                  run-case))

(deftest yaml-test-suite-roundtrip-conformance
  (run-suite-test "roundtrip"
                  roundtrip-expected-failures-resource
                  roundtrip-case))

(deftest yaml-test-suite-emit-conformance
  (run-suite-test "emit"
                  emit-expected-failures-resource
                  emit-case))
