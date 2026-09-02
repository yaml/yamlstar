(ns yamlstar.representer
  "Represent native data as YAMLStar nodes."
  (:refer-clojure :exclude [represent]))

(defn represent
  "Represent a native value as a YAMLStar node tree."
  [value]
  (cond
    (nil? value)
    {:kind :scalar :tag "!!null" :value "null"}

    (true? value)
    {:kind :scalar :tag "!!bool" :value "true"}

    (false? value)
    {:kind :scalar :tag "!!bool" :value "false"}

    (number? value)
    {:kind :scalar
     :tag (if (integer? value) "!!int" "!!float")
     :value (str value)}

    (keyword? value)
    {:kind :scalar :tag "!!str" :value (subs (str value) 1)}

    (string? value)
    {:kind :scalar :tag "!!str" :value value}

    (map? value)
    {:kind :mapping
     :tag "!!map"
     :value (mapv (fn [[k v]]
                    [(represent k) (represent v)])
                  value)}

    (coll? value)
    {:kind :sequence
     :tag "!!seq"
     :value (mapv represent value)}

    :else
    (throw (ex-info "YAMLStar dump does not support this value"
                    {:value value :type (type value)}))))
