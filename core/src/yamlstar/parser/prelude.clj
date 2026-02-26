(ns yamlstar.parser.prelude
  (:require [clojure.string :as str]))

;; Environment access - returns nil (falsy) when not set
(defn env [key]
  (not-empty (os.Getenv key)))

;; Type checking predicates
(defn is-null? [x] (nil? x))
(defn is-boolean? [x] (or (true? x) (false? x)))
(defn is-number? [x] (number? x))
(defn is-string? [x] (string? x))
(defn is-function? [x] (fn? x))
(defn is-array? [x] (or (vector? x) (seq? x)))
(defn is-object? [x] (map? x))

(defn typeof* [value]
  (cond
    (nil? value) "null"
    (or (true? value) (false? value)) "boolean"
    (number? value) "number"
    (string? value) "string"
    (keyword? value) "string"  ;; Keywords treated as strings
    (symbol? value) "string"   ;; Symbols treated as strings
    (fn? value) "function"
    (or (vector? value) (seq? value)) "array"
    (map? value) "object"
    :else (throw (ex-info "Unknown type" {:value value}))))

;; String helpers
(defn stringify [o]
  (cond
    (= o "\ufeff") "\\uFEFF"
    (fn? o) (str "@" (or (:trace (meta o)) "fn"))
    (map? o) (pr-str (keys o))
    (or (vector? o) (seq? o)) (str "[" (str/join "," (map stringify o)) "]")
    (string? o) o
    :else (pr-str o)))

(defn hex-char [chr]
  (fmt.Sprintf "%x" (int (first chr))))

;; Debug and error functions
(defn warn [msg]
  (fmt.Fprintln os.Stderr msg))

(defn die [msg]
  (throw (ex-info msg {})))

(defn die* [msg]
  (die msg))

(defn debug [msg]
  (warn (str ">>> " msg)))

(defn debug-rule [name & args]
  (when (env "DEBUG")
    (let [args-str (str/join "," (map stringify args))]
      (debug (str name "(" args-str ")")))))

(defn FAIL [& args]
  (doseq [o args]
    (prn o))
  (die (str "FAIL '" (or (first args) "???") "'")))

;; Utility functions
(defn name* [name func trace]
  (with-meta func {:trace (or trace name)}))

;; Timer (for performance measurement - stubbed, tracing not used in Glojure)
(defn timer
  ([] 0)
  ([start] 0.0))
