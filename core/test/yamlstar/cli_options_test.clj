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

  (testing "Alias-Data keys are preserved"
    (is (= {:plugin {:alias-data {:data {"FOO_BAR" 1}}}}
           (opts/config-options
            "{plugin: {alias_data: {data: {FOO_BAR: 1}}}}"))))

  (testing "config must load to a mapping"
    (is (thrown-with-msg?
         Exception #"config must be a mapping"
         (opts/config-options "[1, 2]")))))

(deftest plugin-options-test
  (testing "generic plugin flag options"
    (is (= {:plugin {:parser {:name "reference"}}}
           (opts/plugin-options "parser=reference")))
    (is (= {:plugin {:json-comments {}}}
           (opts/plugin-options "json-comments")))
    (is (= {:plugin
            {:parser {:name "reference" :version "v0.2.5"}
             :json-comments {}}}
           (opts/plugin-options
            "parser=reference@v0.2.5,json-comments"))))

  (testing "malformed plugin option"
    (is (thrown-with-msg?
         Exception #"Plugin selector must be"
         (opts/plugin-options "parser=")))))

(deftest runtime-options-precedence-test
  (testing "CLI plugin beats CLI config and environment config"
    (is (= {:plugin-install true
            :plugin {:parser {:name "reference"}}}
           (opts/runtime-options
            {:config "{plugin: {parser: {name: snakeyaml}}}"
             :plugin ["parser=reference"]}
            (env {"YAMLSTAR_CONFIG"
                  "{plugin: {parser: {name: go-yaml}}}"})))))

  (testing "CLI config beats environment config"
    (is (= {:plugin-install true
            :plugin {:parser {:name "snakeyaml"}}}
           (opts/runtime-options
            {:config "{plugin: {parser: {name: snakeyaml}}}"}
            (env {"YAMLSTAR_CONFIG"
                  "{plugin: {parser: {name: go-yaml}}}"})))))

  (testing "environment config supplies plugin selection"
    (is (= {:plugin-install true
            :plugin {:parser {:name "go-yaml"}}}
           (opts/runtime-options
            {}
            (env {"YAMLSTAR_CONFIG"
                  "{plugin: {parser: {name: go-yaml}}}"})))))

  (testing "generic plugin selection is merged"
    (is (= {:plugin-install true
            :plugin {:parser {:name "reference"}
                     :yaml-emitter {:name "reference"}}}
           (opts/runtime-options
            {:plugin ["parser=reference"
                      "yaml-emitter=reference"]}
            (env {})))))

  (testing "config can disable automatic plugin installation"
    (is (= {:plugin-install false}
           (opts/runtime-options
            {:config "{plugin_install: false}"}
            (env {})))))

  (testing "CLI flag disables automatic plugin installation"
    (is (= {:plugin-install false}
           (opts/runtime-options
            {:no-plugin-install true}
            (env {}))))))
