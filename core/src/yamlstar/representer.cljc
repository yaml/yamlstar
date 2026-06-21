(ns yamlstar.representer
  "Represent native data as YAMLStar nodes."
  (:require [yamlstar.numbers :as numbers])
  (:refer-clojure :exclude [represent]))

(defn represent
  "Represent a JSON-compatible native value as a YAMLStar node tree."
  [value]
  (cond
    (nil? value)
    {:kind :scalar :tag "!!null" :value "null"}

    (true? value)
    {:kind :scalar :tag "!!bool" :value "true"}

    (false? value)
    {:kind :scalar :tag "!!bool" :value "false"}

    (number? value)
    (do
      (when (integer? value)
        (numbers/validate-safe-integer value))
      {:kind :scalar :tag (if (integer? value) "!!int" "!!float") :value (str value)})

    (string? value)
    {:kind :scalar :tag "!!str" :value value}

    (map? value)
    {:kind :mapping
     :tag "!!map"
     :value (mapv (fn [[k v]]
                    (when-not (string? k)
                      (throw (ex-info "YAMLStar dump only supports string map keys"
                                      {:key k :key-type (type k)})))
                    [(represent k) (represent v)])
                  value)}

    (sequential? value)
    {:kind :sequence
     :tag "!!seq"
     :value (mapv represent value)}

    :else
    (throw (ex-info "YAMLStar dump only supports JSON-compatible values"
                    {:value value :type (type value)}))))
