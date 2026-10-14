(ns yamlstar.cli-test
  (:require [clojure.test :refer :all]
            [clojure.tools.cli :as tools-cli]
            [yamlstar.api :as yaml]
            [yamlstar.cli :as cli]
            [yamlstar.cli-default :as cli-default]
            [yamlstar.plugin :as plugin]))

(def sample "a: &x [1, \"two\"]\nb: *x\n")

(deftest bundled-yaml-plugins
  (is (= #{"reference" cli-default/default-parser}
         (set (plugin/registered-parsers))))
  (is (= #{"reference" cli-default/default-yaml-emitter}
         (set (plugin/registered-yaml-emitters)))))

(deftest bundled-json-comments-plugin
  (is (= {"a" true}
         (yaml/load "a: true // comment\n"
                    {:plugin {:parser "reference@v0.2.5"
                              :json-comments true}}))))

(deftest combined-plugin-selector
  (let [output
        (with-out-str
          (is (zero?
               (cli/main-status
                "--eval" "a: true // comment\n"
                (str "--plugin=parser=reference@0.2.5,"
                     "json-comments")))))]
    (is (= "{\"a\":true}\n" output))))

(deftest version-output
  (is (= (str "yaml v"
              (clojure.string/replace cli/version #"-SNAPSHOT$" ""))
         (clojure.string/trim-newline
          (with-out-str (cli/print-version))))))

(deftest compact-command-line-options
  (let [{:keys [options arguments errors]}
        (tools-cli/parse-opts
         ["-YZofile"
          "--from=yaml"
          "--file=input.yaml"
          "--eval=x: 1"
          "--config={plugin: {alias-data: true}}"
          "--plugin=alias-data"
          "--plugin=parser=go-yaml"
          "--no-install"
          "--debug-stage=parse"]
         cli/cli-options)]
    (is (nil? errors))
    (is (empty? arguments))
    (is (= true (:YAML options)))
    (is (= true (:last options)))
    (is (= "file" (:output options)))
    (is (= "yaml" (:from options)))
    (is (= "input.yaml" (:file options)))
    (is (= "x: 1" (:eval options)))
    (is (= "{plugin: {alias-data: true}}" (:config options)))
    (is (= ["alias-data" "parser=go-yaml"] (:plugin options)))
    (is (= true (:no-install options)))
    (is (= "parse" (:debug-stage options)))))

(deftest yaml-event-node-yaml-chain
  (let [events (cli/convert-input sample {:event true} {})
        nodes (cli/convert-input events {:NODE true} {})
        output (cli/convert-input nodes {:YAML true} {})]
    (is (= "Document" (get (yaml/load nodes) "node")))
    (is (= "Mapping" (get-in (yaml/load nodes) ["content" 0 "node"])))
    (is (= sample output))))

(deftest retired-node-key-is-rejected
  (is (thrown? clojure.lang.ExceptionInfo
               (cli/convert-input "kind: Scalar\nvalue: old\n"
                                  {:from "node" :YAML true} {}))))

(deftest forced-yaml-disambiguates-contract-shaped-data
  (let [source "- {event: STREAM-START}\n- {event: STREAM-END}\n"
        output (cli/convert-input source {:from "yaml" :node true} {})]
    (is (= [{"event" "STREAM-START"} {"event" "STREAM-END"}]
           (yaml/load (cli/convert-input output {:YAML true} {}))))))

(deftest token-stage-is-an-explicit-follow-up
  (let [tokens "- token: STREAM-START\n- token: STREAM-END\n"]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"parser token support is the explicit follow-up"
         (cli/convert-input tokens {:YAML true} {})))))

(deftest backward-stage-conversion-is-rejected
  (let [nodes (cli/convert-input sample {:node true} {})]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"cannot convert node input backward to event output"
         (cli/convert-input nodes {:event true} {})))))

(deftest single-document-output
  (doseq [[source expected]
          [["a: 1\n" "{\"a\":1}"]
           ["- one\n- two\n" "[\"one\",\"two\"]"]
           ["false\n" "false"]
           ["null\n" "null"]
           ["42\n" "42"]
           ["[]\n" "[]"]]]
    (doseq [options [{} {:json true} {:first true} {:last true}]]
      (is (= expected (cli/convert-input source options {})))))
  (is (= "{\n  \"a\": 1\n}"
         (cli/convert-input "a: 1\n" {:JSON true} {})))
  (doseq [options [{:yaml true} {:YAML true}]]
    (let [output (cli/convert-input "a: 1\n" options {})]
      (is (= {"a" 1} (yaml/load output)))
      (is (not (clojure.string/starts-with? output "---\n")))))
  (is (= "[]" (cli/convert-input "" {} {}))))

(deftest document-count-applies-to-output-formats
  (doseq [options [{:event true} {:EVENT true}]]
    (doseq [[source expected] [["a: 1\n" 1]
                               ["a: 1\n---\nb: 2\n" 2]]]
      (let [events (yaml/load (cli/convert-input source options {}))]
        (is (= expected
               (count (filter #(= "DOCUMENT-START" (get % "event"))
                              events)))))))
  (doseq [options [{:node true} {:NODE true}]]
    (is (map? (yaml/load (cli/convert-input "a: 1\n" options {}))))
    (let [nodes (yaml/load
                 (cli/convert-input "a: 1\n---\nb: 2\n" options {}))]
      (is (vector? nodes))
      (is (= 2 (count nodes)))))
  (doseq [options [{:yaml true} {:YAML true}]]
    (let [output (cli/convert-input "a: 1\n---\nb: 2\n" options {})]
      (is (= [{"a" 1} {"b" 2}] (yaml/load-all output)))))
  (doseq [debug [cli/do-debug-compose cli/do-debug-resolve
                 cli/do-debug-construct]]
    (doseq [[source expected] [["a: 1\n" "{"]
                               ["a: 1\n---\nb: 2\n" "["]]]
      (let [output (with-out-str (debug source {} :all))
            value (second (clojure.string/split output #"\n\n" 2))]
        (is (clojure.string/starts-with? value expected))))))

(deftest document-selection-applies-to-output-stages
  (let [source "--- first\n--- second\n"]
    (is (= "[\"first\",\"second\"]"
           (cli/convert-input source {} {})))
    (is (= "\"first\""
           (cli/convert-input source {:first true} {})))
    (is (= "\"second\""
           (cli/convert-input source {:last true} {})))
    (doseq [mode [:event :node :yaml :YAML]]
      (let [first-output (cli/convert-input source {mode true :first true} {})
            last-output (cli/convert-input source {mode true :last true} {})]
        (is (clojure.string/includes? first-output "first") (name mode))
        (is (not (clojure.string/includes? first-output "second"))
            (name mode))
        (is (clojure.string/includes? last-output "second") (name mode))
        (is (not (clojure.string/includes? last-output "first"))
            (name mode))))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"cannot be used together"
         (cli/convert-input source {:first true :last true} {})))))
