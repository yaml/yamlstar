(ns yamlstar.snakeyaml-test
  "Verify the snakeyaml parser plugin produces the same event stream
  and load results as the reference parser."
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.api :as yaml]
            [yamlstar.parser :as parser]
            [yamlstar.plugin.parser.snakeyaml :as snakeyaml]))

(def corpus
  "YAML documents covering the event vocabulary."
  {"plain scalar" "hello\n"
   "single quoted" "'hello world'\n"
   "double quoted" "\"hello\\nworld\"\n"
   "literal block" "|\n  line1\n  line2\n"
   "literal no final newline" "|\n  line1\n  line2\n  line3"
   "folded block" ">\n  line1\n  line2\n"
   "empty value" "key:\n"
   "empty document" ""
   "block mapping" "a: 1\nb: two\nc: true\nd: null\n"
   "block sequence" "- one\n- 2\n- false\n"
   "nested" "top:\n  mid:\n  - a: 1\n    b: 2\n  - deep:\n      x: y\n"
   "flow mapping" "{a: 1, b: 2}\n"
   "flow sequence" "[1, two, 3.5]\n"
   "flow nested" "{a: [1, 2], b: {c: 3}}\n"
   "core tag" "!!int 42\n"
   "str tag" "!!str 42\n"
   "local tag" "!local value\n"
   "verbatim tag" "!<tag:example.com,2000:app/foo> bar\n"
   "non specific tag" "! plain\n"
   "tag directive" "%TAG !e! tag:example.com,2000:\n---\n!e!foo bar\n"
   "collection tags" "!!map {a: !!seq [1]}\n"
   "anchor alias" "a: &anc [1, 2]\nb: *anc\n"
   "scalar anchor" "x: &s hello\ny: *s\n"
   "explicit doc" "---\nkey: value\n...\n"
   "yaml directive" "%YAML 1.2\n---\nvalue\n"
   "multi doc" "---\ndoc1\n---\ndoc2\n...\n---\ndoc3\n"
   "comments" "# leading\na: 1 # trailing\n# footer\n"})

(deftest event-stream-equivalence-test
  (doseq [[label yaml-str] corpus]
    (testing label
      (is (= (parser/parse yaml-str)
             (snakeyaml/parse yaml-str {}))
          (str "event streams differ for: " label)))))

(deftest load-equivalence-test
  (let [opts {:plugin {:parser {:name "snakeyaml"}}}]
    (doseq [[label yaml-str] corpus
            ;; skip inputs the reference loader itself rejects
            :when (try (yaml/load yaml-str) true
                       (catch Exception _ false))]
      (testing label
        (is (= (yaml/load yaml-str)
               (yaml/load yaml-str opts))
            (str "load results differ for: " label))))
    (doseq [[label yaml-str] corpus
            :when (try (doall (yaml/load-all yaml-str)) true
                       (catch Exception _ false))]
      (testing (str label " (load-all)")
        (is (= (yaml/load-all yaml-str)
               (yaml/load-all yaml-str opts))
            (str "load-all results differ for: " label))))))

(deftest snakeyaml-selection-test
  (testing "snakeyaml resolves when Engine 2.7 is available"
    (is (= {"a" 1}
           (yaml/load "a: 1" {:plugin {:parser {:name "snakeyaml"}}}))))
  (testing "malformed YAML errors surface from load"
    (is (thrown? Exception
                 (yaml/load "a: [1, 2"
                            {:plugin {:parser {:name "snakeyaml"}}})))))
