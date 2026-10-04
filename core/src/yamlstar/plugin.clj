(ns yamlstar.plugin
  "YAMLStar parser, emitter, and JSON-comments plugin support."
  (:require [clojure.string :as str]
            [yamlstar.plugin.alias-data :as alias-data]))

(defonce ^:private parser-registry (atom {}))
(defonce ^:private parser-loader (atom nil))
(defonce ^:private yaml-emitter-registry (atom {}))
(defonce ^:private json-comments-registry (atom {}))
(defonce ^:private json-comments-loader (atom nil))
(defonce ^:private alias-data-registry
  (atom {"alias-data"
         {:api "alias-data"
          :name "alias-data"
          :new-context alias-data/new-context}}))

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
  "Register a YAML parser plugin map under its :name."
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
  "Remove the YAML parser plugin registered under name."
  [name]
  (swap! parser-registry dissoc name)
  nil)

(defn registered-parsers
  "Return the registered YAML parser names."
  []
  (sort (keys @parser-registry)))

(defn set-parser-loader!
  "Install a lazy parser implementation loader."
  [loader]
  (when-not (or (nil? loader) (fn? loader))
    (throw (ex-info "Parser loader must be a function or nil"
                    {:loader loader})))
  (reset! parser-loader loader)
  loader)

(defn resolve-parser
  "Look up a YAML parser plugin by name, loading it when needed."
  ([name]
   (resolve-parser name nil false))
  ([name version install?]
   (let [parser
         (or (get @parser-registry name)
             (try
               (some-> (requiring-resolve
                        (symbol (str "yamlstar.plugin.parser." name)
                                "plugin"))
                       deref)
               (catch Exception _ nil))
             (when-let [loader @parser-loader]
               (when-let [loaded (loader "parser" name version install?)]
                 (register-parser! loaded)))
             (throw (ex-info (str "Unknown YAML parser plugin: " name
                                  ". Available: "
                                  (if-let [names (seq (registered-parsers))]
                                    (str/join ", " names)
                                    "none"))
                             {:parser name
                              :available (registered-parsers)})))]
     (when version
       (let [requested (normalize-version version)
             linked (:version parser)]
         (when (not= requested linked)
           (throw
            (ex-info
             (str "Parser plugin version mismatch: linked "
                  (or linked "unversioned") ", requested " requested)
             {:parser name
              :linked linked
              :requested requested})))))
     parser)))

(defn register-yaml-emitter!
  "Register a YAML emitter plugin map under its :name."
  [{:keys [name emit version] :as plugin}]
  (when-not (and (string? name) (not (str/blank? name)))
    (throw (ex-info "Emitter plugin :name must be a string"
                    {:plugin plugin})))
  (when-not (fn? emit)
    (throw (ex-info "Emitter plugin :emit must be a function"
                    {:plugin plugin})))
  (let [plugin (cond-> plugin
                 version (assoc :version (normalize-version version)))]
    (swap! yaml-emitter-registry assoc name plugin)
    plugin))

(defn unregister-yaml-emitter!
  "Remove the YAML emitter plugin registered under name."
  [name]
  (swap! yaml-emitter-registry dissoc name)
  nil)

(defn registered-yaml-emitters
  "Return the registered YAML emitter names."
  []
  (sort (keys @yaml-emitter-registry)))

(defn resolve-yaml-emitter
  "Look up a YAML emitter plugin by name."
  [name]
  (or (get @yaml-emitter-registry name)
      (try
        (some-> (requiring-resolve
                 (symbol (str "yamlstar.plugin.yaml-emitter." name)
                         "plugin"))
                deref)
        (catch Exception _ nil))
      (throw (ex-info (str "Unknown YAML emitter plugin: " name
                           ". Available: "
                           (if-let [names (seq (registered-yaml-emitters))]
                             (str/join ", " names)
                             "none"))
                      {:yaml-emitter name
                       :available (registered-yaml-emitters)}))))

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

(defn register-alias-data!
  "Register an Alias-Data policy implementation."
  [{:keys [api name new-context version] :as plugin}]
  (when-not (= api "alias-data")
    (throw (ex-info "Alias-Data plugin :api must be alias-data"
                    {:plugin plugin})))
  (when-not (and (string? name) (not (str/blank? name)))
    (throw (ex-info "Alias-Data plugin :name must be a string"
                    {:plugin plugin})))
  (when-not (fn? new-context)
    (throw (ex-info "Alias-Data plugin :new-context must be a function"
                    {:plugin plugin})))
  (let [plugin (cond-> plugin
                 version (assoc :version (normalize-version version)))]
    (swap! alias-data-registry assoc name plugin)
    plugin))

(defn unregister-alias-data!
  "Remove an Alias-Data implementation."
  [name]
  (swap! alias-data-registry dissoc name)
  nil)

(defn registered-alias-data
  "Return the registered Alias-Data implementation names."
  []
  (sort (keys @alias-data-registry)))

(defn resolve-alias-data
  "Resolve a named Alias-Data implementation."
  [name]
  (or (get @alias-data-registry name)
      (throw (ex-info
              (str "Unknown YAMLStar alias-data implementation: " name)
              {:api "alias-data" :name name
               :available (registered-alias-data)}))))

