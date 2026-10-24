(ns yamlstar.plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.api :as yaml]
            [yamlstar.parser :as parser]
            [yamlstar.plugin :as plugin]))

(deftest registry-test
  (testing "reference parser is registered lazily"
    (plugin/unregister-parser! "reference")
    (is (not (some #{"reference"} (plugin/registered-parsers))))
    (is (= "reference" (:name (parser/register-reference-parser!))))
    (is (some #{"reference"} (plugin/registered-parsers))))

  (testing "register-parsers! registers plugins by name"
    (plugin/unregister-parser! "reference")
    (is (= ["reference"]
           (mapv :name (parser/register-parsers! "reference"))))
    (is (some #{"reference"} (plugin/registered-parsers)))
    (is (thrown? Exception
                 (parser/register-parsers! "no-such-parser"))))

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

(deftest reference-parser-namespace-test
  (testing "reference parser resolves without registry side effects"
    (plugin/unregister-parser! "reference")
    (is (= "reference"
           (:name (plugin/resolve-parser "reference"))))
    (parser/register-reference-parser!)))

(deftest parser-opts-test
  (testing "nil and empty opts take the fast path"
    (is (nil? (plugin/parser-opts nil)))
    (is (nil? (plugin/parser-opts {})))
    (is (nil? (plugin/parser-opts {:plugin {}}))))

  (testing "boolean selections keep the runtime default"
    (is (= [nil {}] (plugin/parser-opts {:plugin {:yaml-parser true}})))
    (is (nil? (plugin/parser-opts {:plugin {:yaml-parser false}})))
    (is (nil? (plugin/parser-opts
               {:plugin {:yaml-parser {:disable true}}}))))

  (testing "the old API name is rejected"
    (doseq [value [true false "reference" {:name "reference"}]]
      (is (thrown-with-msg?
           Exception #"parser was renamed to yaml-parser"
           (plugin/parser-opts {:plugin {:parser value}})))))

  (testing "parser selection is extracted with config"
    (is (= ["snakeyaml" {}]
           (plugin/parser-opts
            {:plugin {:yaml-parser {:name "snakeyaml"}}})))
    (is (= ["x" {:setting 1}]
           (plugin/parser-opts
            {:plugin {:yaml-parser {:name "x" :setting 1}}}))))

  (testing "parser short form accepts a release version"
    (is (= ["reference" {:version "0.2.6"}]
           (plugin/parser-opts
            {:plugin {:yaml-parser "reference@v0.2.6"}}))))

  (testing "malformed opts are rejected"
    (is (thrown-with-msg? Exception #":plugin must be a map"
                          (plugin/parser-opts {:plugin "nope"})))
    (is (thrown-with-msg? Exception #":name must be a string"
                          (plugin/parser-opts
                           {:plugin {:yaml-parser {:name 5}}})))))

(deftest load-with-opts-test
  (testing "unnamed selections preserve the runtime default parser"
    (doseq [selection [true false {} {:disable true}]]
      (is (= (yaml/load "a: 1")
             (yaml/load "a: 1" {:plugin {:yaml-parser selection}})))))

  (testing "reference via opts equals the 1-arity result"
    (let [yaml "a: 1\nb:\n- 2\n- x\n"
          opts {:plugin {:yaml-parser {:name "reference"}}}]
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
                              {:plugin {:yaml-parser {:name "fixed"}}})))
    (is (= "custom" (yaml/load "ignored"
                               {:plugin {:yaml-parser {:name "fixed"
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
    (is (= "AB"
           (yaml/load "x" {:plugin {:yaml-parser {:name "cfg"}}})))
    (is (= "Ab"
           (yaml/load "x"
                      {:plugin
                       {:yaml-parser {:name "cfg" :b "b"}}})))
    (plugin/unregister-parser! "cfg"))

  (testing "unknown parser in load opts throws"
    (is (thrown-with-msg? Exception #"Unknown YAML parser plugin"
                          (yaml/load "a: 1"
                                     {:plugin
                                      {:yaml-parser {:name "nope"}}})))))

(deftest parse-arity-test
  (testing "parse 1-arity and 2-arity nil opts agree"
    (let [yaml "key: value\n"]
      (is (= (parser/parse yaml)
             (parser/parse yaml nil)
             (parser/parse yaml {}))))))

(deftest json-comments-parser-test
  (let [opts {:plugin {:yaml-parser "json-comments@v0.1.0"}}]
    (is (= {"a" true} (yaml/load "a: true// comment" opts)))
    (is (= {"a" "foo  bar"}
           (yaml/load "a: foo /*comment*/ bar" opts)))
    (is (= "http://example.com" (yaml/load "http://example.com" opts)))
    (is (thrown-with-msg? Exception #"version mismatch"
          (yaml/load "true" {:plugin {:yaml-parser "json-comments@v0.0.1"}})))
    (is (thrown-with-msg? Exception #"Unknown YAMLStar plugin API"
          (yaml/load "true" {:plugin {:json-comments true}})))))
