# YAMLStar - Clojure

A pure YAML 1.2 loader for Clojure.

## Installation

### Leiningen/Boot

```clojure
[com.yaml/yamlstar "0.1.1"]
```

### Clojure CLI (deps.edn)

```clojure
com.yaml/yamlstar {:mvn/version "0.1.1"}
```

## Usage

```clojure
(require '[yamlstar.core :as yaml])

;; Load single document
(yaml/load "key: value")
;=> {"key" "value"}

;; Load multiple documents
(yaml/load-all "---\ndoc1\n---\ndoc2")
;=> ["doc1" "doc2"]
```

## Publishing to Clojars

To publish a release, add credentials to `~/.yamlstar-secrets.yaml`:

```yaml
clojars:
  user: <clojars-username>
  token: <clojars-deploy-token>
```

Then run: `make release-clojure` from the repository root.

## License

MIT License - See [License](../License) for details.
