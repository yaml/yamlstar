(ns yamlstar.plugin.alias-data
  "Default anchor policy and Alias-Data source handling."
  (:require [clojure.string :as str]))

(def anchor-name-pattern #"^[A-Za-z0-9_-]+$")

(defn valid-name
  [value]
  (let [name (cond
               (string? value) value
               (keyword? value) (name value)
               :else nil)]
    (when-not (and name (re-matches anchor-name-pattern name))
      (throw (ex-info
              (str "Alias-Data key " (pr-str value)
                   " must match [A-Za-z0-9_-]+")
              {:key value})))
    name))

(defn environment
  "Return the process environment as a string map."
  []
  #?(:clj (into {} (System/getenv))
     :glj (into {}
                (map (fn [entry]
                       (let [[name value] (str/split entry #"=" 2)]
                         [name value])))
                (seq (os.Environ)))))

(defn star-match?
  "Match VALUE against PATTERN, treating only * as a wildcard."
  [pattern value]
  (let [parts (str/split pattern #"\*" -1)
        count-parts (count parts)
        last-part (peek parts)]
    (and
     (or (str/ends-with? pattern "*")
         (str/ends-with? value last-part))
     (loop [index 0
            position 0]
       (if (= index count-parts)
         true
         (let [part (nth parts index)]
           (cond
             (str/blank? part)
             (recur (inc index) position)

             (zero? index)
             (and (str/starts-with? value part)
                  (recur (inc index) (count part)))

             :else
             (when-let [found (str/index-of value part position)]
               (recur (inc index)
                      (long (+ found (count part))))))))))))

(defn- normalize-data
  [data source]
  (when-not (map? data)
    (throw (ex-info (str "Alias-Data " source " must be a mapping")
                    {:source source :value data})))
  (reduce (fn [result [key value]]
            (let [name (valid-name key)]
              (when (contains? result name)
                (throw (ex-info
                        (str "Duplicate Alias-Data key " (pr-str name))
                        {:source source :key name})))
              (assoc result name value)))
          {}
          data))

(defn- select-environment
  [selector values]
  (let [selected
        (cond
          (or (nil? selector) (false? selector))
          {}

          (true? selector)
          values

          (string? selector)
          (if (str/includes? selector "*")
            (into {} (filter (fn [[name _]]
                               (star-match? selector name))) values)
            (if (contains? values selector)
              {selector (get values selector)}
              (throw (ex-info
                      (str "Alias-Data environment variable "
                           (pr-str selector) " is not set")
                      {:environment selector}))))

          (sequential? selector)
          (reduce (fn [result value]
                    (let [name (valid-name value)]
                      (if (contains? values name)
                        (assoc result name (get values name))
                        (throw (ex-info
                                (str "Alias-Data environment variable "
                                     (pr-str name) " is not set")
                                {:environment name})))))
                  {}
                  selector)

          :else
          (throw (ex-info
                  "Alias-Data env must be a boolean, string, or sequence"
                  {:env selector})))]
    (normalize-data selected "environment")))

(defn- validate-config
  [config]
  (let [unknown (seq (remove #{:data :file :env :stream} (keys config)))]
    (when unknown
      (throw (ex-info "Unknown Alias-Data configuration key"
                      {:keys unknown})))
    (when (and (contains? config :file)
               (not (and (string? (:file config))
                         (not (str/blank? (:file config))))))
      (throw (ex-info "Alias-Data file must be a non-empty string"
                      {:file (:file config)})))
    (when (and (contains? config :stream)
               (not (boolean? (:stream config))))
      (throw (ex-info "Alias-Data stream must be a boolean"
                      {:stream (:stream config)})))
    config))

(defn new-context
  "Create a context after snapshotting configured data sources."
  [config {:keys [load-file get-environment]}]
  (let [config (validate-config config)
        inline (normalize-data (or (:data config) {}) "data")
        file (if-let [path (:file config)]
               (normalize-data (load-file path) "file")
               {})
        env (if (contains? config :env)
              (select-environment
               (:env config)
               ((or get-environment environment)))
              {})
        current (atom {})
        prior (atom {})
        stream? (true? (:stream config))]
    {:begin-stream
     (fn [] (reset! prior {}) nil)
     :begin-document
     (fn [] (reset! current {}) nil)
     :define-anchor
     (fn [name value]
       (swap! current assoc name value)
       nil)
     :resolve-alias
     (fn [name]
       (loop [sources [@current inline file env @prior]]
         (if-let [source (first sources)]
           (if (contains? source name)
             [true (get source name)]
             (recur (next sources)))
           [false nil])))
     :end-document
     (fn []
       (when stream?
         (swap! prior merge @current))
       nil)
     :end-stream
     (fn [] nil)}))

(defn default-context
  []
  (new-context {} {:get-environment (constantly {})}))

(defn begin-stream [context] ((:begin-stream context)))
(defn begin-document [context] ((:begin-document context)))
(defn define-anchor [context name value]
  ((:define-anchor context) name value))
(defn resolve-alias [context name] ((:resolve-alias context) name))
(defn end-document [context] ((:end-document context)))
(defn end-stream [context] ((:end-stream context)))
