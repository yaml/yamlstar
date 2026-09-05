(ns yamlstar.plugin.shared
  "Shared-library event-source protocol support for native hosts.")

(def abi-version 1)
(def event-format "yamlstar-events-edn-v1")

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
  (require-value manifest :kind "event-source")
  (require-value manifest :event-format event-format)
  (require-value manifest :requires {:parser "reference"})
  manifest)

(defn- plugin-error
  [response]
  (let [error (:error response)]
    (if (map? error)
      (ex-info (or (:message error) "Shared plugin parse failed")
               (assoc (or (:data error) {})
                      :plugin-error-type (:type error)))
      (ex-info "Shared plugin returned an invalid error envelope"
               {:response response}))))

(defn make-loader
  "Create a YAMLStar event-source loader from native host functions.

  manifest-fn receives api and name and returns manifest EDN.
  parse-fn receives api, name, input, and options EDN and returns
  [status output-edn]."
  [manifest-fn parse-fn]
  (fn [api name]
    (let [manifest (-> (manifest-fn api name)
                       (read-edn "manifest")
                       (validate-manifest api name))]
      {:api api
       :name name
       :manifest manifest
       :requires (:requires manifest)
       :default-config {}
       :parse
       (fn [input options]
         (let [[status output] (parse-fn api name (or input "")
                                          (pr-str options))
               response (read-edn output "parse response")]
           (case (long status)
             0 response
             1 (throw (plugin-error response))
             (throw
              (ex-info "Shared plugin ABI call failed"
                       {:api api :name name :status status
                        :response response})))))})))
