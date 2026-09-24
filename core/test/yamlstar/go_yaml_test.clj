(ns yamlstar.go-yaml-test
  (:require [clojure.test :refer [deftest is testing]]
            [yaml-parser.core :as ref-parser]
            [yamlstar.api :as yaml]
            [yamlstar.plugin :as plugin]
            [yamlstar.parser :as parser]
            [yamlstar.plugin.yaml-parser.go-yaml]))

(deftest go-yaml-selection-test
  (testing "go-yaml fails outside the Glojure runtime"
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"requires the Glojure YAMLStar runtime"
          (yaml/load "a: 1" {:plugin {:yaml-parser {:name "go-yaml"}}})))))

(deftest default-parser-test
  (testing "runtime default parser can be selected without opts"
    (let [calls (atom [])]
      (try
        (plugin/register-yaml-parser!
          {:name "go-yaml"
           :parse (fn [yaml-str config]
                    (swap! calls conj [yaml-str config])
                    (ref-parser/parse yaml-str))
           :default-config {}})
        (parser/set-default-yaml-parser! "go-yaml")
        (is (= {"a" 1} (yaml/load "a: 1\n")))
        (is (= [["a: 1\n" {}]] @calls))
        (finally
          (plugin/register-yaml-parser!
           yamlstar.plugin.yaml-parser.go-yaml/plugin)
          (parser/set-default-yaml-parser! "reference"))))))
