(ns yamlstar.public-core-test
  (:require [clojure.test :refer [deftest is]]
            [yamlstar.core :as yaml]))

(deftest public-core-shim-test
  (is (= {"key" "value"} (yaml/load "key: value")))
  (is (= ["doc1" "doc2"] (yaml/load-all "---\ndoc1\n---\ndoc2")))
  (is (= "key: value\n" (yaml/dump {"key" "value"})))
  (is (string? (yaml/version))))
