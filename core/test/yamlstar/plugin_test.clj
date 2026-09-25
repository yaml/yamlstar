(ns yamlstar.plugin-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.api :as yaml]
            [yamlstar.parser :as parser]
            [yamlstar.plugin :as plugin]))

(deftest registry-test
  (testing "reference parser is registered lazily"
    (plugin/unregister-yaml-parser! "reference")
    (is (not (some #{"reference"} (plugin/registered-yaml-parsers))))
    (is (= "reference" (:name (parser/register-reference-yaml-parser!))))
    (is (some #{"reference"} (plugin/registered-yaml-parsers))))

  (testing "register-yaml-parsers! registers plugins by name"
    (plugin/unregister-yaml-parser! "reference")
    (is (= ["reference"]
           (mapv :name (parser/register-yaml-parsers! "reference"))))
    (is (some #{"reference"} (plugin/registered-yaml-parsers)))
    (is (thrown? Exception
                 (parser/register-yaml-parsers! "no-such-parser"))))

  (testing "register, resolve, and unregister a parser"
    (let [p {:name "test-parser"
             :parse (fn [_ _] [{:event "stream_start"}
                               {:event "stream_end"}])}]
      (plugin/register-yaml-parser! p)
      (is (= p (plugin/resolve-yaml-parser "test-parser")))
      (plugin/unregister-yaml-parser! "test-parser")
      (is (thrown-with-msg? Exception #"Unknown YAML parser plugin"
                            (plugin/resolve-yaml-parser "test-parser")))))

  (testing "re-registering replaces the previous plugin"
    (let [p1 {:name "test-parser" :parse (fn [_ _] :one)}
          p2 {:name "test-parser" :parse (fn [_ _] :two)}]
      (plugin/register-yaml-parser! p1)
      (plugin/register-yaml-parser! p2)
      (is (= p2 (plugin/resolve-yaml-parser "test-parser")))
      (plugin/unregister-yaml-parser! "test-parser")))

  (testing "registration validates plugin shape"
    (is (thrown-with-msg? Exception #":name must be a string"
                          (plugin/register-yaml-parser!
                            {:name :nope :parse (fn [_ _])})))
    (is (thrown-with-msg? Exception #":parse must be a function"
                          (plugin/register-yaml-parser!
                            {:name "nope" :parse "not-a-fn"})))))

(deftest unknown-parser-test
  (testing "unknown parser error names the available parsers"
    (is (thrown-with-msg? Exception #"Available: .*reference"
                          (plugin/resolve-yaml-parser "no-such-parser")))))

(deftest reference-parser-namespace-test
  (testing "reference parser resolves without registry side effects"
    (plugin/unregister-yaml-parser! "reference")
    (is (= "reference"
           (:name (plugin/resolve-yaml-parser "reference"))))
    (parser/register-reference-yaml-parser!)))

(deftest yaml-parser-opts-test
  (testing "nil and empty opts take the fast path"
    (is (nil? (plugin/yaml-parser-opts nil)))
    (is (nil? (plugin/yaml-parser-opts {})))
    (is (nil? (plugin/yaml-parser-opts {:plugin {}}))))

  (testing "parser selection is extracted with config"
    (is (= ["snakeyaml" {}]
           (plugin/yaml-parser-opts
            {:plugin {:yaml-parser {:name "snakeyaml"}}})))
    (is (= ["x" {:setting 1}]
           (plugin/yaml-parser-opts
            {:plugin {:yaml-parser {:name "x" :setting 1}}}))))

  (testing "parser short form accepts a release version"
    (is (= ["reference" {:version "0.2.5"}]
           (plugin/yaml-parser-opts
            {:plugin {:yaml-parser "reference@v0.2.5"}}))))

  (testing "malformed opts are rejected"
    (is (thrown-with-msg? Exception #":plugin must be a map"
                          (plugin/yaml-parser-opts {:plugin "nope"})))
    (is (thrown-with-msg? Exception #":name must be a string"
                          (plugin/yaml-parser-opts
                           {:plugin {:yaml-parser {:name 5}}})))))

(deftest load-with-opts-test
  (testing "reference via opts equals the 1-arity result"
    (let [yaml "a: 1\nb:\n- 2\n- x\n"
          opts {:plugin {:yaml-parser {:name "reference"}}}]
      (is (= (yaml/load yaml) (yaml/load yaml opts)))
      (is (= (yaml/load-all "---\na\n---\nb\n")
             (yaml/load-all "---\na\n---\nb\n" opts)))))

  (testing "custom parser plugin is used when selected"
    (plugin/register-yaml-parser!
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
    (plugin/unregister-yaml-parser! "fixed"))

  (testing "default-config merges under call config"
    (plugin/register-yaml-parser!
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
    (plugin/unregister-yaml-parser! "cfg"))

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

(deftest json-comments-test
  (let [sanitizer {:api "json-comments"
                   :name "test-sanitizer"
                   :version "1.2.3"
                   :sanitize (fn [input _]
                               (.replace input "// value" "42"))}]
    (try
      (plugin/register-json-comments! sanitizer)
      (is (= 42
             (yaml/load "// value"
                        {:plugin
                         {:json-comments
                          "test-sanitizer@v1.2.3"}})))
      (is (thrown-with-msg? Exception #"version mismatch"
                            (yaml/load
                             "// value"
                             {:plugin
                              {:json-comments
                               "test-sanitizer@v1.2.4"}})))
      (finally
        (plugin/unregister-json-comments! "test-sanitizer")))))

(deftest json-comments-loader-test
  (try
    (plugin/set-json-comments-loader!
     (fn [api name _version _install?]
       {:api api :name name :version "1.0.0"
        :sanitize (fn [_ _] "loaded")}))
    (is (= "loaded"
           (yaml/load "ignored"
                      {:plugin {:json-comments "external"}})))
    (finally
      (plugin/unregister-json-comments! "external")
      (plugin/set-json-comments-loader! nil))))

(deftest json-comments-install-option-test
  (let [install-values (atom [])]
    (try
      (plugin/set-json-comments-loader!
       (fn [api name _version install?]
         (swap! install-values conj install?)
         {:api api :name name
          :sanitize (fn [_ _] "loaded")}))
      (is (= "loaded"
             (yaml/load "ignored"
                        {:plugin
                         {:json-comments "external-no-install"}})))
      (plugin/unregister-json-comments! "external-no-install")
      (is (= "loaded"
             (yaml/load "ignored"
                        {:plugin
                         {:json-comments "external-install"}
                         :plugin-install true})))
      (is (= [false true] @install-values))
      (finally
        (plugin/unregister-json-comments! "external-no-install")
        (plugin/unregister-json-comments! "external-install")
        (plugin/set-json-comments-loader! nil)))))
