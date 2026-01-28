(defproject com.yaml/yamlstar "0.1.3"
  :description "YAMLStar - YAML 1.2 loader for Java"
  :url "https://github.com/yaml/yamlstar"

  :license
  {:name "MIT"
   :url "https://opensource.org/license/mit/"}

  :scm
  {:name "git"
   :url "https://github.com/yaml/yamlstar"
   :tag "java"
   :dir ".."}

  :pom-addition
  [:developers
   [:developer
    [:id "ingydotnet"]
    [:name "Ingy döt Net"]
    [:email "ingy@ingy.net"]
    [:url "https://github.com/ingydotnet"]]]

  :dependencies
  [[org.clojure/clojure "1.12.0"]]

  :source-paths ["src" "../core/src"]
  :test-paths ["test"]

  :aot [com.yaml.yamlstar]

  :deploy-repositories
  [["releases"
    {:url "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
     :username :env/ossrh_username
     :password :env/ossrh_password
     :sign-releases false}]]

  :repl-options {:init-ns com.yaml.yamlstar})
