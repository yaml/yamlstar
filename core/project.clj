(defproject yamlstar/core "0.1.3-SNAPSHOT"
  :description "YAMLStar - A pure YAML 1.2 loader for Clojure"
  :url "https://github.com/yaml/yamlstar"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/data.json "2.5.0"]]

  :source-paths ["src"]
  :test-paths ["test"]

  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}
             :uberjar {:aot :all
                       :global-vars {*assert* false
                                     *warn-on-reflection* true}
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]}}

  :repl-options {:init-ns yamlstar.core})
