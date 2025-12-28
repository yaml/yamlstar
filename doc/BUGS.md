# Known Issues and Bugs

## Current Status

**Phase 1A implementation is code-complete but untested.**

### Bugs Fixed

1. ✅ **Composer recur mismatch** - Fixed inner loop trying to recur to outer loop
2. ✅ **Missing receiver callbacks** - Changed to use `make-receiver-with-callbacks`
3. ✅ **Implicit document handling** - Added support for documents without explicit markers

### Active Issues

1. ❌ **Test hang/timeout** - Tests hang when running, possible causes:
   - Infinite event stream from parser
   - Loop not terminating properly in composer
   - Event processing creating circular references

   **Next steps**:
   - Add debug logging to see what events are generated
   - Add iteration limit to composer loop as safety
   - Verify parser event stream is finite
   - Test each component individually (parser, composer, resolver)

2. ⚠️ **Namespace shadowing warnings** - Several warnings about shadowing core functions:
   - `yamlstar.resolver/resolve` shadows `clojure.core/resolve`
   - `yamlstar.core/load` shadows `clojure.core/load`
   - `yamlstar.parser.grammar/empty` shadows `clojure.core/empty`

   **Fix**: Rename these functions or exclude from core namespace

## Testing Environment

### Issue: Clojure tools not in PATH

The Makefile sets up Clojure/Leiningen but they're not readily available for manual testing.

**Workaround**: Use `make test` which handles tool installation

## Debugging Steps

When tools are available:

```bash
# 1. Test parser alone
cd core
lein repl
```

```clojure
(require '[yamlstar.parser :as parser])
(def events (parser/parse "hello"))
(count events)  ; Should be finite!
(take 20 events) ; Inspect events
```

```clojure
# 2. Test composer alone
(require '[yamlstar.composer :as composer])
(def node (composer/compose events))
node  ; Should return a node tree
```

```clojure
# 3. Test resolver alone
(require '[yamlstar.resolver :as resolver])
(resolver/resolve node)  ; Should return "hello"
```

```clojure
# 4. Test full pipeline
(require '[yamlstar.core :as yaml])
(yaml/load "hello")  ; Should return "hello"
```

## Code Review Needed

### Composer Event Loop

File: `core/src/yamlstar/composer.clj`

The main event processing loop needs review:
- Is `(seq events)` creating a lazy infinite sequence?
- Are all event types handled properly?
- Is the termination condition correct?

### Parser Event Generation

File: `core/src/yamlstar/parser.clj`

Questions:
- Does `@(:events receiver)` return a finite vector?
- Could the parser be generating events indefinitely?
- Is the parser properly terminating?

## Proposed Fixes

### 1. Add Safety Counter to Composer

```clojure
(defn compose-events [events]
  (loop [events (seq events)
         ...
         iteration 0
         max-iterations 100000]  ; Safety limit
    (when (> iteration max-iterations)
      (throw (ex-info "Composer iteration limit exceeded"
                      {:iteration iteration})))
    ...))
```

### 2. Add Debug Logging

```clojure
(defn compose-events [events]
  (let [event-vec (vec events)]  ; Force realization
    (println "Total events:" (count event-vec))
    (loop [events (seq event-vec)
           ...]
      ...)))
```

### 3. Verify Event Stream Finality

```clojure
(defn parse [yaml-str]
  (let [receiver (r/make-receiver-with-callbacks)
        parser (p/make-parser receiver)]
    (p/parse parser yaml-str)
    (let [events @(:events receiver)]
      (assert (vector? events) "Events should be a vector")
      (assert (< (count events) 100000) "Too many events")
      events)))
```

## Next Session TODO

1. Install Leiningen globally or use Makefile-provided version
2. Run REPL and test parser output
3. Add debug logging to see event stream
4. Fix infinite loop issue
5. Run full test suite
6. Fix any remaining bugs

## Environment Setup for Testing

```bash
# Option 1: Use Makefile (installs tools automatically)
make test

# Option 2: Install Leiningen manually
curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
chmod +x /usr/local/bin/lein
lein version

# Then
cd core
lein test
```
