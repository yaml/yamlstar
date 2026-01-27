(defproject yamlstar-example "0.1.1"
  :description "Example usage of YAMLStar from Clojars"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [com.yaml/yamlstar "0.1.1"]
                 [org.clojure/data.json "2.5.0"]]

  :plugins [[io.github.borkdude/lein-lein2deps "0.1.0"]
            [lein-exec "0.3.7"]]

  :prep-tasks [["lein2deps" "--write-file" "deps.edn" "--print" "false"]])
