(ns yamlstar.core-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [yamlstar.core :as yaml]
            [yamlstar.emitter :as emitter]
            [yamlstar.serializer :as serializer]))

(deftest test-version
  (testing "Version string is returned"
    (is (string? (yaml/version)))
    (is (re-matches #"\d+\.\d+\.\d+.*" (yaml/version)))))

;; Basic Scalar Tests
(deftest test-load-simple-scalar
  (testing "Load a simple scalar value"
    (is (= "hello" (yaml/load "hello")))))

(deftest test-load-null-values
  (testing "Load various null representations"
    (is (nil? (yaml/load "null")))
    (is (nil? (yaml/load "Null")))
    (is (nil? (yaml/load "NULL")))
    (is (nil? (yaml/load "~")))))

(deftest test-load-implicit-empty-values
  (testing "Load implicit empty mapping values as null"
    (is (= {"a" nil "b" nil "c" nil}
           (yaml/load "{a,b,c}")))
    (is (= {"a" nil}
           (yaml/load "a:")))))

(deftest test-load-boolean-values
  (testing "Load boolean values"
    (is (= true (yaml/load "true")))
    (is (= true (yaml/load "True")))
    (is (= true (yaml/load "TRUE")))
    (is (= false (yaml/load "false")))
    (is (= false (yaml/load "False")))
    (is (= false (yaml/load "FALSE")))))

(deftest test-load-integer-values
  (testing "Load integer values"
    (is (= 42 (yaml/load "42")))
    (is (= -42 (yaml/load "-42")))
    (is (= 0 (yaml/load "0")))))

(deftest test-load-safe-integer-range
  (testing "Load integers within JSON-safe exact range"
    (is (= 9007199254740991 (yaml/load "9007199254740991")))
    (is (= 9007199254740991 (yaml/load "+9007199254740991")))
    (is (= -9007199254740991 (yaml/load "-9007199254740991"))))
  (testing "Reject integers outside JSON-safe exact range"
    (doseq [value ["9007199254740992"
                   "+9007199254740992"
                   "-9007199254740992"
                   "9999999999999999999"
                   "!!int 9007199254740992"]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"YAML integer out of supported range"
           (yaml/load value)))))
  (testing "Load quoted large integers as strings"
    (is (= "9999999999999999999"
           (yaml/load "\"9999999999999999999\"")))))

(deftest test-load-float-values
  (testing "Load float values"
    (is (= 3.14 (yaml/load "3.14")))
    (is (= -3.14 (yaml/load "-3.14")))
    (is (= 1.23e10 (yaml/load "1.23e10")))
    (is (= Double/POSITIVE_INFINITY (yaml/load ".inf")))
    (is (= Double/NEGATIVE_INFINITY (yaml/load "-.inf")))
    (is (Double/isNaN (yaml/load ".nan")))))

;; Mapping Tests
(deftest test-load-simple-mapping
  (testing "Load a simple mapping"
    (is (= {"key" "value"}
           (yaml/load "key: value")))))

(deftest test-load-nested-mapping
  (testing "Load nested mappings"
    (is (= {"outer" {"inner" "value"}}
           (yaml/load "outer:\n  inner: value")))))

(deftest test-load-mapping-with-multiple-keys
  (testing "Load mapping with multiple keys"
    (is (= {"key1" "value1"
            "key2" "value2"
            "key3" "value3"}
           (yaml/load "key1: value1\nkey2: value2\nkey3: value3")))))

(deftest test-load-preserves-mapping-order
  (testing "Load mappings in source order beyond Clojure hash-map threshold"
    (is (= "{\"a\":null,\"b\":null,\"c\":null,\"d\":null,\"e\":null,\"f\":null,\"g\":null,\"h\":null,\"i\":null}"
           (json/write-str (yaml/load "{a,b,c,d,e,f,g,h,i}")))))
  (testing "Load nested mappings in source order"
    (is (= "{\"outer\":{\"a\":null,\"b\":null,\"c\":null,\"d\":null,\"e\":null,\"f\":null,\"g\":null,\"h\":null,\"i\":null},\"z\":0}"
           (json/write-str (yaml/load "{outer: {a,b,c,d,e,f,g,h,i}, z: 0}"))))))

;; Sequence Tests
(deftest test-load-simple-sequence
  (testing "Load a simple sequence"
    (is (= ["item1" "item2" "item3"]
           (yaml/load "- item1\n- item2\n- item3")))))

(deftest test-load-nested-sequence
  (testing "Load nested sequences"
    (is (= [["a" "b"] ["c" "d"]]
           (yaml/load "- [a, b]\n- [c, d]")))))

(deftest test-load-flow-sequence
  (testing "Load flow-style sequence"
    (is (= ["a" "b" "c"]
           (yaml/load "[a, b, c]")))))

;; Mixed Structure Tests
(deftest test-load-sequence-of-mappings
  (testing "Load sequence of mappings"
    (is (= [{"name" "Alice" "age" 30}
            {"name" "Bob" "age" 25}]
           (yaml/load "- name: Alice\n  age: 30\n- name: Bob\n  age: 25")))))

(deftest test-load-mapping-of-sequences
  (testing "Load mapping with sequence values"
    (is (= {"fruits" ["apple" "banana" "orange"]
            "colors" ["red" "green" "blue"]}
           (yaml/load "fruits:\n  - apple\n  - banana\n  - orange\ncolors:\n  - red\n  - green\n  - blue")))))

;; Anchor and Alias Tests
(deftest test-load-anchor-and-alias
  (testing "Load document with anchor and alias"
    (let [result (yaml/load "anchor: &ref value\nalias: *ref")]
      (is (= {"anchor" "value" "alias" "value"} result))
      ;; Both should point to the same value
      (is (= (get result "anchor") (get result "alias"))))))

(deftest test-load-complex-anchor-alias
  (testing "Load complex anchor/alias with mapping"
    (is (= {"person" {"name" "Alice" "age" 30}
            "copy" {"name" "Alice" "age" 30}}
           (yaml/load "person: &p\n  name: Alice\n  age: 30\ncopy: *p")))))

;; Explicit Tag Tests
(deftest test-load-explicit-tags
  (testing "Load values with explicit tags"
    (is (= "123" (yaml/load "!!str 123")))
    (is (= 123 (yaml/load "!!int 123")))
    (is (= 3.14 (yaml/load "!!float 3.14")))
    (is (= true (yaml/load "!!bool true")))
    (is (nil? (yaml/load "!!null null")))))

(deftest test-load-non-specific-explicit-tag
  (testing "Explicit ! tag resolves scalars as strings"
    (is (= "12" (yaml/load "! 12")))
    (is (= "true" (yaml/load "! true")))
    (is (= "null" (yaml/load "! null")))
    (is (= {"a" "12"} (yaml/load "a: ! 12"))))
  (testing "Explicit ! tag resolves collections by kind"
    (is (= [1] (yaml/load "! [1]")))
    (is (= {"a" 1} (yaml/load "! {a: 1}")))))

;; Multi-Document Tests
(deftest test-load-null
  (testing "Load nil from null input"
    (is (nil? (yaml/load nil)))))

(deftest test-load-all-multi-document
  (testing "Load multiple documents"
    (is (= ["doc1" "doc2" "doc3"]
           (yaml/load-all "---\ndoc1\n---\ndoc2\n---\ndoc3")))))

(deftest test-load-all-with-explicit-markers
  (testing "Load multiple documents with explicit end markers"
    (is (= [{"a" 1} {"b" 2}]
           (yaml/load-all "---\na: 1\n...\n---\nb: 2\n...")))))

;; Edge Cases
(deftest test-load-empty-string
  (testing "Load empty string returns nil"
    (is (nil? (yaml/load "")))))

(deftest test-load-whitespace-only
  (testing "Load whitespace-only string"
    (is (nil? (yaml/load "   \n  \n  ")))))

(deftest test-load-quoted-strings
  (testing "Load single and double quoted strings"
    (is (= "hello world" (yaml/load "'hello world'")))
    (is (= "hello world" (yaml/load "\"hello world\"")))
    (is (= "" (yaml/load "''")))
    (is (= "" (yaml/load "\"\"")))
    (is (= "null" (yaml/load "\"null\"")))
    (is (= "true" (yaml/load "'true'")))
    (is (= "42" (yaml/load "\"42\"")))))

(deftest test-load-multiline-literal
  (testing "Load literal block scalar"
    (is (= "line1\nline2\nline3\n"
           (yaml/load "|\n  line1\n  line2\n  line3")))))

(deftest test-load-multiline-folded
  (testing "Load folded block scalar"
    (is (string? (yaml/load ">\n  folded\n  text\n  here")))))

;; Dump Tests
(deftest test-dump-simple-scalar
  (testing "Dump a simple scalar value"
    (is (= "hello\n" (yaml/dump "hello")))
    (is (= "42\n" (yaml/dump 42)))
    (is (= "true\n" (yaml/dump true)))
    (is (= "null\n" (yaml/dump nil)))))

(deftest test-dump-safe-integer-range
  (testing "Dump integers within JSON-safe exact range"
    (is (= "9007199254740991\n" (yaml/dump 9007199254740991)))
    (is (= "-9007199254740991\n" (yaml/dump -9007199254740991))))
  (testing "Reject integers outside JSON-safe exact range"
    (doseq [value [9007199254740992
                   -9007199254740992
                   (bigint "9999999999999999999")]]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"YAML integer out of supported range"
           (yaml/dump value)))))
  (testing "Dump string versions of large integers as strings"
    (is (= "'9999999999999999999'\n"
           (yaml/dump "9999999999999999999")))))

(deftest test-dump-simple-mapping
  (testing "Dump a simple mapping"
    (is (= "key: value\n" (yaml/dump {"key" "value"})))))

(deftest test-dump-simple-sequence
  (testing "Dump a simple sequence"
    (is (= "- a\n- b\n- c\n" (yaml/dump ["a" "b" "c"])))))

(deftest test-dump-nested-sequence
  (testing "Dump a nested sequence without flattening child items"
    (is (= "- - 1\n  - 2\n  - 3\n"
           (yaml/dump [[1 2 3]]))))
  (testing "Dump a deeply nested sequence without over-indenting child items"
    (is (= "- 1\n- - 2\n  - - 3\n    - 4\n  - 5\n- 6\n"
           (yaml/dump [1 [2 [3 4] 5] 6])))))

(deftest test-dump-mapping-inside-nested-sequence
  (testing "Dump a compact mapping sequence item with a block sequence value"
    (is (= "- 1\n- - 2\n  - foo:\n    - 3\n    - 4\n  - 5\n- 6\n"
           (yaml/dump [1 [2 {"foo" [3 4]} 5] 6])))))

(deftest test-dump-compact-nested-sequence
  (testing "Dump mapping value sequences in PyYAML-style block layout"
    (is (= "foo:\n- - bar\n"
           (yaml/dump {"foo" [["bar"]]})))))

(deftest test-dump-sequence-of-mappings-aligns-sibling-keys
  (testing "Dump sequence item mappings with sibling keys aligned after the dash"
    (is (= "steps:\n- name: Checkout tag\n  uses: 'actions/checkout@v4'\n  with:\n    ref: main\n"
           (yaml/dump {"steps" [{"name" "Checkout tag"
                                  "uses" "actions/checkout@v4"
                                  "with" {"ref" "main"}}]})))))

(deftest test-emitter-keeps-node-properties-on-header-line
  (testing "Emit anchors tags and literal markers with mapping keys and sequence dashes"
    (is (= "foo: &x !y |\n  ...\nbar:\n- &xx !yy |\n  ...\n"
           (emitter/emit
            [{:event "stream_start"}
             {:event "document_start"}
             {:event "mapping_start"}
             {:event "scalar" :value "foo"}
             {:event "scalar" :anchor "x" :tag "!y" :style "literal" :value "...\n"}
             {:event "scalar" :value "bar"}
             {:event "sequence_start"}
             {:event "scalar" :anchor "xx" :tag "!yy" :style "literal" :value "...\n"}
             {:event "sequence_end"}
             {:event "mapping_end"}
             {:event "document_end"}
             {:event "stream_end"}])))))

(deftest test-serializer-preserves-node-properties
  (testing "Serialize anchors tags styles and aliases"
    (is (= [{:event "stream_start"}
            {:event "document_start"}
            {:event "mapping_start" :anchor "m" :tag "!map"}
            {:event "scalar" :value "foo"}
            {:event "scalar" :value "bar" :anchor "x" :tag "!y" :style "literal"}
            {:event "scalar" :value "alias"}
            {:event "alias" :name "x"}
            {:event "mapping_end"}
            {:event "document_end"}
            {:event "stream_end"}]
           (serializer/serialize
            {:kind :mapping
             :anchor "m"
             :tag "!map"
             :value [[{:kind :scalar :value "foo"}
                      {:kind :scalar :value "bar" :anchor "x" :tag "!y" :style "literal"}]
                     [{:kind :scalar :value "alias"}
                      {:kind :alias :name "x"}]]})))))

(deftest test-dump-empty-collections
  (testing "Dump empty maps and sequences"
    (is (= "{}\n" (yaml/dump {})))
    (is (= "[]\n" (yaml/dump [])))
    (is (= {"empty-map" {} "empty-seq" []}
           (yaml/load (yaml/dump {"empty-map" {} "empty-seq" []}))))))

(deftest test-dump-nested-structure-roundtrip
  (testing "Dump and load nested JSON-compatible data"
    (let [value {"users" [{"name" "Alice" "age" 30}
                          {"name" "Bob" "age" 25}]
                 "active" true
                 "note" nil}]
      (is (= value (yaml/load (yaml/dump value)))))))

(deftest test-dump-quotes-ambiguous-strings
  (testing "Dump strings so they reload as strings"
    (doseq [value ["null" "true" "42" "3.14" "" "hello world"]]
      (is (= value (yaml/load (yaml/dump value)))))))

(deftest test-dump-all
  (testing "Dump multiple documents"
    (let [values ["doc1" {"a" 1} ["b"]]]
      (is (= "---\ndoc1\n---\na: 1\n---\n- b\n"
             (yaml/dump-all values)))
      (is (= values (yaml/load-all (yaml/dump-all values)))))))

(deftest test-dump-rejects-non-string-map-keys
  (testing "Dump rejects non-string map keys"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"string map keys"
         (yaml/dump {1 "one"})))))
