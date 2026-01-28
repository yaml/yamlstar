(ns com.yaml.yamlstar-test
  (:require [clojure.test :refer [deftest is testing]])
  (:import [com.yaml YAMLStar]
           [java.util HashMap ArrayList]))

(deftest test-load-scalar-string
  (testing "Load a simple string scalar"
    (let [result (YAMLStar/load "hello")]
      (is (= "hello" result)))))

(deftest test-load-scalar-integer
  (testing "Load an integer scalar"
    (let [result (YAMLStar/load "42")]
      (is (= 42 result)))))

(deftest test-load-scalar-float
  (testing "Load a float scalar"
    (let [result (YAMLStar/load "3.14")]
      (is (= 3.14 result)))))

(deftest test-load-scalar-boolean
  (testing "Load boolean scalars"
    (is (= true (YAMLStar/load "true")))
    (is (= false (YAMLStar/load "false")))))

(deftest test-load-scalar-null
  (testing "Load null scalar"
    (let [result (YAMLStar/load "null")]
      (is (nil? result)))))

(deftest test-load-simple-mapping
  (testing "Load a simple mapping"
    (let [result (YAMLStar/load "key: value")]
      (is (instance? HashMap result))
      (is (= "value" (.get result "key"))))))

(deftest test-load-nested-mapping
  (testing "Load a nested mapping"
    (let [result (YAMLStar/load "outer:\n  inner: value")]
      (is (instance? HashMap result))
      (let [outer (.get result "outer")]
        (is (instance? HashMap outer))
        (is (= "value" (.get outer "inner")))))))

(deftest test-load-simple-sequence
  (testing "Load a simple sequence"
    (let [result (YAMLStar/load "- a\n- b\n- c")]
      (is (instance? ArrayList result))
      (is (= 3 (.size result)))
      (is (= "a" (.get result 0)))
      (is (= "b" (.get result 1)))
      (is (= "c" (.get result 2))))))

(deftest test-load-sequence-of-mappings
  (testing "Load a sequence of mappings"
    (let [result (YAMLStar/load "- name: Alice\n  age: 30\n- name: Bob\n  age: 25")]
      (is (instance? ArrayList result))
      (is (= 2 (.size result)))
      (let [first-item (.get result 0)]
        (is (instance? HashMap first-item))
        (is (= "Alice" (.get first-item "name")))
        (is (= 30 (.get first-item "age")))))))

(deftest test-load-mapping-with-sequence-value
  (testing "Load a mapping with sequence values"
    (let [result (YAMLStar/load "items:\n  - apple\n  - banana\n  - cherry")]
      (is (instance? HashMap result))
      (let [items (.get result "items")]
        (is (instance? ArrayList items))
        (is (= 3 (.size items)))
        (is (= "apple" (.get items 0)))))))

(deftest test-load-with-anchors-and-aliases
  (testing "Load YAML with anchors and aliases"
    (let [result (YAMLStar/load "base: &anchor\n  x: 1\n  y: 2\nderived: *anchor")]
      (is (instance? HashMap result))
      (let [base (.get result "base")
            derived (.get result "derived")]
        (is (instance? HashMap base))
        (is (instance? HashMap derived))
        (is (= 1 (.get base "x")))
        (is (= 2 (.get base "y")))
        (is (= 1 (.get derived "x")))
        (is (= 2 (.get derived "y")))))))

(deftest test-load-all-single-document
  (testing "loadAll with single document"
    (let [result (YAMLStar/loadAll "key: value")]
      (is (instance? ArrayList result))
      (is (= 1 (.size result)))
      (let [doc (.get result 0)]
        (is (instance? HashMap doc))
        (is (= "value" (.get doc "key")))))))

(deftest test-load-all-multiple-documents
  (testing "loadAll with multiple documents"
    (let [result (YAMLStar/loadAll "---\ndoc1\n---\ndoc2\n---\ndoc3")]
      (is (instance? ArrayList result))
      (is (= 3 (.size result)))
      (is (= "doc1" (.get result 0)))
      (is (= "doc2" (.get result 1)))
      (is (= "doc3" (.get result 2))))))

(deftest test-load-all-mixed-types
  (testing "loadAll with mixed document types"
    (let [result (YAMLStar/loadAll "---\nstring\n---\n42\n---\nkey: value")]
      (is (instance? ArrayList result))
      (is (= 3 (.size result)))
      (is (= "string" (.get result 0)))
      (is (= 42 (.get result 1)))
      (is (instance? HashMap (.get result 2))))))

(deftest test-version
  (testing "Get version string"
    (let [version (YAMLStar/version)]
      (is (string? version))
      (is (= "0.1.3" version)))))

(deftest test-special-float-values
  (testing "Load special float values"
    (is (= Double/POSITIVE_INFINITY (YAMLStar/load ".inf")))
    (is (= Double/NEGATIVE_INFINITY (YAMLStar/load "-.inf")))
    (is (Double/isNaN (YAMLStar/load ".nan")))))

(deftest test-explicit-tags
  (testing "Load values with explicit tags"
    (is (= "123" (YAMLStar/load "!!str 123")))
    (is (= 123 (YAMLStar/load "!!int 123")))))
