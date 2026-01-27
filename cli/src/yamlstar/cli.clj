(ns yamlstar.cli
  "YAMLStar command-line interface"
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [yamlstar.core :as yaml]
            [yamlstar.parser :as parser]
            [yamlstar.composer :as composer]
            [yamlstar.resolver :as resolver]
            [yamlstar.constructor :as constructor])
  (:gen-class))

(def version "0.1.2-SNAPSHOT")

(defmacro with-timing [stage-name & body]
  `(let [start# (System/nanoTime)
         result# (do ~@body)
         elapsed# (/ (- (System/nanoTime) start#) 1000000.0)]
     (println (format "*** %-9s *** %.6f ms" ~stage-name elapsed#))
     (println)
     result#))

(defn reorder-node-keys
  "Reorder map keys to show :tag before :value for better readability"
  [node]
  (if (map? node)
    (let [reordered (cond-> {}
                      (:kind node) (assoc :kind (:kind node))
                      (:tag node) (assoc :tag (:tag node))
                      (:value node) (assoc :value (if (vector? (:value node))
                                                     (mapv reorder-node-keys (:value node))
                                                     (reorder-node-keys (:value node))))
                      (:style node) (assoc :style (:style node))
                      (:anchor node) (assoc :anchor (:anchor node))
                      (:flow node) (assoc :flow (:flow node)))]
      reordered)
    node))

(def cli-options
  [["-f" "--file FILE" "Input file (or use positional arg)"]
   ["-e" "--eval YAML" "Evaluate YAML string"]
   ["-J" "--json" "Output pretty JSON"]
   ["-Y" "--yaml" "Output YAML"]
   ["-o" "--output FILE" "Output file"]
   ["-s" "--stream" "Output all documents"]
   ["-d" "--debug" "Debug all stages"]
   ["-D" "--debug-stage STAGE" "Debug specific stage: parse, compose, resolve, construct"
    :validate [#{"parse" "compose" "resolve" "construct"} "Must be: parse, compose, resolve, construct"]]
   ["-S" "--stack-trace" "Show full stack traces"]
   ["-v" "--version" "Print version"]
   ["-h" "--help" "Print help"]])

(def usage-text
  "yaml - YAMLStar CLI - YAML Loader

Usage: yaml [options] [file]

Default: Read stdin, output compact JSON

Examples:
  yaml                       # stdin → compact JSON
  yaml config.yaml           # file → compact JSON
  yaml -J config.yaml        # file → pretty JSON
  yaml -Y config.yaml        # file → YAML
  cat f.yaml | yaml -J       # stdin → pretty JSON
  yaml -D parse config.yaml  # Debug parser stage

Options:")

(defn print-help [options-summary]
  (println usage-text)
  (println options-summary))

(defn print-version []
  (println (str "yamlstar version " version)))

;;; Debug functions

(defn do-debug-parse [yaml-str]
  (try
    (let [events (with-timing "parse"
                   (parser/parse yaml-str))]
      (doseq [e events]
        (prn e)))
    (catch Exception e
      (println "Parser error:" (.getMessage e))
      (when (.getCause e)
        (println "Cause:" (.getMessage (.getCause e)))))))

(defn do-debug-compose [yaml-str]
  (try
    (let [events (parser/parse yaml-str)
          node (with-timing "compose"
                 (composer/compose events))]
      (binding [pp/*print-right-margin* 50]
        (pp/pprint node)))
    (catch Exception e
      (println "Composer error:" (.getMessage e))
      (when (.getCause e)
        (println "Cause:" (.getMessage (.getCause e)))))))

(defn do-debug-resolve [yaml-str]
  (try
    (let [events (parser/parse yaml-str)
          node (composer/compose events)
          resolved (with-timing "resolve"
                     (resolver/resolve node))]
      (binding [pp/*print-right-margin* 50]
        (pp/pprint (reorder-node-keys resolved))))
    (catch Exception e
      (println "Resolver error:" (.getMessage e))
      (when (.getCause e)
        (println "Cause:" (.getMessage (.getCause e)))))))

(defn do-debug-construct [yaml-str]
  (try
    (let [events (parser/parse yaml-str)
          node (composer/compose events)
          resolved (resolver/resolve node)
          data (with-timing "construct"
                 (constructor/construct resolved))]
      (binding [pp/*print-right-margin* 50]
        (pp/pprint data)))
    (catch Exception e
      (println "Constructor error:" (.getMessage e))
      (when (.getCause e)
        (println "Cause:" (.getMessage (.getCause e)))))))

(defn do-debug-all [yaml-str]
  (do-debug-parse yaml-str)
  (println)
  (do-debug-compose yaml-str)
  (println)
  (do-debug-resolve yaml-str)
  (println)
  (do-debug-construct yaml-str))

;;; Input handling

(defn read-input [opts args]
  (cond
    ;; -e flag takes precedence
    (:eval opts)
    (:eval opts)

    ;; -f flag or positional argument
    (or (:file opts) (first args))
    (let [filename (or (:file opts) (first args))]
      (if (= filename "-")
        ;; "-" means stdin
        (slurp *in*)
        ;; Otherwise read from file
        (try
          (slurp filename)
          (catch Exception e
            (binding [*out* *err*]
              (println "Error reading file:" (.getMessage e)))
            (System/exit 1)))))

    ;; Default: read stdin
    :else
    (slurp *in*)))

;;; Output handling

(defn nil-keys->string
  "Replace nil keys with string 'null' for JSON serialization.
  JSON allows null values but not null keys."
  [x]
  (cond
    (map? x) (into {} (map (fn [[k v]]
                             [(if (nil? k) "null" (nil-keys->string k))
                              (nil-keys->string v)])
                           x))
    (vector? x) (mapv nil-keys->string x)
    (seq? x) (map nil-keys->string x)
    :else x))

(defn format-output [data opts]
  (let [preprocessed (nil-keys->string data)]
    (cond
      ;; YAML output - TODO: implement proper YAML serialization
      (:yaml opts)
      (str "# YAML output not yet implemented\n" (json/write-str preprocessed :indent true))

      ;; Pretty JSON
      (:json opts)
      (json/write-str preprocessed :indent true)

      ;; Default: compact JSON
      :else
      (json/write-str preprocessed))))

(defn write-output [output opts]
  (if-let [out-file (:output opts)]
    (spit out-file output)
    (println output)))

;;; Main logic

(defn do-load [yaml-str opts]
  (try
    (let [data (if (:stream opts)
                 (yaml/load-all yaml-str)
                 (yaml/load yaml-str))
          output (format-output data opts)]
      (write-output output opts)
      0)
    (catch Exception e
      (binding [*out* *err*]
        (println "Error:" (.getMessage e))
        (when (:stack-trace opts)
          (.printStackTrace e)))
      1)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      ;; Help
      (:help options)
      (do (print-help summary) 0)

      ;; Version
      (:version options)
      (do (print-version) 0)

      ;; Errors in parsing
      errors
      (do
        (binding [*out* *err*]
          (doseq [error errors]
            (println error))
          (println)
          (print-help summary))
        1)

      ;; Debug modes
      :else
      (let [yaml-str (read-input options arguments)]
        (cond
          ;; Debug all stages
          (:debug options)
          (do (do-debug-all yaml-str) 0)

          ;; Debug specific stage
          (:debug-stage options)
          (do
            (case (:debug-stage options)
              "parse"     (do-debug-parse yaml-str)
              "compose"   (do-debug-compose yaml-str)
              "resolve"   (do-debug-resolve yaml-str)
              "construct" (do-debug-construct yaml-str))
            0)

          ;; Normal load
          :else
          (do-load yaml-str options))))))
