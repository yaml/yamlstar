(ns yamlstar.shared-plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.plugin.shared :as shared]))

(def manifest
  {:abi 2
   :api "yaml-parser"
   :name "toml"
   :version "0.1.9"
   :kind "event-source"})

(deftest shared-loader-test
  (let [loader (shared/make-loader
                (fn [_ _ _] (pr-str manifest))
                (fn [_ _ input _]
                  [0 (pr-str [{:event "scalar" :value input}])]))
        plugin (loader "yaml-parser" "toml" nil false)]
    (is (= manifest (:manifest plugin)))
    (is (= "0.1.9" (:version plugin)))
    (is (= [{:event "scalar" :value "answer = 42"}]
           ((:parse plugin) "answer = 42" {})))))

(deftest manifest-validation-test
  (testing "old parser manifests are rejected by the loader"
    (doseq [name ["toml" "rapidyaml"]]
      (let [loader (shared/make-loader
                    (fn [_ _ _]
                      (pr-str (assoc manifest :api "parser" :name name)))
                    (fn [& _] (throw (Exception. "must not parse"))))]
        (is (thrown-with-msg?
             Exception #"manifest api must be.*yaml-parser"
             (loader "yaml-parser" name nil false))))))

  (testing "ABI mismatch"
    (is (thrown-with-msg?
         Exception #"manifest abi"
         (shared/validate-manifest (assoc manifest :abi 1)
                                   "yaml-parser" "toml"
                                   "event-source"))))
  (testing "API mismatch"
    (is (thrown-with-msg?
         Exception #"manifest api"
         (shared/validate-manifest manifest "other" "toml"
                                   "event-source")))))

(deftest shared-parser-loader-test
  (let [parser-manifest {:abi 2
                         :api "yaml-parser"
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
                  (is (= "yaml-parser" api))
                  (is (= "parser-toml" artifact))
                  (pr-str parser-manifest))
                (fn [_ artifact input options]
                  (is (= "parser-toml" artifact))
                  (is (= "answer = 42\n" input))
                  (is (= "{}" options))
                  [0 (pr-str events)]))
        plugin (loader "yaml-parser" "toml" nil false)]
    (is (= parser-manifest (:manifest plugin)))
    (is (= events ((:parse plugin) "answer = 42\n" {})))
    (is (nil? (loader "yaml-parser" "unknown" nil false)))))

(deftest shared-rapidyaml-loader-test
  (let [parser-manifest {:abi 2
                         :api "yaml-parser"
                         :name "rapidyaml"
                         :version "0.1.2"
                         :kind "event-source"
                         :event-format "yamlstar-events-edn-v1"}
        events [{:event "stream_start"}
                {:event "document_start"}
                {:event "scalar" :value "ok" :tag "!!str"}
                {:event "document_end"}
                {:event "stream_end"}]
        loader (shared/make-loader
                (fn [api artifact _]
                  (is (= "yaml-parser" api))
                  (is (= "parser-rapidyaml" artifact))
                  (pr-str parser-manifest))
                (fn [_ artifact input options]
                  (is (= "parser-rapidyaml" artifact))
                  (is (= "answer: 42\n" input))
                  (is (= "{}" options))
                  [0 (pr-str events)]))
        plugin (loader "yaml-parser" "rapidyaml" nil false)]
    (is (= parser-manifest (:manifest plugin)))
    (is (= events ((:parse plugin) "answer: 42\n" {})))
    (is (nil? (loader "yaml-parser" "unknown" nil false)))))

(deftest shared-error-test
  (let [loader (shared/make-loader
                (fn [_ _ _] (pr-str manifest))
                (fn [_ _ _ _] [1 "bad input"]))
        plugin (loader "yaml-parser" "toml" nil false)]
    (is (thrown-with-msg? Exception #"bad input"
                          ((:parse plugin) "x" {})))))
