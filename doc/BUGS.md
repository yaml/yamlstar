# Known Issues and Bugs

## Current Status

**Phase 1 is complete and tested.** All major bugs have been resolved and 23
tests are passing.

### Bugs Fixed

1. ✅ **Composer recur mismatch** - Fixed inner loop trying to recur to outer
   loop
2. ✅ **Missing receiver callbacks** - Changed to use
   `make-receiver-with-callbacks`
3. ✅ **Implicit document handling** - Added support for documents without
   explicit markers
4. ✅ **Test hang/timeout** - Resolved by fixing composer and parser event
   stream handling

### Active Issues

1. ⚠️ **Namespace shadowing warnings** - Several warnings about shadowing core
   functions:
   - `yamlstar.resolver/resolve` shadows `clojure.core/resolve`
   - `yamlstar.core/load` shadows `clojure.core/load`
   - `yamlstar.parser.grammar/empty` shadows `clojure.core/empty`

   **Note**: These are intentional and use `:refer-clojure :exclude` to avoid
   conflicts.
   No action needed unless warnings become problematic.

## Testing Environment

The Makefile-based build system automatically installs and manages all required
tools:
- Leiningen (for Clojure builds)
- GraalVM (for native-image shared library)
- Language-specific tools (for each binding)

Simply run `make test` in any directory to run tests.
The build system handles everything automatically.

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

## Phase 1 Complete - Next Phase TODO

Phase 1 is complete.
Next steps for Phase 2 (Glojure Migration):

1. Test core with Glojure interpreter
2. Port code to work with Glojure
3. Use Glojure AOT compilation to Go
4. Create Go shared library
5. Eliminate GraalVM dependency
6. Test cross-platform support

## Environment Setup for Testing

```bash
# Run tests (auto-installs all tools)
cd core
make test

# Run language binding tests
cd python  # or nodejs, rust, etc.
make test

# Run all tests
make test  # from root directory
```
