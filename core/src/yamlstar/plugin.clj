(ns yamlstar.plugin
  "YAMLStar plugin system.

  A plugin can replace or extend parts of the YAML load and dump stacks.
  Parser plugins swap the parser implementation.
  Event-source plugins produce the complete event stream and therefore
  supersede the parser for a load operation.

  A parser plugin is a plain map:

    {:name \"snakeyaml\"                          ; string, registry key
     :parse (fn [yaml-str config] ...)            ; -> seq of event maps
     :default-config {}}                          ; optional

  The :parse function must return the standard YAMLStar event stream:
  a sequence of maps like {:event \"scalar\" :value \"hello\"} using the
  event vocabulary consumed by yamlstar.composer.

  Plugins are selected per load operation via the opts map:

    {:plugin {:parser {:name \"snakeyaml\"}}}

  Keys other than :name under :parser are passed to the plugin's :parse
  function as its config argument, merged over :default-config."
  (:require [clojure.string :as str]))

(defonce ^:private parser-registry (atom {}))
(defonce ^:private event-source-registry (atom {}))
(defonce ^:private event-source-loader (atom nil))

(def ^:private event-types
  #{"stream_start" "stream_end"
    "document_start" "document_end"
    "mapping_start" "mapping_end"
    "sequence_start" "sequence_end"
    "scalar" "alias"})

(defn register-parser!
  "Register a parser plugin map under its :name.

  Required keys: :name (string), :parse (fn [yaml-str config]).
  Optional keys: :default-config (map).

  Re-registering a name replaces the previous plugin.
  Returns the plugin map."
  [{:keys [name parse] :as plugin}]
  (when-not (string? name)
    (throw (ex-info "Parser plugin :name must be a string"
                    {:plugin plugin})))
  (when-not (fn? parse)
    (throw (ex-info "Parser plugin :parse must be a function"
                    {:plugin plugin})))
  (swap! parser-registry assoc name plugin)
  plugin)

(defn unregister-parser!
  "Remove the parser plugin registered under name."
  [name]
  (swap! parser-registry dissoc name)
  nil)

(defn registered-parsers
  "Return a sorted sequence of registered parser plugin names."
  []
  (sort (keys @parser-registry)))

(defn register-event-source!
  "Register an event-source plugin under [api name]."
  [{:keys [api name parse requires] :as plugin}]
  (when-not (and (string? api) (not (str/blank? api)))
    (throw (ex-info "Event-source plugin :api must be a string"
                    {:plugin plugin})))
  (when-not (and (string? name) (not (str/blank? name)))
    (throw (ex-info "Event-source plugin :name must be a string"
                    {:plugin plugin})))
  (when-not (fn? parse)
    (throw (ex-info "Event-source plugin :parse must be a function"
                    {:plugin plugin})))
  (when (and requires
             (or (not (map? requires))
                 (not (string? (:parser requires)))))
    (throw (ex-info
            "Event-source plugin :requires must name a parser"
            {:plugin plugin})))
  (swap! event-source-registry assoc [api name] plugin)
  plugin)

(defn unregister-event-source!
  "Remove an event-source plugin registered under [api name]."
  [api name]
  (swap! event-source-registry dissoc [api name])
  nil)

(defn set-event-source-loader!
  "Install the native host's lazy shared-library loader.

  The loader receives api and name strings and returns an event-source
  plugin map. Passing nil disables external plugin loading."
  [loader]
  (when-not (or (nil? loader) (fn? loader))
    (throw (ex-info "Event-source loader must be a function or nil"
                    {:loader loader})))
  (reset! event-source-loader loader)
  loader)

(defn resolve-event-source
  "Resolve an event-source by API and name, loading it on first use."
  [api name]
  (or (get @event-source-registry [api name])
      (when-let [loader @event-source-loader]
        (when-let [loaded (loader api name)]
          (register-event-source! loaded)))
      (throw (ex-info
              (str "Unknown YAMLStar event-source plugin: " api "=" name)
              {:api api :name name :kind "event-source"}))))

(defn resolve-parser
  "Look up a parser plugin by name.

  If the name is not registered, tries to load the namespace
  yamlstar.plugin.parser.<name> and use its `plugin` var (which is expected
  to self-register). Throws if no plugin can be found."
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

(defn- plugin-config
  [opts]
  (when-let [config (:plugin opts)]
    (when-not (map? config)
      (throw (ex-info "Option :plugin must be a map"
                      {:plugin config})))
    config))

(defn parser-opts
  "Extract [parser-name config] from a load opts map.

  Returns nil when opts selects no parser plugin (the fast path).
  The config is the :parser map without :name, merged over the plugin's
  :default-config by the caller.

  Throws on malformed opts:
  - :plugin value is not a map
  - a plugin type other than :parser is configured
  - :parser value is not a map
  - :name value is not a string"
  [opts]
  (when-let [plugin-cfg (plugin-config opts)]
    (when-let [parser-cfg (:parser plugin-cfg)]
      (when-not (map? parser-cfg)
        (throw (ex-info "Plugin config :parser must be a map"
                        {:parser parser-cfg})))
      (let [name (:name parser-cfg)]
        (when-not (string? name)
          (throw (ex-info "Parser plugin :name must be a string name"
                          {:name name})))
        [name (dissoc parser-cfg :name)]))))

(defn event-source-opts
  "Resolve the one configured event-source plugin, if any.

  The parser plugin may be omitted or explicitly set to reference.
  More than one event-source and any other explicit parser are rejected."
  [opts]
  (when-let [plugin-cfg (plugin-config opts)]
    (let [sources (seq (dissoc plugin-cfg :parser))]
      (when (> (count sources) 1)
        (throw (ex-info "Only one event-source plugin may be enabled"
                        {:plugins (mapv key sources)})))
      (when-let [[api config] (first sources)]
        (when-not (map? config)
          (throw (ex-info "Event-source plugin config must be a map"
                          {:api api :config config})))
        (let [api-name (name api)
              plugin-name (or (:name config) api-name)]
          (when-not (string? plugin-name)
            (throw (ex-info "Event-source plugin :name must be a string"
                            {:api api-name :name plugin-name})))
          (let [source (resolve-event-source api-name plugin-name)
                required-parser (get-in source [:requires :parser])
                parser-name (first (parser-opts opts))]
            (when (and required-parser parser-name
                       (not= parser-name required-parser))
              (throw
               (ex-info
                (str "Event-source plugin " api-name
                     " requires parser " required-parser ", but "
                     parser-name " was explicitly selected")
                {:api api-name
                 :requires (:requires source)
                 :parser parser-name})))
            [source (dissoc config :name)]))))))

(defn validate-events
  "Validate and return a plugin-produced YAMLStar event vector."
  [events]
  (when-not (vector? events)
    (throw (ex-info "Event-source plugin result must be a vector"
                    {:result-type (type events)})))
  (doseq [[index event] (map-indexed vector events)]
    (when-not (map? event)
      (throw (ex-info "Event-source plugin event must be a map"
                      {:index index :event event})))
    (when-not (contains? event-types (:event event))
      (throw (ex-info "Event-source plugin returned an invalid event"
                      {:index index :event event}))))
  events)

(defn parse-with-event-source
  "Parse yaml-str using a resolved event-source plugin."
  [[{:keys [parse default-config]} config] yaml-str]
  (validate-events (parse yaml-str (merge default-config config))))

(defn parse-with
  "Resolve the named parser plugin and parse yaml-str with it.

  config is merged over the plugin's :default-config."
  [name config yaml-str]
  (let [{:keys [parse default-config]} (resolve-parser name)]
    (parse yaml-str (merge default-config config))))
