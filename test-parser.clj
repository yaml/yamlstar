#!/usr/bin/env bb
;; Quick test script to validate parser integration

(require '[yamlstar.parser :as parser])

(println "Testing YAMLStar parser integration...")
(println)

(try
  (let [yaml "key: value"
        events (parser/parse yaml)]
    (println "✓ Parser loaded successfully!")
    (println "✓ Parsed simple YAML:")
    (println "  Input:" yaml)
    (println "  Events:" (count events))
    (println)
    (doseq [event (take 10 events)]
      (println "  " event)))

  (catch Exception e
    (println "✗ Error:" (.getMessage e))
    (println)
    (println "Stack trace:")
    (.printStackTrace e)))
