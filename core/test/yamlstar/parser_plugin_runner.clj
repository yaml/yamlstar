(ns yamlstar.parser-plugin-runner
  (:require [clojure.test :as test]
            [yamlstar.parser :as parser]
            [yamlstar.plugin-test]
            [yamlstar.shared-plugin-test]
            [yamlstar.cli-options-test]))

(defn -main [& _]
  (parser/register-reference-parser!)
  (let [{:keys [fail error]}
        (test/run-tests 'yamlstar.plugin-test
                        'yamlstar.shared-plugin-test
                        'yamlstar.cli-options-test)]
    (System/exit (+ fail error))))
