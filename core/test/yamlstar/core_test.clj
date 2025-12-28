(ns yamlstar.core-test
  (:require [clojure.test :refer :all]
            [yamlstar.core :as yaml]))

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
    (is (= "hello world" (yaml/load "\"hello world\"")))))

(deftest test-load-multiline-literal
  (testing "Load literal block scalar"
    (is (= "line1\nline2\nline3\n"
           (yaml/load "|\n  line1\n  line2\n  line3")))))

(deftest test-load-multiline-folded
  (testing "Load folded block scalar"
    (is (string? (yaml/load ">\n  folded\n  text\n  here")))))
