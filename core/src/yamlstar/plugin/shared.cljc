(ns yamlstar.plugin.shared
  "Shared-library text-transform protocol support for native hosts.")

(def abi-version 2)

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
  "Validate a shared plugin manifest for the requested API and name."
  [manifest api name]
  (when-not (map? manifest)
    (throw (ex-info "Shared plugin manifest must be an EDN map"
                    {:manifest manifest})))
  (require-value manifest :abi abi-version)
  (require-value manifest :api api)
  (require-value manifest :name name)
  (require-value manifest :kind "text-transform")
  manifest)

(defn make-loader
  "Create a JSON-comments loader from native host functions.

  manifest-fn receives api, artifact name, and install? and returns EDN.
  transform-fn receives api, artifact name, input, and options EDN and
  returns [status output]."
  [manifest-fn transform-fn]
  (fn [api name _version install?]
    (when-not (and (= api "json-comments") (= name "sanitizer"))
      (throw (ex-info "No shared plugin artifact for implementation"
                      {:api api :name name})))
    (let [artifact "json-comments"
          manifest (-> (manifest-fn api artifact install?)
                       (read-edn "manifest")
                       (validate-manifest api name))]
      {:api api
       :name name
       :version (:version manifest)
       :manifest manifest
       :default-config {}
       :sanitize
       (fn [input options]
         (let [[status output]
               (transform-fn api artifact (or input "")
                             (pr-str options))]
           (case (long status)
             0 output
             1 (throw (ex-info output
                               {:api api :name name :kind :transform}))
             (throw
              (ex-info "Shared plugin ABI call failed"
                       {:api api :name name :status status
                        :response output})))))})))
