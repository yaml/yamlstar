;; Run with: clj -M yaml-to-json.clj [input.yaml]

(require '[yamlstar.core :as yaml]
         '[clojure.data.json :as json])

(defn -main
  [& args]
  (let [yaml-file (or (first args) "sample.yaml")]
    (println (str "YAMLStar Example - Loading " yaml-file " and outputting JSON\n"))

    ;; Read the YAML file
    (let [yaml-content (slurp yaml-file)
          _ (println "Input YAML:")
          _ (println yaml-content)
          _ (println "\n---\n")

          ;; Parse YAML to Clojure data
          data (yaml/load yaml-content)

          ;; Convert to JSON
          json-output (json/write-str data :indent true)]

      (println "Output JSON:")
      (println json-output))))

;; Call -main when run as script
(apply -main *command-line-args*)
