(ns yamlstar.emitter-plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.api :as yaml]
            [yamlstar.emitter :as emitter]
            [yamlstar.plugin :as plugin]
            [yamlstar.plugin.yaml-emitter.reference]
            [yamlstar.plugin.yaml-emitter.snakeyaml]))

(def reference-opts
  {:plugin {:yaml-emitter {:name "reference"}}})

(def snakeyaml-opts
  {:plugin {:yaml-emitter {:name "snakeyaml"}}})

(deftest emitter-selection-test
  (is (= "foo:\n- bar\n"
         (yaml/dump {"foo" ["bar"]} reference-opts)))
  (is (= "foo:\n- bar\n"
         (yaml/dump {"foo" ["bar"]} snakeyaml-opts)))
  (is (= {"foo" ["bar"]}
         (yaml/load (yaml/dump {"foo" ["bar"]} snakeyaml-opts)))))

(deftest emitter-registry-test
  (testing "runtime default emitter can be selected"
    (try
      (emitter/set-default-yaml-emitter! "snakeyaml")
      (is (= "foo:\n- bar\n" (yaml/dump {"foo" ["bar"]})))
      (finally
        (emitter/set-default-yaml-emitter! "reference"))))
  (is (some #{"reference"} (plugin/registered-yaml-emitters)))
  (is (some #{"snakeyaml"} (plugin/registered-yaml-emitters))))

(deftest emitter-config-test
  (let [seen (atom nil)]
    (try
      (plugin/register-yaml-emitter!
       {:name "capture"
        :default-config {:a "A" :b "B"}
        :emit (fn [_events multi? config]
                (reset! seen [multi? config])
                "captured\n")})
      (is (= "captured\n"
             (yaml/dump "value"
                        {:plugin {:yaml-emitter {:name "capture"
                                                :b "b"}}})))
      (is (= [false {:a "A" :b "b"}] @seen))
      (finally
        (plugin/unregister-yaml-emitter! "capture"))))
  (is (thrown-with-msg?
       Exception
       #"Unknown YAML emitter plugin"
       (yaml/dump "value"
                  {:plugin {:yaml-emitter {:name "no-such-emitter"}}})))
  (is (thrown-with-msg?
       Exception
       #"renamed to :yaml-parser"
       (yaml/dump "value"
                  {:plugin {:parser {:name "reference"}}}))))

(deftest go-yaml-emitter-runtime-test
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo
       #"requires the Glojure YAMLStar runtime"
       (yaml/dump {"foo" ["bar"]}
                  {:plugin {:yaml-emitter {:name "go-yaml"}}}))))
