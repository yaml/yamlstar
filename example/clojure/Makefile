LIBYAMLSTAR-VERSION := 0.1.2

M := $(abspath ../../.cache/makes)
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/lein.mk
include $M/clojure.mk
include $M/libyamlstar.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := \
  .cpcache/ \
  target/ \


test: $(CLOJURE) $(LIBYAMLSTAR) deps.edn
	clj -M yaml-to-json.clj ../sample.yaml
	@echo
	@echo 'Success!'
	@echo 'YAMLStar loaded from Clojars and working correctly.'

deps.edn: project.clj $(LEIN)
	lein lein2deps --write-file deps.edn --print false
