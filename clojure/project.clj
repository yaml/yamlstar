(defproject org.yamlstar/yamlstar "0.1.2"
  :description "YAMLStar - A pure YAML 1.2 loader for Clojure"
  :url "https://github.com/yaml/yamlstar"

  :license
  {:name "MIT"
   :url "https://opensource.org/license/mit/"}

  :scm
  {:name "git"
   :url "https://github.com/yaml/yamlstar"
   :tag "clojure"
   :dir ".."}

  :dependencies
  [[org.clojure/clojure "1.12.0"]]

  :source-paths ["../core/src"]
  :test-paths ["../core/test"]

  :deploy-repositories
  [["releases"
    {:url "https://repo.clojars.org"
     :username :env/clojars_username
     :password :env/clojars_password
     :sign-releases false}]]

  :repl-options {:init-ns yamlstar.core})
