(ns yamlstar.cli-test
  (:require [clojure.test :refer :all]
            [yamlstar.api :as yaml]
            [yamlstar.cli :as cli]))

(def sample "a: &x [1, \"two\"]\nb: *x\n")

(deftest yaml-event-node-yaml-chain
  (let [events (cli/convert-input sample {:event true})
        nodes (cli/convert-input events {:NODE true})
        output (cli/convert-input nodes {:YAML true})]
    (is (= sample output))))

(deftest forced-yaml-disambiguates-contract-shaped-data
  (let [source "- {event: STREAM-START}\n- {event: STREAM-END}\n"
        output (cli/convert-input source {:from "yaml" :node true})]
    (is (= [{"event" "STREAM-START"} {"event" "STREAM-END"}]
           (yaml/load (cli/convert-input output {:YAML true}))))))

(deftest token-stage-is-an-explicit-follow-up
  (let [tokens "- token: STREAM-START\n- token: STREAM-END\n"]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"yaml-parser token support is the explicit follow-up"
         (cli/convert-input tokens {:YAML true})))))

(deftest backward-stage-conversion-is-rejected
  (let [nodes (cli/convert-input sample {:node true})]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"cannot convert node input backward to event output"
         (cli/convert-input nodes {:event true})))))
