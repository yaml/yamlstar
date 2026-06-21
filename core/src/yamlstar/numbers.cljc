(ns yamlstar.numbers
  "Numeric policy shared by YAML load and dump paths.")

(def safe-integer-min
  -9007199254740991)

(def safe-integer-max
  9007199254740991)

(defn integer-range-message [value]
  (str "YAML integer out of supported range: " value
       ". Supported range is " safe-integer-min
       " to " safe-integer-max
       ". Quote the value to load or dump it as a string."))

(defn integer-range-error [value]
  (ex-info (integer-range-message value)
           {:value value
            :min safe-integer-min
            :max safe-integer-max}))

(defn safe-integer? [n]
  (and (<= safe-integer-min n)
       (<= n safe-integer-max)))

(defn validate-safe-integer [n]
  (when-not (safe-integer? n)
    (throw (integer-range-error n)))
  n)

(defn parse-safe-integer [value]
  #?(:clj
     (let [n (bigint value)]
       (validate-safe-integer n)
       (long n))
     :glj
     (let [[n err] (strconv.ParseInt value 10 64)]
       (when err
         (throw (integer-range-error value)))
       (validate-safe-integer n)
       n)))
