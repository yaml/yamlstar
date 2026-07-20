(ns yamlstar.plugin
  "YAMLStar plugin system.

  A plugin can replace or extend parts of the YAML load and dump stacks.
  The first supported plugin type is the parser plugin, which swaps the
  entire parser implementation.

  A parser plugin is a plain map:

    {:name \"snakeyaml\"                          ; string, registry key
     :parse (fn [yaml-str config] ...)            ; -> seq of event maps
     :default-config {}}                          ; optional

  The :parse function must return the standard YAMLStar event stream:
  a sequence of maps like {:event \"scalar\" :value \"hello\"} using the
  event vocabulary consumed by yamlstar.composer.

  Plugins are selected per load operation via the opts map:

    {:plugin {:parser {:use \"snakeyaml\"}}}

  Keys other than :use under :parser are passed to the plugin's :parse
  function as its config argument, merged over :default-config."
  (:require [clojure.string :as str]))

(defonce ^:private parser-registry (atom {}))

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

(defn resolve-parser
  "Look up a parser plugin by name.

  If the name is not registered, tries to load the namespace
  yamlstar.plugin.<name> and use its `plugin` var (which is expected
  to self-register). Throws if no plugin can be found."
  [name]
  (or (get @parser-registry name)
      (try
        (some-> (requiring-resolve
                  (symbol (str "yamlstar.plugin." name) "plugin"))
                deref)
        (catch Exception _ nil))
      (throw (ex-info (str "Unknown YAML parser plugin: " name
                           ". Available: "
                           (if-let [names (seq (registered-parsers))]
                             (str/join ", " names)
                             "none"))
                      {:parser name
                       :available (registered-parsers)}))))

(defn parser-opts
  "Extract [parser-name config] from a load opts map.

  Returns nil when opts selects no parser plugin (the fast path).
  The config is the :parser map without :use, merged over the plugin's
  :default-config by the caller.

  Throws on malformed opts:
  - :plugin value is not a map
  - a plugin type other than :parser is configured
  - :parser value is not a map
  - :use value is not a string"
  [opts]
  (when-let [plugin-cfg (:plugin opts)]
    (when-not (map? plugin-cfg)
      (throw (ex-info "Option :plugin must be a map"
                      {:plugin plugin-cfg})))
    (when-let [unknown (seq (dissoc plugin-cfg :parser))]
      (throw (ex-info (str "Unknown plugin type(s): "
                           (pr-str (mapv key unknown))
                           ". Supported: [:parser]")
                      {:unknown (mapv key unknown)})))
    (when-let [parser-cfg (:parser plugin-cfg)]
      (when-not (map? parser-cfg)
        (throw (ex-info "Plugin config :parser must be a map"
                        {:parser parser-cfg})))
      (let [use (:use parser-cfg)]
        (when-not (string? use)
          (throw (ex-info "Parser plugin :use must be a string name"
                          {:use use})))
        [use (dissoc parser-cfg :use)]))))

(defn parse-with
  "Resolve the named parser plugin and parse yaml-str with it.

  config is merged over the plugin's :default-config."
  [name config yaml-str]
  (let [{:keys [parse default-config]} (resolve-parser name)]
    (parse yaml-str (merge default-config config))))
