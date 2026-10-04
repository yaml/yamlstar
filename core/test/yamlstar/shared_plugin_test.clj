(ns yamlstar.shared-plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.plugin.shared :as shared]))

(def manifest
  {:abi 2
   :api "json-comments"
   :name "sanitizer"
   :version "0.1.9"
   :kind "text-transform"})

(deftest shared-loader-test
  (let [loader (shared/make-loader
                (fn [_ _ _] (pr-str manifest))
                (fn [_ _ input _]
                  [0 (.replace input "// comment" "")]))
        plugin (loader "json-comments" "sanitizer" nil false)]
    (is (= manifest (:manifest plugin)))
    (is (= "0.1.9" (:version plugin)))
    (is (= "a: true "
           ((:sanitize plugin) "a: true // comment" {})))))

(deftest manifest-validation-test
  (testing "ABI mismatch"
    (is (thrown-with-msg?
         Exception #"manifest abi"
         (shared/validate-manifest (assoc manifest :abi 1)
                                   "json-comments" "sanitizer"
                                   "text-transform"))))
  (testing "API mismatch"
    (is (thrown-with-msg?
         Exception #"manifest api"
         (shared/validate-manifest manifest "other" "sanitizer"
                                   "text-transform")))))

(deftest shared-parser-loader-test
  (let [parser-manifest {:abi 2
                         :api "parser"
                         :name "toml"
                         :version "0.1.0"
                         :kind "event-source"
                         :event-format "yamlstar-events-edn-v1"}
        events [{:event "stream_start"}
                {:event "document_start"}
                {:event "scalar" :value "ok" :tag "!!str"}
                {:event "document_end"}
                {:event "stream_end"}]
        loader (shared/make-loader
                (fn [api artifact _]
                  (is (= "parser" api))
                  (is (= "parser-toml" artifact))
                  (pr-str parser-manifest))
                (fn [_ artifact input options]
                  (is (= "parser-toml" artifact))
                  (is (= "answer = 42\n" input))
                  (is (= "{}" options))
                  [0 (pr-str events)]))
        plugin (loader "parser" "toml" nil false)]
    (is (= parser-manifest (:manifest plugin)))
    (is (= events ((:parse plugin) "answer = 42\n" {})))
    (is (nil? (loader "parser" "unknown" nil false)))))

(deftest shared-error-test
  (let [loader (shared/make-loader
                (fn [_ _ _] (pr-str manifest))
                (fn [_ _ _ _] [1 "bad input"]))
        plugin (loader "json-comments" "sanitizer" nil false)]
    (is (thrown-with-msg? Exception #"bad input"
                          ((:sanitize plugin) "x" {})))))
