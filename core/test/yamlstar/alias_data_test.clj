(ns yamlstar.alias-data-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.api :as yaml]
            [yamlstar.plugin :as plugin]
            [yamlstar.plugin.alias-data :as alias-data]))

(deftest inline-data-and-merge-test
  (is (= {"color" "blue" "size" 5}
         (yaml/load
          "<<: *defaults\nsize: 5\n"
          {:plugin
           {:alias-data
            {:data
             {"defaults" {"color" "blue" "size" 3}}}}}))))

(deftest dotted-path-test
  (doseq [parser ["reference"]
          [input want]
          [["base: &x {foo: {bar: 42}}\ncopy: *x.foo.bar\n"
            {"base" {"foo" {"bar" 42}} "copy" 42}]
           ["base: &x [{baz: zero}, {baz: one}]\ncopy: *x.1.baz\n"
            {"base" [{"baz" "zero"} {"baz" "one"}] "copy" "one"}]
           ["base: &x {'3': three}\ncopy: *x.'3'\n"
            {"base" {"3" "three"} "copy" "three"}]
           ["base: &x {foo: nested}\nliteral: &x.foo literal\ncopy: *x.foo\n"
            {"base" {"foo" "nested"} "literal" "literal" "copy" "nested"}]
           ["base: &x 42\ncopy: *x\n" {"base" 42 "copy" 42}]
           ["leaf: &leaf {answer: 42}\nbase: &x {nested: *leaf}\ncopy: *x.nested.answer\n"
            {"leaf" {"answer" 42} "base" {"nested" {"answer" 42}} "copy" 42}]
           ["base: &x {nothing: null, off: false}\nnothing: *x.nothing\noff: *x.off\n"
            {"base" {"nothing" nil "off" false} "nothing" nil "off" false}]]]
    (testing (str parser " " input)
      (is (= want (yaml/load input {:plugin {:yaml-parser parser
                                             :alias-data {:path true}}}))))))

(deftest dotted-path-literal-test
  (doseq [selection [nil {:path false}]]
    (is (= {"base" "literal" "copy" "literal"}
           (yaml/load "base: &x.foo literal\ncopy: *x.foo\n"
                      (if selection
                        {:plugin {:alias-data selection}}
                        {}))))))

(deftest dotted-path-snakeyaml-limitation-test
  (is (thrown-with-msg?
       Exception #"while scanning an alias"
       (yaml/load "base: &x {foo: 42}\ncopy: *x.foo\n"
                  {:plugin {:yaml-parser "snakeyaml"
                            :alias-data {:path true}}}))))

(deftest dotted-path-merge-test
  (doseq [parser ["reference"]
          [base expected]
          [["{<<: *first}" "first"]
           ["{<<: [*first, *second]}" "first"]
           ["{<<: [*first, *second], foo: explicit}" "explicit"]]]
    (let [input (str "first: &first {foo: first}\n"
                     "second: &second {foo: second}\n"
                     "base: &x " base "\ncopy: *x.foo\n")]
      (is (= expected
             (get (yaml/load input {:plugin {:yaml-parser parser
                                            :alias-data {:path true}}})
                  "copy"))))))

(deftest dotted-path-stream-test
  (let [input "--- &saved {foo: 42}\n---\ncopy: *saved.foo\n"]
    (is (= [{"foo" 42} {"copy" 42}]
           (yaml/load-all input {:plugin {:alias-data {:path true}}})))
    (is (thrown-with-msg?
         Exception #"unknown anchor"
         (yaml/load-all input
                        {:plugin {:alias-data {:path true :stream false}}}))))
  (is (thrown-with-msg?
       Exception #"missing mapping key"
       (yaml/load "local: &x {}\ncopy: *x.foo\n"
                  {:plugin {:alias-data
                            {:path true :data {"x" {"foo" "external"}}}}}))))

