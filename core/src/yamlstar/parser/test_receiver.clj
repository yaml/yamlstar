(ns yamlstar.parser.test-receiver
  (:require [clojure.string :as str]
            [yamlstar.parser.receiver :as r]))

(def event-map
  {"stream_start" "+STR"
   "stream_end" "-STR"
   "document_start" "+DOC"
   "document_end" "-DOC"
   "mapping_start" "+MAP"
   "mapping_end" "-MAP"
   "sequence_start" "+SEQ"
   "sequence_end" "-SEQ"
   "scalar" "=VAL"
   "alias" "=ALI"})

(def style-map
  {"plain" ":"
   "single" "'"
   "double" "\""
   "literal" "|"
   "folded" ">"})

(defn escape-value [value]
  (-> value
      (str/replace "\\" "\\\\")
      (str/replace "\u0000" "\\0")
      (str/replace "\u0007" "\\a")
      (str/replace "\u0008" "\\b")
      (str/replace "\t" "\\t")
      (str/replace "\n" "\\n")
      (str/replace "\u000b" "\\v")
      (str/replace "\u000c" "\\f")
      (str/replace "\r" "\\r")
      (str/replace "\u001b" "\\e")
      (str/replace "\u0085" "\\N")
      (str/replace "\u00a0" "\\_")
      (str/replace "\u2028" "\\L")
      (str/replace "\u2029" "\\P")
      ;; Trailing space becomes ␣ for test output (so str/trim doesn't remove it)
      (str/replace #" $" "\u2423")))

(defn format-event [e]
  (let [type (get event-map (:event e))
        parts [type]]
    (cond-> parts
      (and (= type "+DOC") (:explicit e)) (conj "---")
      (and (= type "-DOC") (:explicit e)) (conj "...")
      (and (= type "+MAP") (:flow e)) (conj "{}")
      (and (= type "+SEQ") (:flow e)) (conj "[]")
      (:anchor e) (conj (str "&" (:anchor e)))
      (:tag e) (conj (str "<" (:tag e) ">"))
      (:name e) (conj (str "*" (:name e)))
      (contains? e :value) (conj (str (get style-map (:style e))
                                      (escape-value (:value e))))
      true (#(str (str/join " " %) "\n")))))

(defn output [receiver]
  (let [events @(:events receiver)]
    (str/join "" (map format-event events))))

(defn make-test-receiver []
  (r/make-receiver-with-callbacks))
