(defproject yamlstar/libyamlstar "0.1.2-SNAPSHOT"
  :description "Shared Library for YAMLStar"
  :url "https://github.com/yaml/yamlstar"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[yamlstar/core "0.1.2-SNAPSHOT"]
                 [org.clojure/clojure "1.12.0"]
                 [org.clojure/data.json "2.5.0"]]

  :prep-tasks [["compile"] ["javac"]]

  :java-source-paths ["src"]

  :profiles
  {:uberjar
   {:aot :all
    :main libyamlstar.core
    :global-vars {*assert* false
                  *warn-on-reflection* true}
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"
               "-Dclojure.spec.skip-macros=true"]}})
