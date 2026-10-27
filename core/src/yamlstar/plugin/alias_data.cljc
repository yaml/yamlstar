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
  (let [unknown (seq (remove #{:data :file :env :stream :path} (keys config)))]
    (when unknown
      (throw (ex-info "Unknown Alias-Data configuration key"
                      {:keys unknown})))
    (when (and (contains? config :file)
               (not (and (string? (:file config))
                         (not (str/blank? (:file config))))))
      (throw (ex-info "Alias-Data file must be a non-empty string"
                      {:file (:file config)})))
    (doseq [key [:stream :path]]
      (when (and (contains? config key)
                 (not (boolean? (get config key))))
        (throw (ex-info (str "Alias-Data " (name key) " must be a boolean")
                        {key (get config key)}))))
    config))

(defn- path-error
  [alias segment reason]
  (throw (ex-info (str "Alias-Data path " (pr-str alias)
                       " segment " (pr-str segment) ": " reason)
                  {:alias alias :segment segment :reason reason})))

(defn- validate-path
  [alias parts]
  (doseq [part parts]
    (when (empty? part)
      (path-error alias part "empty path segment"))
    (when-not (or (re-matches #"[A-Za-z0-9_+\-]+" part)
                  (re-matches #"'[0-9]+'" part))
      (path-error alias part "invalid path segment")))
  (doseq [segment (next parts)]
    (when (and (re-matches #"[0-9]+" segment)
               (> (count segment) 1)
               (str/starts-with? segment "0"))
      (path-error alias segment "sequence index must not have leading zeros"))))

(defn- path-index
  [alias segment size]
  (when-not (re-matches #"[0-9]+" segment)
    (path-error alias segment "sequence index must contain decimal digits"))
  (when (zero? size)
    (path-error alias segment "sequence index out of range"))
  (let [limit (dec size)]
    (loop [chars (seq segment) index 0]
      (if-let [ch (first chars)]
        (let [digit (- (int ch) (int \0))]
          (when (or (> index (quot limit 10))
                    (and (= index (quot limit 10))
                         (> digit (mod limit 10))))
            (path-error alias segment "sequence index out of range"))
          (recur (next chars) (+ (* index 10) digit)))
        index))))

(defn- walk-path
  [alias value segments]
  (reduce (fn [value segment]
            (cond
              (map? value)
              (do
                (when (re-matches #"[0-9]+" segment)
                  (path-error alias segment
                              "numeric mapping keys must be single-quoted"))
                (let [key (if (str/starts-with? segment "'")
                            (subs segment 1 (dec (count segment)))
                            segment)]
                  (if (contains? value key)
                    (get value key)
                    (path-error alias segment "missing mapping key"))))

              (sequential? value)
              (nth value (path-index alias segment (count value)))

              :else
              (path-error alias segment "cannot traverse a scalar")))
          value segments))

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
        path? (true? (:path config))
        stream? (if (contains? config :stream)
                  (true? (:stream config))
                  path?)]
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
       (let [dotted? (and path? (str/includes? name "."))
             parts (if dotted? (str/split name #"\." -1) [name])
             base (first parts)
             segments (next parts)]
         (when dotted?
           (validate-path name parts))
         (loop [sources [@current inline file env @prior]]
           (if-let [source (first sources)]
             (if (contains? source base)
               [true (if segments
                       (walk-path name (get source base) segments)
                       (get source base))]
               (recur (next sources)))
             (if segments
               (path-error name base "unknown anchor")
               [false nil])))))
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

(defn disabled-context
  "Reject alias references without reading configured sources."
  []
  (assoc (default-context)
         :resolve-alias
         (fn [name]
           (throw (ex-info "Alias-Data aliases are disabled"
                           {:alias name})))))

(defn begin-stream [context] ((:begin-stream context)))
(defn begin-document [context] ((:begin-document context)))
(defn define-anchor [context name value]
  ((:define-anchor context) name value))
(defn resolve-alias [context name] ((:resolve-alias context) name))
(defn end-document [context] ((:end-document context)))
(defn end-stream [context] ((:end-stream context)))