(deftest dotted-path-source-test
  (let [file (java.io.File/createTempFile "alias-data-path" ".yaml")]
    (try
      (spit file "from_file: {nested: [file]}\n")
      (is (= {"inline" "inline" "file" "file"}
             (yaml/load "inline: *x.nested.0\nfile: *from_file.nested.0\n"
                        {:plugin
                         {:alias-data
                          {:path true
                           :data {"x" {"nested" ["inline"]}}
                           :file (.getPath file)}}})))
      (finally
        (.delete file))))
  (with-redefs [alias-data/environment (fn [] {"FOO" "001"})]
    (is (thrown-with-msg?
         Exception #"cannot traverse a scalar"
         (yaml/load "copy: *FOO.bar\n"
                    {:plugin {:alias-data {:path true :env ["FOO"]}}})))))

(deftest dotted-path-errors-test
  (doseq [name ["missing.foo" "x.missing" "x.nothing.foo" "x.off.foo"
                ".foo" "x..foo" "x." "items.-1" "items.+0" "items.foo"
                "items.1" "items.99999999999999999999999999999999999"
                "integer_keys.'3'" "x.42" "items.00" "items.01"
                "items.042" "items.'0'" "x.'abc'" "x.'-1'" "x.'+1'"
                "x.'1.2'" "x.\"42\"" "x.'42" "x.42'" "x.<<"
                "bad/name.foo" "x.foo/bar" "x.foo:bar"]]
    (let [opts {:plugin {:alias-data
                         {:path true
                          :data {"x" {"nothing" nil "off" false}
                                 "items" ["one"]
                                 "integer_keys" {3 "three"}}}}}]
      (try
        (yaml/load (str "copy: *" name "\n") opts)
        (is false (str "Expected path error for " name))
        (catch Exception error
          (is (= name (:alias (ex-data error))))
          (is (contains? (ex-data error) :segment))))))
  (doseq [value [nil "true" 1]]
    (is (thrown-with-msg?
         Exception #"path must be a boolean"
         (yaml/load "value: 1\n"
                    {:plugin {:alias-data {:path value}}})))))

(deftest dotted-path-grammar-test
  (let [long-key (apply str (repeat 40 "9"))
        context (alias-data/new-context
                 {:path true
                  :data {"x" {"42" {"bar" 42} "042" "leading"
                              "0" "zero" long-key "long"
                              "-42" "negative" "+42" "plus"
                              "foo-bar+baz_1" "name"
                              "items" [{"42" {"bar" 42}}]}}}
                 {})]
    (doseq [[name want] [["x.'42'.bar" 42] ["x.'042'" "leading"]
                        ["x.'0'" "zero"] [(str "x.'" long-key "'") "long"]
                        ["x.-42" "negative"] ["x.+42" "plus"]
                        ["x.foo-bar+baz_1" "name"]
                        ["x.items.0.'42'.bar" 42]]]
      (is (= [true want] (alias-data/resolve-alias context name))))
    (doseq [base ["01" "'42'"]]
      (alias-data/define-anchor context base {"bar" 42})
      (is (= [true 42]
             (alias-data/resolve-alias context (str base ".bar")))))
    (doseq [name ["bad/name.foo" "x.foo/bar" "x.é" "x.<<" "x.'abc'"]]
      (try
        (alias-data/resolve-alias context name)
        (is false (str "Expected grammar error for " name))
        (catch Exception error
          (is (= "invalid path segment" (:reason (ex-data error)))))))
    (alias-data/define-anchor context "bad/name" 42)
    (is (= [true 42] (alias-data/resolve-alias context "bad/name")))))

(deftest document-scope-test
  (let [input "--- &saved {x: 1}\n---\ncopy: *saved\n"]
    (testing "standard YAML scope rejects cross-document aliases"
      (is (thrown-with-msg? Exception #"Unknown anchor: saved"
                            (yaml/load-all input))))
    (testing "explicit empty selection enables stream anchors"
      (is (= [{"x" 1} {"copy" {"x" 1}}]
             (yaml/load-all input {:plugin {:alias-data true}}))))))

(deftest explicit-document-scope-test
  (doseq [parser ["reference" "snakeyaml"]]
    (let [opts {:plugin {:yaml-parser parser
                         :alias-data {:stream false}}}]
      (is (= {"local" 1 "copy" 1}
             (yaml/load "local: &a 1\ncopy: *a\n" opts)))
      (is (thrown-with-msg?
           Exception #"Unknown anchor: saved"
           (yaml/load-all "--- &saved {x: 1}\n---\ncopy: *saved\n"
                          opts))))))