(defn- plugin-config
  [opts]
  (when-let [config (:plugin opts)]
    (when-not (map? config)
      (throw (ex-info "Option :plugin must be a map"
                      {:plugin config})))
    (when (contains? config :yaml-parser)
      (throw (ex-info "Unknown YAMLStar plugin API: yaml-parser"
                      {:api :yaml-parser})))
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
      (when-let [config
                 (selection-config
                  :parser (:parser plugins))]
        (let [name (:name config)]
          (when (and name (not (string? name)))
            (throw (ex-info "Parser plugin :name must be a string"
                            {:name name})))
          [name (dissoc config :name)])))))

(defn yaml-emitter-opts
  "Extract [emitter-name config] from dump options."
  [opts]
  (when-let [plugins (plugin-config opts)]
    (when (contains? plugins :yaml-emitter)
      (when-let [config
                 (selection-config
                  :yaml-emitter (:yaml-emitter plugins))]
        (let [name (:name config)]
          (when (and name (not (string? name)))
            (throw (ex-info "Emitter plugin :name must be a string"
                            {:name name})))
          [name (dissoc config :name)])))))

(defn tab-indent-config
  "Validate and normalize the built-in tab-indent plugin configuration."
  [opts]
  (when-let [plugins (plugin-config opts)]
    (let [raw (:tab-indent plugins)]
      (when-not (or (nil? raw) (false? raw))
        (let [config (cond
                       (true? raw) {}
                       (map? raw) raw
                       :else
                       (throw
                        (ex-info
                         "Plugin config :tab-indent must be a map or boolean"
                         {:tab-indent raw})))
              name (:name config)
              disabled (:disable config)
              config (dissoc config :name :disable)
              unknown (seq
                       (remove #{:mode :load :dump :auto} (keys config)))]
          (when (and name (not= name "tab-indent"))
            (throw (ex-info
                    "Built-in tab-indent plugin name must be tab-indent"
                    {:name name})))
          (when-not (or (nil? disabled) (boolean? disabled))
            (throw (ex-info "tab-indent :disable must be boolean"
                            {:disable disabled})))
          (when unknown
            (throw (ex-info "Unknown tab-indent configuration key"
                            {:keys unknown})))
          (when-not disabled
            (let [mode (or (:mode config) "auto")
                  load (or (:load config)
                           (if (= mode "tabs") "tabs" "auto"))
                  dump (or (:dump config) "tabs")
                  auto (or (:auto config) "document")]
              (when-not (contains? #{"auto" "tabs"} mode)
                (throw (ex-info "tab-indent mode must be auto or tabs"
                                {:mode mode})))
              (when-not (contains? #{"auto" "spaces" "tabs"} load)
                (throw (ex-info
                        "tab-indent load must be auto, spaces, or tabs"
                        {:load load})))
              (when-not (contains? #{"spaces" "tabs"} dump)
                (throw (ex-info
                        "tab-indent dump must be spaces or tabs"
                        {:dump dump})))
              (when-not (contains? #{"document" "stream"} auto)
                (throw (ex-info
                        "tab-indent auto must be document or stream"
                        {:auto auto})))
              {:mode mode :load load :dump dump :auto auto})))))))

(defn json-comments-opts
  "Resolve the configured JSON-comments sanitizer."
  [opts]
  (when-let [plugins (plugin-config opts)]
    (doseq [api (keys (dissoc plugins
                              :parser
                              :yaml-emitter
                              :tab-indent
                              :alias-data
                              :json-comments))]
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

(defn alias-data-context
  "Create the per-load Alias-Data context selected by opts."
  [opts services]
  (let [plugins (plugin-config opts)]
    (if-not (and plugins (contains? plugins :alias-data))
      (alias-data/default-context)
      (if-let [selection
               (selection-config :alias-data (:alias-data plugins))]
        (let [name (or (:name selection) "alias-data")
              requested (:version selection)
              plugin (resolve-alias-data name)
              linked (:version plugin)
              config (dissoc selection :name :version)
              config (if (empty? config) {:stream true} config)]
          (when requested
            (let [requested (normalize-version requested)]
              (when (not= requested linked)
                (throw
                 (ex-info
                  (str "Alias-Data plugin version mismatch: linked "
                       (or linked "unversioned") ", requested " requested)
                  {:api "alias-data" :name name
                   :linked linked :requested requested})))))
          ((:new-context plugin) config services))
        (alias-data/default-context)))))

(defn sanitize-with
  "Sanitize YAML input with a resolved JSON-comments plugin."
  [[{:keys [sanitize default-config]} config] yaml-str]
  (sanitize (or yaml-str "") (merge default-config config)))

(defn parse-with
  "Resolve the named parser plugin and parse yaml-str with it."
  ([name config yaml-str]
   (parse-with name config yaml-str false))
  ([name config yaml-str install?]
   (let [{:keys [parse default-config]}
         (resolve-parser name (:version config) install?)]
     (parse yaml-str (merge default-config (dissoc config :version))))))

(defn emit-with
  "Resolve the named YAML emitter plugin and emit an event stream."
  [name config events multi?]
  (let [{:keys [emit default-config version]}
        (resolve-yaml-emitter name)
        requested (:version config)]
    (when requested
      (let [requested (normalize-version requested)]
        (when (not= requested version)
          (throw
           (ex-info
            (str "Emitter plugin version mismatch: linked "
                 (or version "unversioned") ", requested " requested)
            {:yaml-emitter name
             :linked version
             :requested requested})))))
    (emit events multi?
          (merge default-config (dissoc config :version)))))
