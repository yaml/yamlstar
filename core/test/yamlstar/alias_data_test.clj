(ns yamlstar.alias-data-test
  (:require [clojure.test :refer [deftest is testing]]
            [yamlstar.api :as yaml]
            [yamlstar.plugin :as plugin]
            [yamlstar.plugin.alias-data :as alias-data]))

(deftest inline-data-and-merge-test
  (is (= {"color" "blue" "size" 5}
         (yaml/load
          "<<: *defaults\nsize: 5\n"
          {:plugin
           {:alias-data
            {:data
             {"defaults" {"color" "blue" "size" 3}}}}}))))

(deftest document-scope-test
  (let [input "--- &saved {x: 1}\n---\ncopy: *saved\n"]
    (testing "standard YAML scope rejects cross-document aliases"
      (is (thrown-with-msg? Exception #"Unknown anchor: saved"
                            (yaml/load-all input))))
    (testing "explicit empty selection enables stream anchors"
      (is (= [{"x" 1} {"copy" {"x" 1}}]
             (yaml/load-all input {:plugin {:alias-data true}}))))))

(deftest source-precedence-test
  (with-redefs [alias-data/environment
                (fn [] {"ALIAS_ENV" "environment"
                        "shared" "environment"})]
    (let [file (java.io.File/createTempFile "alias-data" ".yaml")]
      (try
        (spit file "file_only: {source: file}\nshared: file\n")
        (is (= {"inline" "inline"
                "file" {"source" "file"}
                "env" "environment"
                "local" "local"
                "winner" "local"}
               (yaml/load
                (str "inline: *shared\n"
                     "file: *file_only\n"
                     "env: *ALIAS_ENV\n"
                     "local: &shared local\n"
                     "winner: *shared\n")
                {:plugin
                 {:alias-data
                  {:data {"shared" "inline"}
                   :file (.getPath file)
                   :env "ALIAS_*"}}})))
        (finally
          (.delete file))))))

(deftest environment-validation-test
  (with-redefs [alias-data/environment
                (fn [] {"GOOD_NAME" "yes" "bad.name" "no"})]
    (is (= {"value" "yes"}
           (yaml/load "value: *GOOD_NAME\n"
                      {:plugin {:alias-data {:env ["GOOD_NAME"]}}})))
    (is (thrown-with-msg?
         Exception
         #"must match"
         (yaml/load "value: true\n"
                    {:plugin {:alias-data {:env true}}})))
    (is (thrown-with-msg?
         Exception
         #"is not set"
         (yaml/load "value: true\n"
                    {:plugin {:alias-data {:env ["MISSING"]}}})))))

(deftest lifecycle-test
  (let [actions (atom [])
        anchors (atom {})
        context
        {:begin-stream #(swap! actions conj :begin-stream)
         :begin-document (fn []
                           (swap! actions conj :begin-document)
                           (reset! anchors {}))
         :define-anchor (fn [name value]
                          (swap! actions conj [:define name])
                          (swap! anchors assoc name value))
         :resolve-alias (fn [name]
                          (swap! actions conj [:resolve name])
                          [(contains? @anchors name) (get @anchors name)])
         :end-document #(swap! actions conj :end-document)
         :end-stream #(swap! actions conj :end-stream)}]
    (plugin/register-alias-data!
     {:api "alias-data"
      :name "trace"
      :new-context (fn [_ _] context)})
    (try
      (is (= {"value" 1 "copy" 1}
             (yaml/load "value: &a 1\ncopy: *a\n"
                        {:plugin {:alias-data {:name "trace"}}})))
      (is (= [:begin-stream
              :begin-document
              [:define "a"]
              [:resolve "a"]
              :end-document
              :end-stream]
             @actions))
      (finally
        (plugin/unregister-alias-data! "trace")))))

(deftest invalid-configuration-test
  (is (thrown-with-msg?
       Exception
       #"must match"
       (yaml/load "value: true\n"
                  {:plugin
                   {:alias-data {:data {"bad.name" "value"}}}})))
  (is (thrown-with-msg?
       Exception
       #"Unknown Alias-Data configuration key"
       (yaml/load "value: true\n"
                  {:plugin {:alias-data {:unknown true}}}))))
