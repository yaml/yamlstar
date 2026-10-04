(ns yamlstar.plugin.shared
  "Shared-library plugin protocol support for native hosts.")

(def abi-version 2)

(def ^:private artifacts
  {["json-comments" "sanitizer"]
   {:artifact "json-comments"
    :kind "text-transform"
    :operation :sanitize}

   ["parser" "toml"]
   {:artifact "parser-toml"
    :kind "event-source"
    :operation :parse}})

(defn- read-edn
  [text context]
  (try
    (read-string text)
    (catch #?(:clj Exception :glj go/any) error
      (throw (ex-info (str "Invalid EDN from shared plugin " context)
                      {:context context :text text}
                      error)))))

(defn- require-value
  [manifest key expected]
  (when-not (= expected (get manifest key))
    (throw (ex-info
            (str "Shared plugin manifest " (name key) " must be "
                 (pr-str expected))
            {:key key :expected expected :actual (get manifest key)}))))

(defn validate-manifest
  "Validate a shared plugin manifest for the requested implementation."
  [manifest api name kind]
  (when-not (map? manifest)
    (throw (ex-info "Shared plugin manifest must be an EDN map"
                    {:manifest manifest})))
  (require-value manifest :abi abi-version)
  (require-value manifest :api api)
  (require-value manifest :name name)
  (require-value manifest :kind kind)
  manifest)

(defn- invoke
  [transform-fn api artifact name input options]
  (let [[status output]
        (transform-fn api artifact (or input "") (pr-str options))]
    (case (long status)
      0 output
      1 (throw (ex-info output
                        {:api api :name name :kind :transform}))
      (throw
       (ex-info "Shared plugin ABI call failed"
                {:api api :name name :status status
                 :response output})))))

(defn make-loader
  "Create a native plugin loader from shared host functions."
  [manifest-fn transform-fn]
  (fn [api name _version install?]
    (when-let [{:keys [artifact kind operation]}
               (get artifacts [api name])]
      (let [manifest (-> (manifest-fn api artifact install?)
                         (read-edn "manifest")
                         (validate-manifest api name kind))
            base {:api api
                  :name name
                  :version (:version manifest)
                  :manifest manifest
                  :default-config {}}]
        (case operation
          :sanitize
          (assoc base :sanitize
                 (fn [input options]
                   (invoke transform-fn api artifact name input options)))

          :parse
          (assoc base :parse
                 (fn [input options]
                   (-> (invoke transform-fn api artifact name input options)
                       (read-edn "event stream")))))))))
