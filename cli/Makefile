include ../common/init.mk
M := ../.cache/makes

ifdef YAMLSTAR_GLOJURE
include $M/gloat.mk
else
include $M/graalvm.mk
include $M/lein.mk
endif

include $(COMMON)/common.mk

ifndef YAMLSTAR_GLOJURE
include $(COMMON)/native.mk
endif

MAKES-CLEAN := \
  target \
  .lein-* \
  bin \

CLI-NAME ?= yaml
ifeq ($(OS-NAME),windows)
CLI-BIN := bin/$(CLI-NAME).exe
else
CLI-BIN := bin/$(CLI-NAME)
endif
CLI-OUT := bin/$(CLI-NAME)
CLI-JAR := target/cli-$(LIBYAMLSTAR-VERSION)-SNAPSHOT-standalone.jar

GLOAT-ENGINE := $(or $(GLOAT_ENGINE),glj)
GLOAT-PLATFORM-OPT := \
  $(if $(GLOAT_PLATFORM),--platform=$(GLOAT_PLATFORM))
GLOAT-RUN := $(GLOAT-BIN)/gloat
GLOAT-RUN-DEPS :=
ifeq ($(OS-NAME),windows)
GLOAT-RUN := /usr/bin/bash $(GLOAT-BIN)/gloat
GLOAT-BASH-SHIM := $(GLOAT-BIN)/bash
GLOAT-RUN-DEPS := $(GLOAT-BASH-SHIM)

$(GLOAT-BASH-SHIM): | $(GLOAT-DIR)
	@printf '%s\n' '#!/usr/bin/bash' \
	  'exec /usr/bin/bash "$$@"' > $@
	chmod +x $@
endif
CLI-GLOAT-NAME ?= yaml-gloat-$(GLOAT-ENGINE)
CLI-BIN-GLOAT := bin/$(CLI-GLOAT-NAME)
GLOAT-CACHE := ../libyamlstar/cache/gloat-srcs
GLOAT-CORE := ../core/src/yamlstar
GLOAT-CLI-SRCS := \
  $(GLOAT-CACHE)/yaml_parser/prelude.clj \
  $(GLOAT-CACHE)/yaml_parser/parser.clj \
  $(GLOAT-CACHE)/yaml_parser/receiver.clj \
  $(GLOAT-CACHE)/yaml_parser/grammar.clj \
  $(GLOAT-CACHE)/yaml_parser/core.clj \
  $(GLOAT-CORE)/parser.clj \
  $(GLOAT-CORE)/composer.clj \
  $(GLOAT-CORE)/resolver.clj \
  $(GLOAT-CACHE)/numbers.clj \
  $(GLOAT-CACHE)/constructor.clj \
  $(GLOAT-CACHE)/representer.clj \
  $(GLOAT-CACHE)/desolver.clj \
  $(GLOAT-CACHE)/serializer.clj \
  $(GLOAT-CACHE)/emitter.clj \
  $(GLOAT-CORE)/api.clj \
  gloat/src/yamlstar/cli.clj \

_prepare-gloat-bin:
	@if ! test -x "$(GLOAT)"; then \
	  case "$(GLOAT-DIR)" in \
	    "$(LOCAL-CACHE)"/*) \
	      rm -rf "$(GLOAT-DIR)"; \
	      $(MAKE) "$(GLOAT)"; \
	      ;; \
	    *) \
	      echo "Gloat executable not found: $(GLOAT)" >&2; \
	      exit 1; \
	      ;; \
	  esac; \
	fi
	@test -x "$(GLOAT)"

ifdef TEST-WITH-GLOJURE
GLOAT-GLOJURE-ENV := \
  GLOJURE_DIR=$(abspath $(TEST-WITH-GLOJURE)) \
  GOCACHE=$(GLOAT-DIR)/.cache/local/cache/go-build \
  GOMODCACHE=$(GLOAT-DIR)/.cache/local/go/pkg/mod

.PHONY: _prepare-glj
_prepare-glj: _prepare-gloat-bin $(GLOAT-RUN-DEPS)
	env -u GOROOT $(GLOAT-GLOJURE-ENV) $(GLOAT-RUN) --glj-build

GLOAT-CLI-GLJ-DEP := _prepare-glj
endif

# Core dependency jar in local maven repository
CORE-JAR := $(MAVEN-REPOSITORY)/yamlstar/core/$(LIBYAMLSTAR-VERSION)-SNAPSHOT/core-$(LIBYAMLSTAR-VERSION)-SNAPSHOT.jar


test: $(CORE-JAR) $(LEIN)
	lein test

ifdef YAMLSTAR_GLOJURE
build: _build-gloat
else
build: $(CLI-BIN)
endif

jar: $(CLI-JAR)

install:

build-graalvm:
	$(MAKE) build CLI-NAME=yaml-graalvm YAMLSTAR_GLOJURE=

build-gloat:
	YAMLSTAR_GLOJURE=1 $(MAKE) _build-gloat \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  CLI-GLOAT-NAME=$(CLI-GLOAT-NAME) \
	  $(if $(GLOAT-DIR),GLOAT-DIR=$(GLOAT-DIR)) \
	  $(if $(LET_GO_SRC),LET_GO_SRC=$(LET_GO_SRC)) \
	  $(if $(YAML-PARSER-DIR),YAML-PARSER-DIR=$(YAML-PARSER-DIR)) \
	  $(if $(TEST-WITH-GLOJURE),TEST-WITH-GLOJURE=$(TEST-WITH-GLOJURE))

build-gloat-glj:
	$(MAKE) build-gloat GLOAT_ENGINE=glj

build-gloat-lgvm:
	$(MAKE) build-gloat GLOAT_ENGINE=lgvm

build-gloat-lglvm:
	$(MAKE) build-gloat GLOAT_ENGINE=lglvm

_build-gloat: $(CLI-BIN-GLOAT)

.PHONY: _prepare-gloat
_prepare-gloat:
	YAMLSTAR_GLOJURE=1 $(MAKE) -C ../libyamlstar prepare-gloat \
	  GLOAT-DIR=$(GLOAT-DIR) \
	  $(if $(YAML-PARSER-DIR),YAML-PARSER-DIR=$(YAML-PARSER-DIR))

$(CLI-BIN-GLOAT): _prepare-gloat _prepare-gloat-bin \
  $(GLOAT-RUN-DEPS) \
  $(GLOAT-CLI-GLJ-DEP) \
  $(GLOAT-CORE)/parser.clj \
  $(GLOAT-CORE)/composer.clj \
  $(GLOAT-CORE)/resolver.clj \
  $(GLOAT-CORE)/api.clj \
  gloat/src/yamlstar/cli.clj
	mkdir -p $(dir $@)
	</dev/null env -u GOROOT $(GLOAT-GLOJURE-ENV) \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  $(if $(LET_GO_SRC),LET_GO_SRC=$(abspath $(LET_GO_SRC))) \
	  $(GLOAT-RUN) --force $(GLOAT-PLATFORM-OPT) \
	  $(GLOAT-CLI-SRCS) -o $@
	test -x $@
	@echo "Gloat ($(GLOAT-ENGINE)) CLI created at: $@"

$(CLI-JAR): $(CORE-JAR) $(LEIN)
	lein uberjar

$(CORE-JAR):
	$(MAKE) -C ../core install

$(CLI-BIN): $(CLI-JAR) $(GRAALVM)
	mkdir -p $(dir $@)
	$(NATIVE-IMAGE) \
	  $(NATIVE-OPTS) \
	  -jar $< \
	  -o $(CLI-OUT)
	test -f $@
	@echo "Native binary created at: $@"
