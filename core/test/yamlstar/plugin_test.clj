(ns yamlstar.plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.api :as yaml]
            [yamlstar.parser :as parser]
            [yamlstar.plugin :as plugin]))

(deftest registry-test
  (testing "reference parser is registered by default"
    (is (some #{"reference"} (plugin/registered-parsers))))

  (testing "register, resolve, and unregister a parser"
    (let [p {:name "test-parser"
             :parse (fn [_ _] [{:event "stream_start"}
                               {:event "stream_end"}])}]
      (plugin/register-parser! p)
      (is (= p (plugin/resolve-parser "test-parser")))
      (plugin/unregister-parser! "test-parser")
      (is (thrown-with-msg? Exception #"Unknown YAML parser plugin"
                            (plugin/resolve-parser "test-parser")))))

  (testing "re-registering replaces the previous plugin"
    (let [p1 {:name "test-parser" :parse (fn [_ _] :one)}
          p2 {:name "test-parser" :parse (fn [_ _] :two)}]
      (plugin/register-parser! p1)
      (plugin/register-parser! p2)
      (is (= p2 (plugin/resolve-parser "test-parser")))
      (plugin/unregister-parser! "test-parser")))

  (testing "registration validates plugin shape"
    (is (thrown-with-msg? Exception #":name must be a string"
                          (plugin/register-parser!
                            {:name :nope :parse (fn [_ _])})))
    (is (thrown-with-msg? Exception #":parse must be a function"
                          (plugin/register-parser!
                            {:name "nope" :parse "not-a-fn"})))))

(deftest unknown-parser-test
  (testing "unknown parser error names the available parsers"
    (is (thrown-with-msg? Exception #"Available: .*reference"
                          (plugin/resolve-parser "no-such-parser")))))

(deftest parser-opts-test
  (testing "nil and empty opts take the fast path"
    (is (nil? (plugin/parser-opts nil)))
    (is (nil? (plugin/parser-opts {})))
    (is (nil? (plugin/parser-opts {:plugin {}}))))

  (testing "parser selection is extracted with config"
    (is (= ["snakeyaml" {}]
           (plugin/parser-opts {:plugin {:parser {:use "snakeyaml"}}})))
    (is (= ["x" {:setting 1}]
           (plugin/parser-opts {:plugin {:parser {:use "x" :setting 1}}}))))

  (testing "malformed opts are rejected"
    (is (thrown-with-msg? Exception #":plugin must be a map"
                          (plugin/parser-opts {:plugin "nope"})))
    (is (thrown-with-msg? Exception #"Unknown plugin type"
                          (plugin/parser-opts {:plugin {:emitter {}}})))
    (is (thrown-with-msg? Exception #":parser must be a map"
                          (plugin/parser-opts {:plugin {:parser "nope"}})))
    (is (thrown-with-msg? Exception #":use must be a string"
                          (plugin/parser-opts
                            {:plugin {:parser {:use 5}}})))))

(deftest load-with-opts-test
  (testing "reference via opts equals the 1-arity result"
    (let [yaml "a: 1\nb:\n- 2\n- x\n"
          opts {:plugin {:parser {:use "reference"}}}]
      (is (= (yaml/load yaml) (yaml/load yaml opts)))
      (is (= (yaml/load-all "---\na\n---\nb\n")
             (yaml/load-all "---\na\n---\nb\n" opts)))))

  (testing "custom parser plugin is used when selected"
    (plugin/register-parser!
      {:name "fixed"
       :parse (fn [_ config]
                [{:event "stream_start"}
                 {:event "document_start"}
                 {:event "scalar" :value (str (:value config "fixed"))}
                 {:event "document_end"}
                 {:event "stream_end"}])})
    (is (= "fixed" (yaml/load "ignored"
                              {:plugin {:parser {:use "fixed"}}})))
    (is (= "custom" (yaml/load "ignored"
                               {:plugin {:parser {:use "fixed"
                                                  :value "custom"}}})))
    (plugin/unregister-parser! "fixed"))

  (testing "default-config merges under call config"
    (plugin/register-parser!
      {:name "cfg"
       :default-config {:a "A" :b "B"}
       :parse (fn [_ config]
                [{:event "stream_start"}
                 {:event "document_start"}
                 {:event "scalar" :value (str (:a config) (:b config))}
                 {:event "document_end"}
                 {:event "stream_end"}])})
    (is (= "AB" (yaml/load "x" {:plugin {:parser {:use "cfg"}}})))
    (is (= "Ab" (yaml/load "x" {:plugin {:parser {:use "cfg" :b "b"}}})))
    (plugin/unregister-parser! "cfg"))

  (testing "unknown parser in load opts throws"
    (is (thrown-with-msg? Exception #"Unknown YAML parser plugin"
                          (yaml/load "a: 1"
                                     {:plugin {:parser {:use "nope"}}})))))

(deftest parse-arity-test
  (testing "parse 1-arity and 2-arity nil opts agree"
    (let [yaml "key: value\n"]
      (is (= (parser/parse yaml)
             (parser/parse yaml nil)
             (parser/parse yaml {}))))))
