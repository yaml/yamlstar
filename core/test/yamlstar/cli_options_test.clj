(ns yamlstar.cli-options-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.cli-options :as opts]))

(defn env
  [m]
  (fn [k] (get m k)))

(deftest config-options-test
  (testing "inline flow YAML config"
    (is (= {:plugin {:parser {:name "reference"}}}
           (opts/config-options
             "{plugin: {parser: {name: reference}}}"))))

  (testing "file config"
    (let [file (java.io.File/createTempFile "yamlstar-options" ".yaml")]
      (try
        (spit file "plugin:\n  parser:\n    name: reference\n")
        (is (= {:plugin {:parser {:name "reference"}}}
               (opts/config-options (.getPath file))))
        (finally
          (.delete file)))))

  (testing "blank file config"
    (let [file (java.io.File/createTempFile "yamlstar-options" ".yaml")]
      (try
        (spit file "\n")
        (is (= {} (opts/config-options (.getPath file))))
        (finally
          (.delete file)))))

  (testing "underscore keys normalize to hyphen keywords"
    (is (= {:plugin {:test-plugin {:name "x"}}}
           (opts/config-options
             "{plugin: {test_plugin: {name: x}}}"))))

  (testing "config must load to a mapping"
    (is (thrown-with-msg?
          Exception #"config must be a mapping"
          (opts/config-options "[1, 2]")))))

(deftest plugin-options-test
  (testing "parser flag options"
    (is (= {:plugin {:parser {:name "go-yaml"}}}
           (opts/parser-options "go-yaml"))))

  (testing "generic plugin flag options"
    (is (= {:plugin {:parser {:name "reference"}}}
           (opts/plugin-options "parser=reference"))))

  (testing "malformed plugin option"
    (is (thrown-with-msg?
          Exception #"Plugin option must be API=NAME"
          (opts/plugin-options "parser")))))

(deftest runtime-options-precedence-test
  (testing "cli parser beats cli config, env config, and env parser"
    (is (= {:plugin {:parser {:name "reference"}}}
           (opts/runtime-options
             {:config "{plugin: {parser: {name: snakeyaml}}}"
              :parser "reference"}
             (env {"YAMLSTAR_PARSER" "env-parser"
                   "YAMLSTAR_CONFIG"
                   "{plugin: {parser: {name: go-yaml}}}"})))))

  (testing "cli config beats environment config and parser"
    (is (= {:plugin {:parser {:name "snakeyaml"}}}
           (opts/runtime-options
             {:config "{plugin: {parser: {name: snakeyaml}}}"}
             (env {"YAMLSTAR_PARSER" "env-parser"
                   "YAMLSTAR_CONFIG"
                   "{plugin: {parser: {name: go-yaml}}}"})))))

  (testing "environment config beats YAMLSTAR_PARSER"
    (is (= {:plugin {:parser {:name "go-yaml"}}}
           (opts/runtime-options
             {}
             (env {"YAMLSTAR_PARSER" "env-parser"
                   "YAMLSTAR_CONFIG"
                   "{plugin: {parser: {name: go-yaml}}}"})))))

  (testing "--parser beats --plugin parser=NAME"
    (is (= {:plugin {:parser {:name "reference"}}}
           (opts/runtime-options
             {:plugin ["parser=go-yaml"]
              :parser "reference"}
             (env {})))))))
