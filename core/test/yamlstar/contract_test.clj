(ns yamlstar.contract-test
  (:require [clojure.test :refer :all]
            [yamlstar.api :as yaml]
            [yamlstar.contract :as contract]))

(def sample "a: &x [1, \"two\"]\nb: *x\n")

(deftest retired-node-key-is-rejected
  (doseq [source ["kind: Scalar\nvalue: old\n"
                  "node: Scalar\nkind: Scalar\nvalue: old\n"
                  "node: Document\ncontent:\n- kind: Scalar\n  value: old\n"
                  (str "node: Document\ncontent:\n- node: Scalar\n"
                       "  kind: Scalar\n  value: old\n")]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (contract/contract-nodes
                  (:value (contract/read-contract source "node"))))))
  (is (= :yaml (:stage (contract/read-contract
                       "kind: Scalar\nvalue: old\n" nil)))))

(deftest event-and-node-contracts-chain
  (let [events (contract/yaml-events sample)
        external-events (contract/event-contract events)
        imported-events (contract/contract-events external-events)
        nodes (contract/events-nodes imported-events)
        detailed (contract/node-contract nodes true)
        imported-nodes (contract/contract-nodes detailed)]
    (is (= "STREAM-START" (get (first external-events) "event")))
    (is (= "STREAM-END" (get (last external-events) "event")))
    (is (= "Document" (get detailed "node")))
    (is (= sample (contract/events-yaml imported-events)))
    (is (= sample (contract/nodes-yaml imported-nodes)))))

(deftest compact-nodes-chain
  (let [nodes (contract/events-nodes (contract/yaml-events sample))
        compact (contract/node-contract nodes false)
        output (contract/nodes-yaml (contract/contract-nodes compact))
        loaded (try (yaml/load output) (catch Exception _ ::invalid-yaml))]
    (is (= (yaml/load sample)
           loaded)
        output)))

(deftest input-stage-detection-and-direction
  (let [events (contract/event-contract (contract/yaml-events sample))]
    (is (= :event (:stage (contract/read-contract
                           (yaml/dump events) nil))))
    (is (= :yaml (:stage (contract/read-contract
                          (yaml/dump events) "yaml"))))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"cannot convert event input backward to token output"
         (contract/check-forward! :event :token)))))

(deftest stream-node-wrappers-are-not-documents
  (let [stream {"node" "Stream" "encoding" "UTF-8"}
        document {"node" "Document"
                  "content" [{"node" "Scalar"
                              "style" "Plain"
                              "tag" "!!str"
                              "value" "value"}]}
        nodes (contract/contract-nodes [stream document stream])]
    (is (= 1 (count nodes)))
    (is (= "value" (yaml/load (contract/nodes-yaml nodes))))))
