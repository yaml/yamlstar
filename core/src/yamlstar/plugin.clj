(ns yamlstar.plugin
  "YAMLStar parser and JSON-comments plugin support."
  (:require [clojure.string :as str]))

(defonce ^:private parser-registry (atom {}))
(defonce ^:private json-comments-registry (atom {}))
(defonce ^:private json-comments-loader (atom nil))

(def ^:private release-version-pattern
  #"^v?([0-9]+\.[0-9]+\.[0-9]+)$")

(defn normalize-version
  "Normalize VERSION to digits without a leading v."
  [version]
  (when-not (string? version)
    (throw (ex-info "Plugin version must be a release version"
                    {:version version})))
  (or (second (re-matches release-version-pattern version))
      (throw (ex-info "Plugin version must be a release version"
                      {:version version}))))

(defn register-parser!
  "Register a parser plugin map under its :name."
  [{:keys [name parse version] :as plugin}]
  (when-not (and (string? name) (not (str/blank? name)))
    (throw (ex-info "Parser plugin :name must be a string"
                    {:plugin plugin})))
  (when-not (fn? parse)
    (throw (ex-info "Parser plugin :parse must be a function"
                    {:plugin plugin})))
  (let [plugin (cond-> plugin
                 version (assoc :version (normalize-version version)))]
    (swap! parser-registry assoc name plugin)
    plugin))

(defn unregister-parser!
  "Remove the parser plugin registered under name."
  [name]
  (swap! parser-registry dissoc name)
  nil)

(defn registered-parsers
  "Return the registered parser names."
  []
  (sort (keys @parser-registry)))

(defn resolve-parser
  "Look up a parser plugin by name."
  [name]
  (or (get @parser-registry name)
      (try
        (some-> (requiring-resolve
                 (symbol (str "yamlstar.plugin.parser." name) "plugin"))
                deref)
        (catch Exception _ nil))
      (throw (ex-info (str "Unknown YAML parser plugin: " name
                           ". Available: "
                           (if-let [names (seq (registered-parsers))]
                             (str/join ", " names)
                             "none"))
                      {:parser name
                       :available (registered-parsers)}))))

(defn register-json-comments!
  "Register a JSON-comments sanitizer implementation."
  [{:keys [api name sanitize version] :as plugin}]
  (when-not (= api "json-comments")
    (throw (ex-info "JSON-comments plugin :api must be json-comments"
                    {:plugin plugin})))
  (when-not (and (string? name) (not (str/blank? name)))
    (throw (ex-info "JSON-comments plugin :name must be a string"
                    {:plugin plugin})))
  (when-not (fn? sanitize)
    (throw (ex-info "JSON-comments plugin :sanitize must be a function"
                    {:plugin plugin})))
  (let [plugin (cond-> plugin
                 version (assoc :version (normalize-version version)))]
    (swap! json-comments-registry assoc name plugin)
    plugin))

(defn unregister-json-comments!
  "Remove a JSON-comments implementation."
  [name]
  (swap! json-comments-registry dissoc name)
  nil)

(defn set-json-comments-loader!
  "Install a lazy JSON-comments implementation loader."
  [loader]
  (when-not (or (nil? loader) (fn? loader))
    (throw (ex-info "JSON-comments loader must be a function or nil"
                    {:loader loader})))
  (reset! json-comments-loader loader)
  loader)

(defn- classpath-json-comments
  [name]
  (when (= name "sanitizer")
    (try
      (let [sanitize (requiring-resolve
                      'yamlstar-plugin.json-comments/sanitize-comments)
            version (requiring-resolve
                     'yamlstar-plugin.json-comments/version)]
        (when sanitize
          {:api "json-comments"
           :name "sanitizer"
           :version (when version (deref version))
           :sanitize (fn [input _] (sanitize input))
           :default-config {}}))
      (catch Exception _ nil))))

(defn resolve-json-comments
  "Resolve a JSON-comments implementation, loading it when needed."
  [name version install?]
  (let [plugin
        (or (get @json-comments-registry name)
            (when-let [loaded (classpath-json-comments name)]
              (register-json-comments! loaded))
            (when-let [loader @json-comments-loader]
              (when-let [loaded
                         (loader "json-comments" name version install?)]
                (register-json-comments! loaded)))
            (throw
             (ex-info
              (str "Unknown YAMLStar json-comments implementation: " name)
              {:api "json-comments" :name name})))]
    (when version
      (let [requested (normalize-version version)
            linked (:version plugin)]
        (when (not= requested linked)
          (throw
           (ex-info
            (str "JSON-comments plugin version mismatch: linked "
                 (or linked "unversioned") ", requested " requested)
            {:api "json-comments" :name name
             :linked linked :requested requested})))))
    plugin))

(defn- plugin-config
  [opts]
  (when-let [config (:plugin opts)]
    (when-not (map? config)
      (throw (ex-info "Option :plugin must be a map"
                      {:plugin config})))
    config))

(defn- short-config
  [api text]
  (let [parts (str/split text #"@" -1)]
    (when (or (> (count parts) 2) (str/blank? (first parts))
              (and (= 2 (count parts)) (str/blank? (second parts))))
      (throw (ex-info "Invalid plugin short form"
                      {:api api :value text})))
    (cond-> {:name (first parts)}
      (= 2 (count parts))
      (assoc :version (normalize-version (second parts))))))

(defn- selection-config
  [api value]
  (cond
    (true? value) {}
    (false? value) nil
    (string? value) (short-config api value)
    (map? value)
    (do
      (when (and (contains? value :disable)
                 (not (boolean? (:disable value))))
        (throw (ex-info "Plugin :disable must be a boolean"
                        {:api api :value value})))
      (when-not (:disable value)
        (dissoc value :disable)))
    :else
    (throw (ex-info "Plugin config must be a map, string, or boolean"
                    {:api api :value value}))))

(defn parser-opts
  "Extract [parser-name config] from load options."
  [opts]
  (when-let [plugins (plugin-config opts)]
    (when (contains? plugins :parser)
      (when-let [config (selection-config :parser (:parser plugins))]
        (let [name (:name config)]
          (when (and name (not (string? name)))
            (throw (ex-info "Parser plugin :name must be a string"
                            {:name name})))
          [name (dissoc config :name)])))))

(defn json-comments-opts
  "Resolve the configured JSON-comments sanitizer."
  [opts]
  (when-let [plugins (plugin-config opts)]
    (doseq [api (keys (dissoc plugins :parser :json-comments))]
      (throw (ex-info (str "Unknown YAMLStar plugin API: " (name api))
                      {:api api})))
    (when (contains? plugins :json-comments)
      (when-let [config
                 (selection-config
                  :json-comments (:json-comments plugins))]
        (let [name (or (:name config) "sanitizer")
              version (:version config)
              plugin (resolve-json-comments
                      name version (true? (:plugin-install opts)))]
          [plugin (dissoc config :name :version)])))))

(defn sanitize-with
  "Sanitize YAML input with a resolved JSON-comments plugin."
  [[{:keys [sanitize default-config]} config] yaml-str]
  (sanitize (or yaml-str "") (merge default-config config)))

(defn parse-with
  "Resolve the named parser plugin and parse yaml-str with it."
  [name config yaml-str]
  (let [{:keys [parse default-config version]} (resolve-parser name)
        requested (:version config)]
    (when requested
      (let [requested (normalize-version requested)]
        (when (not= requested version)
          (throw
           (ex-info
            (str "Parser plugin version mismatch: linked "
                 (or version "unversioned") ", requested " requested)
            {:parser name :linked version :requested requested})))))
    (parse yaml-str (merge default-config (dissoc config :version)))))
