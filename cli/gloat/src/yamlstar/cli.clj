(ns yamlstar.cli
  "Gloat-compatible YAMLStar command-line interface."
  (:require [clojure.string :as str]
            [yamlstar.api :as yaml]
            [yamlstar.cli-default :as cli-default]
            [yamlstar.composer :as composer]
            [yamlstar.contract :as contract]
            [yamlstar.constructor :as constructor]
            [yamlstar.parser :as parser]
            [yamlstar.resolver :as resolver]
            [ys.json :as json]))

(def usage-text
  "yaml - YAMLStar CLI - YAML Loader

Usage: yaml [options] [file]

Options:
  -f, --from STAGE   Force input stage: token, event, node, or yaml
      --file FILE    Read YAML from FILE
      --eval YAML    Read YAML from the command line
  -e, --event        Event output
  -E, --EVENT        Event output with metadata
  -n, --node         Node representation output
  -N, --NODE         Detailed node output
  -j, --json         Output compact JSON
  -J, --JSON         Output pretty JSON
  -y, --yaml         Normalized YAML output
  -Y, --YAML         YAML output preserving representation details
  -o, --output FILE  Write output to FILE
  -s, --stream       Load all YAML documents
  -d, --debug        Debug all stages
  -D, --debug-stage  Debug specific stage: parse, compose, resolve, construct
  -v, --version      Print version
  -h, --help         Print help")

(defmacro with-timing [stage-name & body]
  `(let [start# (time.Now)
         result# (do ~@body)
         elapsed# (/ (double (.Nanoseconds (time.Since start#))) 1000000.0)]
     (println (fmt.Sprintf "*** %-9s *** %.6f ms" ~stage-name elapsed#))
     (println)
     result#))

(defn die [message]
  #?(:glj
     (do
       (fmt.Fprintln os.Stderr (str "Error: " message))
       (os.Exit 1))
     :lg
     (do
       (binding [*out* *err*]
         (println (str "Error: " message)))
       (System/exit 1))))

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

          (or (= arg "-j") (= arg "--json"))
          (recur more (assoc opts :json true) positional)

          (or (= arg "-J") (= arg "--JSON"))
          (recur more (assoc opts :JSON true) positional)

          (or (= arg "-y") (= arg "--yaml"))
          (recur more (assoc opts :yaml true) positional)

          (or (= arg "-Y") (= arg "--YAML"))
          (recur more (assoc opts :YAML true) positional)

          (or (= arg "-e") (= arg "--event"))
          (recur more (assoc opts :event true) positional)

          (or (= arg "-E") (= arg "--EVENT"))
          (recur more (assoc opts :EVENT true) positional)

          (or (= arg "-n") (= arg "--node"))
          (recur more (assoc opts :node true) positional)

          (or (= arg "-N") (= arg "--NODE"))
          (recur more (assoc opts :NODE true) positional)

          (or (= arg "-s") (= arg "--stream"))
          (recur more (assoc opts :stream true) positional)

          (or (= arg "-f") (= arg "--from"))
          (if (empty? more)
            (die (str arg " requires a stage"))
            (recur (rest more) (assoc opts :from (first more)) positional))

          (or (= arg "-d") (= arg "--debug"))
          (recur more (assoc opts :debug true) positional)

          (or (= arg "-D") (= arg "--debug-stage"))
          (if (empty? more)
            (die (str arg " requires a stage"))
            (let [stage (first more)]
              (if (#{"parse" "compose" "resolve" "construct"} stage)
                (recur (rest more) (assoc opts :debug-stage stage) positional)
                (die (str arg " stage must be one of: parse, compose, resolve, construct")))))

          (= arg "--file")
          (if (empty? more)
            (die (str arg " requires a filename"))
            (recur (rest more) (assoc opts :file (first more)) positional))

          (= arg "--eval")
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

(defn do-debug-parse [yaml-str]
  (let [events (with-timing "parse"
                 (parser/parse yaml-str))]
    (doseq [event events]
      (prn event))))

(defn do-debug-compose [yaml-str]
  (let [events (parser/parse yaml-str)
        node (with-timing "compose"
               (composer/compose events))]
    (prn node)))

(defn do-debug-resolve [yaml-str]
  (let [events (parser/parse yaml-str)
        node (composer/compose events)
        resolved (with-timing "resolve"
                   (resolver/resolve node))]
    (prn resolved)))

(defn do-debug-construct [yaml-str]
  (let [events (parser/parse yaml-str)
        node (composer/compose events)
        resolved (resolver/resolve node)
        data (with-timing "construct"
               (constructor/construct resolved))]
    (prn data)))

(defn do-debug-all [yaml-str]
  (do-debug-parse yaml-str)
  (println)
  (do-debug-compose yaml-str)
  (println)
  (do-debug-resolve yaml-str)
  (println)
  (do-debug-construct yaml-str))

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

(defn read-stdin []
  #?(:glj
     (let [[content error] (io.ReadAll os.Stdin)]
       (if (nil? error)
         (fmt.Sprintf "%s" content)
         (throw error)))
     :lg
     (slurp *in*)))

(defn read-input [opts]
  (cond
    (:eval opts) (:eval opts)
    (:file opts) (slurp (:file opts))
    (first (:arguments opts))
    (let [filename (first (:arguments opts))]
      (if (= filename "-") (read-stdin) (slurp filename)))
    :else (read-stdin)))

(defn format-json-output [data opts]
  (let [data (nil-keys->string data)]
    (if (:JSON opts)
      (pretty-json data)
      (json/dump data))))

(defn write-output [output opts]
  (if (:output opts)
    (spit (:output opts) output)
    (println output)))

(defn output-stage [opts]
  (cond
    (or (:event opts) (:EVENT opts)) :event
    (or (:node opts) (:NODE opts)) :node
    (or (:yaml opts) (:YAML opts)) :yaml
    :else :json))

(defn token-follow-up []
  (throw (ex-info
          (str "token chaining is not supported by YAMLStar yet; "
               "yaml-parser token support is the explicit follow-up") {})))

(defn convert-input [input opts]
  (let [{:keys [stage value source]} (contract/read-contract input (:from opts))
        target (output-stage opts)]
    (when (= stage :token) (token-follow-up))
    (when (and (not= target :json) (not= stage :yaml))
      (contract/check-forward! stage target))
    (case stage
      :yaml
      (case target
        :event (yaml/dump (contract/event-contract (contract/yaml-events source)))
        :node (yaml/dump (contract/node-contract
                          (contract/events-nodes (contract/yaml-events source))
                          (:NODE opts)))
        :yaml (contract/yaml-output source (:YAML opts) (:stream opts))
        :json (format-json-output (contract/yaml-value source (:stream opts)) opts))

      :event
      (let [events (contract/contract-events value)]
        (case target
          :event (yaml/dump (contract/event-contract events))
          :node (yaml/dump (contract/node-contract
                            (contract/events-nodes events) (:NODE opts)))
          :yaml (contract/events-yaml events)
          :json (throw (ex-info "JSON output is only supported for YAML text input" {}))))

      :node
      (let [nodes (contract/contract-nodes value)]
        (case target
          :node (yaml/dump (contract/node-contract nodes (:NODE opts)))
          :yaml (contract/nodes-yaml nodes)
          :json (throw (ex-info "JSON output is only supported for YAML text input" {})))))))

(defn run [opts]
  (write-output (convert-input (read-input opts) opts) opts))

(defn -main [& argv]
  (parser/set-default-parser! cli-default/default-parser)
  (let [opts (parse-args argv)]
    (cond
      (:help opts) (println usage-text)
      (:version opts) (println (str "yamlstar version " (yaml/version)))
      :else
      (try
        (cond
          (:debug opts)
          (do-debug-all (read-input opts))

          (:debug-stage opts)
          (case (:debug-stage opts)
            "parse" (do-debug-parse (read-input opts))
            "compose" (do-debug-compose (read-input opts))
            "resolve" (do-debug-resolve (read-input opts))
            "construct" (do-debug-construct (read-input opts)))

          :else
          (run opts))
        (catch #?(:glj go/any :lg Exception) error
          (die (or (ex-message error) (str error))))))))