(deftest disabled-aliases-test
  (doseq [selection [false "disable" {:disable true}
                     {:disable true :file "/not/a/real/file"
                      :env ["NOT_A_REAL_ALIAS_DATA_VARIABLE"]
                      :data {"bad.name" 1} :name "missing"}]
          parser ["reference" "snakeyaml"]]
    (testing (pr-str selection)
      (let [opts {:plugin {:yaml-parser parser :alias-data selection}}]
        (is (= {"value" 1 "literal" "*a"}
               (yaml/load "value: &a 1\nliteral: '*a'\n" opts)))
        (doseq [input ["value: &a 1\ncopy: *a\n"
                       "copy: *missing\n"
                       "base: &a {x: 1}\ncopy: {<<: *a}\n"]]
          (is (thrown-with-msg? Exception #"aliases are disabled"
                                (yaml/load input opts))))
        (is (thrown-with-msg?
             Exception #"aliases are disabled"
             (yaml/load-all "--- &a 1\n--- *a\n" opts)))))))

(deftest source-precedence-test
  (with-redefs [alias-data/environment
                (fn [] {"ALIAS_ENV" "environment"
                        "shared" "environment"})]
    (let [file (java.io.File/createTempFile "alias-data" ".yaml")]
      (try
        (spit file "file_only: {source: file}\nshared: file\n")
        (is (= {"inline" "inline"
                "file" {"source" "file"}
                "env" "environment"
                "local" "local"
                "winner" "local"}
               (yaml/load
                (str "inline: *shared\n"
                     "file: *file_only\n"
                     "env: *ALIAS_ENV\n"
                     "local: &shared local\n"
                     "winner: *shared\n")
                {:plugin
                 {:alias-data
                  {:data {"shared" "inline"}
                   :file (.getPath file)
                   :env "ALIAS_*"}}})))
        (finally
          (.delete file))))))

(deftest environment-validation-test
  (with-redefs [alias-data/environment
                (fn [] {"GOOD_NAME" "yes" "bad.name" "no"})]
    (is (= {"value" "yes"}
           (yaml/load "value: *GOOD_NAME\n"
                      {:plugin {:alias-data {:env ["GOOD_NAME"]}}})))
    (is (thrown-with-msg?
         Exception
         #"must match"
         (yaml/load "value: true\n"
                    {:plugin {:alias-data {:env true}}})))
    (is (thrown-with-msg?
         Exception
         #"is not set"
         (yaml/load "value: true\n"
                    {:plugin {:alias-data {:env ["MISSING"]}}})))))

(deftest lifecycle-test
  (let [actions (atom [])
        anchors (atom {})
        context
        {:begin-stream #(swap! actions conj :begin-stream)
         :begin-document (fn []
                           (swap! actions conj :begin-document)
                           (reset! anchors {}))
         :define-anchor (fn [name value]
                          (swap! actions conj [:define name])
                          (swap! anchors assoc name value))
         :resolve-alias (fn [name]
                          (swap! actions conj [:resolve name])
                          [(contains? @anchors name) (get @anchors name)])
         :end-document #(swap! actions conj :end-document)
         :end-stream #(swap! actions conj :end-stream)}]
    (plugin/register-alias-data!
     {:api "alias-data"
      :name "trace"
      :new-context (fn [_ _] context)})
    (try
      (is (= {"value" 1 "copy" 1}
             (yaml/load "value: &a 1\ncopy: *a\n"
                        {:plugin {:alias-data {:name "trace"}}})))
      (is (= [:begin-stream
              :begin-document
              [:define "a"]
              [:resolve "a"]
              :end-document
              :end-stream]
             @actions))
      (finally
        (plugin/unregister-alias-data! "trace")))))

(deftest invalid-configuration-test
  (is (thrown-with-msg?
       Exception
       #"must match"
       (yaml/load "value: true\n"
                  {:plugin
                   {:alias-data {:data {"bad.name" "value"}}}})))
  (is (thrown-with-msg?
       Exception
       #"Unknown Alias-Data configuration key"
       (yaml/load "value: true\n"
                  {:plugin {:alias-data {:unknown true}}}))))
