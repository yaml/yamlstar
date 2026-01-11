# YAMLStar Development Guide

## Setup

### Install Clojure Tools

You'll need either Leiningen or Clojure CLI:

**Leiningen** (recommended for this project):
```bash
# Linux/macOS
curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
chmod +x /usr/local/bin/lein
lein version
```

**Or Clojure CLI**:
```bash
# Linux
curl -O https://download.clojure.org/install/linux-install-1.11.1.1435.sh
chmod +x linux-install-1.11.1.1435.sh
sudo ./linux-install-1.11.1.1435.sh
```

## Running Tests

```bash
cd core
lein test
```

Expected output:
```
Testing yamlstar.core-test

Ran 23 tests containing XX assertions.
0 failures, 0 errors.
```

## REPL Development

### Start a REPL
```bash
cd core
lein repl
```

### Basic Usage
```clojure
;; Load the namespace
(require '[yamlstar.core :as yaml])

;; Simple scalar
(yaml/load "hello")
;; => "hello"

;; Simple mapping
(yaml/load "key: value")
;; => {"key" "value"}

;; Nested structure
(yaml/load "
person:
  name: Alice
  age: 30
  hobbies:
    - reading
    - coding
")
;; => {"person" {"name" "Alice", "age" 30, "hobbies" ["reading" "coding"]}}

;; Multi-document
(yaml/load-all "---
doc1
---
doc2
")
;; => ["doc1" "doc2"]
```

### Testing Individual Components

```clojure
;; Test parser only (returns events)
(require '[yamlstar.parser :as parser])
(parser/parse "key: value")
;; => [{:event "stream_start"} {:event "document_start" ...} ...]

;; Test composer (returns nodes)
(require '[yamlstar.composer :as composer])
(def events (parser/parse "key: value"))
(composer/compose events)
;; => {:kind :mapping :value [[{:kind :scalar ...} {:kind :scalar ...}]]}

;; Test resolver (returns resolved nodes)
(require '[yamlstar.resolver :as resolver])
(def node (composer/compose events))
(def resolved (resolver/resolve node))
resolved
;; => {:kind :mapping :tag "!!map" ...}

;; Test constructor (returns data)
(require '[yamlstar.constructor :as constructor])
(constructor/construct resolved)
;; => {"key" "value"}
```

## Debugging

### Enable Parser Tracing

The parser has built-in debug support via environment variable:

```bash
DEBUG=1 lein repl
```

```clojure
(require '[yamlstar.parser :as parser])
(parser/parse "key: value")
;; Will print trace information
```

### Inspect Events

```clojure
(require '[yamlstar.parser :as parser])
(require '[clojure.pprint :refer [pprint]])

(def events (parser/parse "
key1: value1
key2:
  - item1
  - item2
"))

(pprint events)
;; Pretty-print all events
```

### Inspect Nodes

```clojure
(require '[yamlstar.parser :as parser])
(require '[yamlstar.composer :as composer])
(require '[clojure.pprint :refer [pprint]])

(def node (-> "key: value"
              parser/parse
              composer/compose))

(pprint node)
;; => {:kind :mapping,
;;     :value [[{:kind :scalar, :value "key", ...}
;;              {:kind :scalar, :value "value", ...}]],
;;     :anchor nil,
;;     :tag nil,
;;     :flow false}
```

## Common Issues and Fixes

### Issue: `ClassNotFoundException` for yamlstar namespaces

**Solution**: Make sure you're running from the `core/` directory:
```bash
cd core
lein repl
```

### Issue: Parser returns empty events

**Cause**: Input string might not have proper YAML format
**Solution**: Check for proper indentation and structure

### Issue: Composer stack overflow

**Cause**: Likely missing `mapping_end` or `sequence_end` events
**Solution**: Check parser event stream for balanced start/end pairs

### Issue: Resolver returns unexpected types

**Cause**: YAML 1.2 core schema type coercion rules
**Solution**: Use explicit tags (e.g., `!!str 123` to force string)

## Performance Testing

```clojure
(require '[yamlstar.core :as yaml])

(time
  (dotimes [_ 1000]
    (yaml/load "key: value")))
;; => "Elapsed time: XXX msecs"

;; Test large document
(def large-yaml
  (apply str
    (for [i (range 1000)]
      (str "key" i ": value" i "\n"))))

(time (yaml/load large-yaml))
```

## Next Steps

1. **Run tests**: `lein test` - Should all pass
2. **Fix failures**: Check composer/resolver logic
3. **Add edge cases**: Find YAML that breaks, add tests
4. **Benchmark**: Compare to SnakeYAML, other loaders
5. **Document**: Add examples to README

## Useful REPL Commands

```clojure
;; Reload code after changes
(require 'yamlstar.core :reload)

;; Run specific test
(require 'yamlstar.core-test)
(clojure.test/run-tests 'yamlstar.core-test)

;; Run single test function
(clojure.test/test-var #'yamlstar.core-test/test-load-simple-mapping)
```

## Architecture Reminder

```
YAML String
    ↓
[Parser] yamlstar.parser
    ↓ events: [{:event "mapping_start" ...} ...]
[Composer] yamlstar.composer
    ↓ nodes: {:kind :mapping :value [...]}
[Resolver] yamlstar.resolver
    ↓ resolved nodes: {:kind :mapping :tag "!!map" ...}
[Constructor] yamlstar.constructor
    ↓
Clojure Data: {"key" "value"}
```

## File Structure

```
core/
├── src/yamlstar/
│   ├── core.clj            - Public API (load, load-all)
│   ├── parser.clj          - Parser wrapper
│   ├── composer.clj        - Event → Node composer
│   ├── resolver.clj        - Node → Resolved node resolver
│   ├── constructor.clj     - Resolved node → Data constructor
│   └── parser/             - Pure Clojure YAML parser
│       ├── core.clj        - Parser entry point
│       ├── parser.clj      - PEG parsing engine
│       ├── receiver.clj    - Event receiver
│       ├── grammar.clj     - YAML 1.2 grammar (4247 lines!)
│       ├── prelude.clj     - Utilities
│       └── test_receiver.clj - Test formatting
└── test/yamlstar/
    └── core_test.clj       - 23 comprehensive tests
```
