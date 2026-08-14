(ns yamlstar.public-core-test
  (:require [clojure.test :refer [deftest is]]
            [yamlstar.core :as yaml]))

(deftest public-core-shim-test
  (is (= {"key" "value"} (yaml/load "key: value")))
  (is (= ["doc1" "doc2"] (yaml/load-all "---\ndoc1\n---\ndoc2")))
  (is (= "key: value\n" (yaml/dump {"key" "value"})))
  (is (string? (yaml/version))))

(deftest parser-plugin-test
  (let [opts {:plugin {:parser {:name "reference"}}}]
    (is (= {"key" "value"} (yaml/load "key: value" opts)))
    (is (= (yaml/load "a: [1, {b: two}]\n")
           (yaml/load "a: [1, {b: two}]\n" opts)))
    (is (= ["doc1" "doc2"] (yaml/load-all "---\ndoc1\n---\ndoc2" opts))))
  (is (thrown-with-msg? Exception #"Unknown YAML parser plugin"
                        (yaml/load "a: 1"
                                   {:plugin {:parser {:name "nope"}}}))))
