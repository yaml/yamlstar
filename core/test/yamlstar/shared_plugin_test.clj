(ns yamlstar.shared-plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.plugin.shared :as shared]))

(def manifest
  {:abi 1
   :api "json-comments"
   :name "json-comments"
   :version "0.1.0"
   :kind "event-source"
   :requires {:parser "reference"}
   :event-format "yamlstar-events-edn-v1"})

(deftest shared-loader-test
  (let [loader (shared/make-loader
                (fn [_ _] (pr-str manifest))
                (fn [_ _ _ _]
                  [0 (pr-str [{:event "stream_start"}
                              {:event "stream_end"}])]))
        plugin (loader "json-comments" "json-comments")]
    (is (= manifest (:manifest plugin)))
    (is (= {:parser "reference"} (:requires plugin)))
    (is (= [{:event "stream_start"} {:event "stream_end"}]
           ((:parse plugin) "x" {})))))

(deftest manifest-validation-test
  (testing "ABI mismatch"
    (is (thrown-with-msg?
         Exception #"manifest abi"
         (shared/validate-manifest (assoc manifest :abi 2)
                                   "json-comments" "json-comments"))))
  (testing "API mismatch"
    (is (thrown-with-msg?
         Exception #"manifest api"
         (shared/validate-manifest manifest "other" "json-comments")))))

(deftest shared-error-test
  (let [loader (shared/make-loader
                (fn [_ _] (pr-str manifest))
                (fn [_ _ _ _]
                  [1 (pr-str {:error {:type "parse"
                                      :message "bad input"
                                      :data {:position 4}}})]))
        plugin (loader "json-comments" "json-comments")]
    (is (thrown-with-msg? Exception #"bad input"
                          ((:parse plugin) "x" {})))))
