(ns yamlstar.public-core-test
  (:require [clojure.test :refer [deftest is]]
            [yamlstar.core :as yaml]))

(deftest public-core-shim-test
  (is (= {"key" "value"} (yaml/load "key: value")))
  (is (= ["doc1" "doc2"] (yaml/load-all "---\ndoc1\n---\ndoc2")))
  (is (= "key: value\n" (yaml/dump {"key" "value"})))
  (is (string? (yaml/version))))

(deftest yaml-parser-plugin-test
  (let [opts {:plugin {:yaml-parser {:name "reference"}}}]
    (is (= {"key" "value"} (yaml/load "key: value" opts)))
    (is (= (yaml/load "a: [1, {b: two}]\n")
           (yaml/load "a: [1, {b: two}]\n" opts)))
    (is (= ["doc1" "doc2"] (yaml/load-all "---\ndoc1\n---\ndoc2" opts))))
  (is (thrown-with-msg? Exception #"Unknown YAML parser plugin"
                        (yaml/load "a: 1"
                                   {:plugin {:yaml-parser {:name "nope"}}}))))

(deftest tab-indent-dump-test
  (is (thrown-with-msg?
       Exception
       #"tab-indent dumping requires the native go-yaml emitter"
       (yaml/dump {"root" {"value" true}}
                  {:plugin {:tab-indent {}}})))
  (let [opts {:plugin {:tab-indent {:dump "spaces"}}}]
    (is (= "root:\n  value: true\n"
           (yaml/dump {"root" {"value" true}} opts)))))
