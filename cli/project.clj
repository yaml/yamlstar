(defproject yamlstar/cli "0.1.0-SNAPSHOT"
  :description "YAMLStar CLI - Pure YAML loader command-line tool"
  :url "https://github.com/yaml/yamlstar"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[yamlstar/core "0.1.0-SNAPSHOT"]
                 [org.clojure/clojure "1.12.0"]
                 [org.clojure/tools.cli "1.1.230"]
                 [org.clojure/data.json "2.5.0"]]

  :main yamlstar.cli
  :aot [yamlstar.cli]

  :profiles {:uberjar {:aot :all
                       :global-vars {*assert* false
                                     *warn-on-reflection* true}
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]}})
