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

;; Sentinel keyword used to retrieve the name from a name*-wrapped function.
;; Must be defined before func-name and stringify.
(def GET-NAME-SENTINEL :yamlstar/get-name)

;; Look up the trace name of a name*-wrapped function via sentinel call.
;; Safely handles non-name*-wrapped functions (which would arity-crash if called
;; with the sentinel) by catching any Go panic and returning nil.
(defn func-name [f]
  (when (fn? f)
    (try
      (let [result (f GET-NAME-SENTINEL)]
        (when (string? result) result))
      (catch go/any _ nil))))

;; Creates a function that embeds its name via a closure sentinel.
;; When called with GET-NAME-SENTINEL as the sole arg, returns the trace name.
;; When called with normal args, delegates to func.
;; Uses explicit multi-arity (not variadic+apply) for Glojure compatibility.
(defn name* [name func trace]
  (let [the-trace (or trace name)]
    (fn
      ([a]
       (if (= a GET-NAME-SENTINEL)
         the-trace
         (func a)))
      ([a b] (func a b))
      ([a b c] (func a b c)))))

;; String helpers
(defn stringify [o]
  (cond
    (= o "\ufeff") "\\uFEFF"
    (fn? o) (str "@" (or (func-name o) "fn"))
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

;; Timer (for performance measurement - stubbed, tracing not used in Glojure)
(defn timer
  ([] 0)
  ([start] 0.0))
