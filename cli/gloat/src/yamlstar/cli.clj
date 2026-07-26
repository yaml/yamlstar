(ns yamlstar.cli
  "Gloat-compatible YAMLStar command-line interface."
  (:require [clojure.string :as str]
            [yamlstar.api :as yaml]
            [ys.json :as json]))

(def usage-text
  "yaml - YAMLStar CLI - YAML Loader

Usage: yaml [options] [file]

Options:
  -f, --file FILE    Read YAML from FILE
  -e, --eval YAML    Read YAML from the command line
  -J, --json         Output pretty JSON
  -Y, --yaml         Output YAML
  -o, --output FILE  Write output to FILE
  -s, --stream       Load all YAML documents
  -v, --version      Print version
  -h, --help         Print help")

(defn die [message]
  (binding [*out* *err*]
    (println (str "Error: " message)))
  #?(:glj (os.Exit 1)
     :lg (System/exit 1)))

(defn parse-args [argv]
  (loop [args argv
         opts {}
         positional []]
    (if (empty? args)
      (assoc opts :arguments positional)
      (let [arg (first args)
            more (rest args)]
        (cond
          (or (= arg "-h") (= arg "--help"))
          (recur more (assoc opts :help true) positional)

          (or (= arg "-v") (= arg "--version"))
          (recur more (assoc opts :version true) positional)

          (or (= arg "-J") (= arg "--json"))
          (recur more (assoc opts :json true) positional)

          (or (= arg "-Y") (= arg "--yaml"))
          (recur more (assoc opts :yaml true) positional)

          (or (= arg "-s") (= arg "--stream"))
          (recur more (assoc opts :stream true) positional)

          (or (= arg "-f") (= arg "--file"))
          (if (empty? more)
            (die (str arg " requires a filename"))
            (recur (rest more) (assoc opts :file (first more)) positional))

          (or (= arg "-e") (= arg "--eval"))
          (if (empty? more)
            (die (str arg " requires a YAML string"))
            (recur (rest more) (assoc opts :eval (first more)) positional))

          (or (= arg "-o") (= arg "--output"))
          (if (empty? more)
            (die (str arg " requires a filename"))
            (recur (rest more) (assoc opts :output (first more)) positional))

          (= arg "--")
          (assoc opts :arguments (into positional more))

          (and (str/starts-with? arg "-") (not= arg "-"))
          (die (str "unknown option: " arg))

          :else
          (recur more opts (conj positional arg)))))))

(defn nil-keys->string [x]
  (cond
    (map? x) (apply array-map
                    (mapcat (fn [[k v]]
                              [(if (nil? k) "null" (nil-keys->string k))
                               (nil-keys->string v)])
                            x))
    (vector? x) (mapv nil-keys->string x)
    (sequential? x) (map nil-keys->string x)
    :else x))

(declare pretty-json)

(defn indent [level]
  (apply str (repeat (* level 2) " ")))

(defn pretty-json-map [value level]
  (if (empty? value)
    "{}"
    (let [rows (map (fn [[key item]]
                      (str (indent (inc level))
                           (json/dump (str key))
                           ": "
                           (pretty-json item (inc level))))
                    value)]
      (str "{\n" (str/join ",\n" rows) "\n" (indent level) "}"))))

(defn pretty-json-seq [value level]
  (if (empty? value)
    "[]"
    (let [rows (map (fn [item]
                      (str (indent (inc level))
                           (pretty-json item (inc level))))
                    value)]
      (str "[\n" (str/join ",\n" rows) "\n" (indent level) "]"))))

(defn pretty-json
  ([value] (pretty-json value 0))
  ([value level]
   (cond
     (map? value) (pretty-json-map value level)
     (sequential? value) (pretty-json-seq value level)
     :else (json/dump value))))

(defn read-input [opts]
  (cond
    (:eval opts) (:eval opts)
    (:file opts) (slurp (:file opts))
    (first (:arguments opts))
    (let [filename (first (:arguments opts))]
      (if (= filename "-") (slurp *in*) (slurp filename)))
    :else (slurp *in*)))

(defn format-output [data opts]
  (let [data (nil-keys->string data)]
    (cond
      (:yaml opts)
      (if (:stream opts) (yaml/dump-all data) (yaml/dump data))

      (:json opts)
      (pretty-json data)

      :else
      (json/dump data))))

(defn write-output [output opts]
  (if (:output opts)
    (spit (:output opts) output)
    (println output)))

(defn run [opts]
  (let [source (read-input opts)
        data (if (:stream opts)
               (yaml/load-all source)
               (yaml/load source))]
    (write-output (format-output data opts) opts)))

(defn -main [& argv]
  (let [opts (parse-args argv)]
    (cond
      (:help opts) (println usage-text)
      (:version opts) (println (str "yamlstar version " (yaml/version)))
      :else
      (try
        (run opts)
        (catch #?(:glj go/any :lg Exception) error
          (die (str error)))))))
