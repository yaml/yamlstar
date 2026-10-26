(ns yamlstar.cli-options-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.cli-options :as opts]))

(defn env
  [m]
  (fn [k] (get m k)))

(deftest config-options-test
  (testing "inline flow YAML config"
    (is (= {:plugin {:yaml-parser {:name "reference"}}}
           (opts/config-options
            "{plugin: {yaml-parser: {name: reference}}}"))))

  (testing "file config"
    (let [file (java.io.File/createTempFile "yamlstar-options" ".yaml")]
      (try
        (spit file "plugin:\n  yaml-parser:\n    name: reference\n")
        (is (= {:plugin {:yaml-parser {:name "reference"}}}
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
  (testing "Alias-Data disable selector selects the rejecting policy"
    (is (= {:plugin {:alias-data {:disable true}}}
           (opts/plugin-options "alias-data=disable"))))

  (testing "the old API selector is rejected"
    (doseq [selector ["parser" "parser@v0.2.6" "parser=reference"
                      "yaml-emitter,parser=reference@v0.2.6"]]
      (is (thrown-with-msg?
           Exception #"parser was renamed to yaml-parser"
           (opts/plugin-options selector)))))

  (testing "bare API selectors retain versions and runtime defaults"
    (is (= {:plugin {:yaml-parser {}}}
           (opts/plugin-options "yaml-parser")))
    (is (= {:plugin {:yaml-parser {:version "v0.2.6"}}}
           (opts/plugin-options "yaml-parser@v0.2.6"))))

  (testing "generic plugin flag options"
    (is (= {:plugin {:yaml-parser {:name "reference"}}}
           (opts/plugin-options "yaml-parser=reference")))
    (is (= {:plugin {:yaml-parser {:name "json-comments"}}}
           (opts/plugin-options "yaml-parser=json-comments")))
    (is (= {:plugin
            {:yaml-parser {:name "json-comments" :version "v0.1.1"}
             :yaml-emitter {}}}
           (opts/plugin-options
            "yaml-parser=json-comments@v0.1.1,yaml-emitter"))))

  (testing "malformed plugin option"
    (is (thrown-with-msg?
         Exception #"Plugin selector must be"
         (opts/plugin-options "yaml-parser=")))))

(deftest runtime-options-precedence-test
  (testing "CLI plugin beats CLI config and environment config"
    (is (= {:plugin-install true
            :plugin {:yaml-parser {:name "reference"}}}
           (opts/runtime-options
            {:config "{plugin: {yaml-parser: {name: snakeyaml}}}"
             :plugin ["yaml-parser=reference"]}
            (env {"YAMLSTAR_CONFIG"
                  "{plugin: {yaml-parser: {name: go-yaml}}}"})))))

  (testing "CLI config beats environment config"
    (is (= {:plugin-install true
            :plugin {:yaml-parser {:name "snakeyaml"}}}
           (opts/runtime-options
            {:config "{plugin: {yaml-parser: {name: snakeyaml}}}"}
            (env {"YAMLSTAR_CONFIG"
                  "{plugin: {yaml-parser: {name: go-yaml}}}"})))))

  (testing "environment config supplies plugin selection"
    (is (= {:plugin-install true
            :plugin {:yaml-parser {:name "go-yaml"}}}
           (opts/runtime-options
            {}
            (env {"YAMLSTAR_CONFIG"
                  "{plugin: {yaml-parser: {name: go-yaml}}}"})))))

  (testing "generic plugin selection is merged"
    (is (= {:plugin-install true
            :plugin {:yaml-parser {:name "reference"}
                     :yaml-emitter {:name "reference"}}}
           (opts/runtime-options
            {:plugin ["yaml-parser=reference"
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
            {:no-install true}
            (env {})))))

  (testing "CLI flag overrides installation enabled in config"
    (is (= {:plugin-install false}
           (opts/runtime-options
            {:no-install true :config "{plugin_install: true}"}
            (env {}))))))
