(ns yamlstar.yaml-test-suite-test
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [yamlstar.core :as yaml]))

(def default-suite-dir "../yaml-test-suite")

(def expected-failures-resource
  "yamlstar/yaml_test_suite_expected_failures.edn")

(defn read-expected-failures []
  (-> expected-failures-resource
      io/resource
      slurp
      edn/read-string))

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

(defn expected-result [^java.io.File case-dir]
  (let [json-file (io/file case-dir "in.json")
        error-file (io/file case-dir "error")]
    (cond
      (.exists json-file)
      (let [content (slurp json-file)]
        (if-not (str/blank? content)
          {:kind :ok
           :value (json/read-str content)}
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

(defn run-case [^java.io.File case-dir expected]
  (let [input (slurp (io/file case-dir "in.yaml"))]
    (try
      (let [actual (yaml/load input)]
        (case (:kind expected)
          :ok (if (= (:value expected) actual)
                {:status :pass}
                {:status :fail
                 :reason :mismatch
                 :actual actual
                 :expected (:value expected)})
          :error {:status :fail
                  :reason :accepted-invalid
                  :actual actual}))
      (catch Throwable t
        (case (:kind expected)
          :ok {:status :fail
               :reason :rejected-valid
               :message (.getMessage t)}
          :error {:status :pass})))))

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

(deftest yaml-test-suite-loader-conformance
  (let [root (suite-root)
        selected (selected-case-ids)
        expected-failures (read-expected-failures)]
    (testing "yaml-test-suite fixture exists"
      (is (.isDirectory root)
          (str "Missing yaml-test-suite fixture at " root
               ". Run: make -C core test-suite")))
    (when (.isDirectory root)
      (let [case-dirs (->> (all-case-dirs root)
                           (filter #(selected? selected (relative-case-id root %))))
            results (mapv (fn [case-dir]
                            (let [id (relative-case-id root case-dir)
                                  expected (expected-result case-dir)]
                              (if (= :skip (:kind expected))
                                {:id id
                                 :name (test-name case-dir)
                                 :status :skip
                                 :reason (:reason expected)}
                                (merge {:id id
                                        :name (test-name case-dir)}
                                       (run-case case-dir expected)))))
                          case-dirs)
            summary (summarize results expected-failures)]
        (testing "selected cases exist"
          (is (seq results)
              (str "No yaml-test-suite cases matched "
                   (or selected "the suite"))))
        (testing "loader conformance summary"
          (println "yaml-test-suite:"
                   (:total summary) "cases,"
                   (:passed summary) "passed,"
                   (:expected-failures summary) "expected failures,"
                   (:skipped summary) "skipped"))
        (testing "no unexpected loader failures"
          (is (empty? (:unexpected-failures summary))
              (str "Unexpected yaml-test-suite failures:\n"
                   (str/join "\n" (map format-result
                                       (:unexpected-failures summary))))))
        (testing "expected-failure manifest is current"
          (is (empty? (:stale-expected-failures summary))
              (str "These cases now pass; remove them from "
                   expected-failures-resource
                   ":\n"
                   (str/join "\n" (map format-result
                                       (:stale-expected-failures summary))))))))))
