include ../common/init.mk
M := ../.cache/makes

RT ?= $(if $(YAMLSTAR_GLOJURE),glj,graal)
PARSER ?= $(if $(filter glj,$(RT)),go,ref)

VALID-RT := graal glj go
VALID-PARSER := ref snake go

ifeq (,$(filter $(RT),$(VALID-RT)))
$(error Unknown RT '$(RT)'. Expected one of: $(VALID-RT))
endif

ifeq (,$(filter $(PARSER),$(VALID-PARSER)))
$(error Unknown PARSER '$(PARSER)'. Expected one of: $(VALID-PARSER))
endif

PARSER-NAME := $(if $(filter ref,$(PARSER)),reference,$(if $(filter snake,$(PARSER)),snakeyaml,go-yaml))
YAMLSTAR_ENGINE ?= $(if $(filter graal,$(RT)),graalvm,glojure)
CLI-NAME ?= yaml-$(RT)-$(PARSER)
CLI-DEFAULT-REQUIRE := $(if $(filter snake,$(PARSER)),[yamlstar.plugin.parser.snakeyaml],$(if $(filter go,$(PARSER)),[yamlstar.plugin.parser.go-yaml]))

ifeq ($(RT),graal)
ifneq (,$(filter go,$(PARSER)))
$(error Unsupported parser/runtime pair: RT=graal PARSER=go)
endif
endif

ifeq ($(RT),glj)
ifneq (,$(filter snake,$(PARSER)))
$(error Unsupported parser/runtime pair: RT=glj PARSER=snake)
endif
endif

ifeq ($(RT),go)
ifneq (,$(filter snake go,$(PARSER)))
$(error Unsupported parser/runtime pair: RT=go PARSER=$(PARSER))
endif
endif

ifeq ($(YAMLSTAR_ENGINE),glojure)
include $M/gloat.mk
include $M/lein.mk
else ifeq ($(YAMLSTAR_ENGINE),graalvm)
include $M/graalvm.mk
include $M/lein.mk
else
$(error Unknown YAMLSTAR_ENGINE '$(YAMLSTAR_ENGINE)')
endif

include $(COMMON)/common.mk

ifeq ($(YAMLSTAR_ENGINE),graalvm)
include $(COMMON)/native.mk
endif

MAKES-CLEAN := \
  target \
  .lein-* \
  bin \
  cache \

ifeq ($(OS-NAME),windows)
CLI-BIN := bin/$(CLI-NAME).exe
else
CLI-BIN := bin/$(CLI-NAME)
endif
CLI-OUT := bin/$(CLI-NAME)
CLI-UBERJAR := target/cli-$(LIBYAMLSTAR-VERSION)-SNAPSHOT-standalone.jar
CLI-JAR := target/$(CLI-NAME)-$(LIBYAMLSTAR-VERSION)-SNAPSHOT-standalone.jar
CLI-DEFAULT-DIR := cache/cli-defaults/$(CLI-NAME)-$(PARSER-NAME)
CLI-DEFAULT-SRC := $(CLI-DEFAULT-DIR)/yamlstar/cli_default.clj

GLOAT-ENGINE := $(or $(GLOAT_ENGINE),$(if $(filter go,$(RT)),lgvm,glj))
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
ifeq ($(YAMLSTAR_ENGINE),glojure)
CLI-GLOAT-NAME ?= $(CLI-NAME)
else
CLI-GLOAT-NAME ?= yaml-gloat-$(GLOAT-ENGINE)
endif
CLI-BIN-GLOAT := bin/$(CLI-GLOAT-NAME)
GLOAT-CACHE := ../libyamlstar/cache/gloat-srcs
GLOAT-CORE := ../core/src/yamlstar
GLOAT-GO-YAML-SRC := $(GLOAT-CACHE)/plugin/parser/go_yaml.clj
GLOAT-CLI-SRCS := \
  $(GLOAT-CACHE)/yaml_parser/prelude.clj \
  $(GLOAT-CACHE)/yaml_parser/parser.clj \
  $(GLOAT-CACHE)/yaml_parser/receiver.clj \
  $(GLOAT-CACHE)/yaml_parser/grammar.clj \
  $(GLOAT-CACHE)/yaml_parser/core.clj \
  $(GLOAT-CORE)/plugin.clj \
  $(GLOAT-CORE)/options.clj \
  $(GLOAT-CORE)/plugin/parser.clj \
  $(if $(filter go,$(PARSER)),$(GLOAT-GO-YAML-SRC)) \
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
  $(CLI-DEFAULT-SRC) \
  $(GLOAT-CORE)/contract.clj \
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

_prepare-glj: _prepare-gloat-bin $(GLOAT-RUN-DEPS)
	env -u GOROOT $(GLOAT-GLOJURE-ENV) $(GLOAT-RUN) --glj-build

GLOAT-CLI-GLJ-DEP := _prepare-glj
endif

# Core dependency jar in local maven repository
CORE-JAR := $(MAVEN-REPOSITORY)/yamlstar/core/$(LIBYAMLSTAR-VERSION)-SNAPSHOT/core-$(LIBYAMLSTAR-VERSION)-SNAPSHOT.jar


test: $(CORE-JAR) $(LEIN) $(CLI-DEFAULT-SRC)
	$(LEIN) update-in :source-paths conj '"$(CLI-DEFAULT-DIR)"' -- test

ifeq ($(YAMLSTAR_ENGINE),glojure)
build: _build-gloat
else
build: $(CLI-BIN)
endif

jar: $(CLI-JAR)

install:

build-graalvm:
	$(MAKE) build RT=graal PARSER=ref CLI-NAME=yaml-graalvm

build-gloat:
	$(MAKE) _build-gloat \
	  RT=glj \
	  PARSER=go \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  CLI-GLOAT-NAME=$(CLI-GLOAT-NAME) \
	  $(if $(GLOAT-DIR),GLOAT-DIR=$(GLOAT-DIR)) \
	  $(if $(LET_GO_SRC),LET_GO_SRC=$(LET_GO_SRC)) \
	  $(if $(YAML-PARSER-DIR),YAML-PARSER-DIR=$(YAML-PARSER-DIR)) \
	  $(if $(TEST-WITH-GLOJURE),TEST-WITH-GLOJURE=$(TEST-WITH-GLOJURE))

build-gloat-glj:
	$(MAKE) build RT=glj PARSER=go CLI-NAME=yaml-gloat-glj

build-gloat-ref:
	$(MAKE) build RT=glj PARSER=ref CLI-NAME=yaml-gloat-ref

build-graalvm-ref:
	$(MAKE) build RT=graal PARSER=ref CLI-NAME=yaml-graalvm-ref

build-graalvm-snake:
	$(MAKE) build RT=graal PARSER=snake CLI-NAME=yaml-graalvm-snake

build-gloat-lgvm:
	$(MAKE) build RT=go PARSER=ref CLI-NAME=yaml-gloat-lgvm GLOAT_ENGINE=lgvm

build-gloat-lglvm:
	$(MAKE) build RT=go PARSER=ref CLI-NAME=yaml-gloat-lglvm GLOAT_ENGINE=lglvm

_build-gloat: $(CLI-BIN-GLOAT)

_prepare-gloat:
	$(MAKE) -C ../libyamlstar prepare-gloat \
	  YAMLSTAR_ENGINE=glojure \
	  GLOAT-DIR=$(GLOAT-DIR) \
	  $(if $(YAML-PARSER-DIR),YAML-PARSER-DIR=$(YAML-PARSER-DIR))

$(CLI-BIN-GLOAT): _prepare-gloat _prepare-gloat-bin \
  $(GLOAT-RUN-DEPS) \
  $(GLOAT-CLI-GLJ-DEP) \
  $(CLI-DEFAULT-SRC) \
  $(GLOAT-CORE)/plugin.clj \
  $(GLOAT-CORE)/options.clj \
  $(GLOAT-CORE)/plugin/parser.clj \
  $(if $(filter go,$(PARSER)),$(GLOAT-GO-YAML-SRC)) \
  $(GLOAT-CORE)/parser.clj \
  $(GLOAT-CORE)/composer.clj \
  $(GLOAT-CORE)/resolver.clj \
  $(GLOAT-CORE)/api.clj \
  $(GLOAT-CORE)/contract.clj \
  gloat/src/yamlstar/cli.clj
	mkdir -p $(dir $@)
	</dev/null env -u GOROOT $(GLOAT-GLOJURE-ENV) \
	  GLOAT_MODULE=github.com/yaml/yamlstar \
	  GLOAT_EXTRA_GO_DIR=$(abspath ../internal/goyamlparser) \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  $(if $(LET_GO_SRC),LET_GO_SRC=$(abspath $(LET_GO_SRC))) \
	  $(GLOAT-RUN) --force $(GLOAT-PLATFORM-OPT) \
	  $(GLOAT-CLI-SRCS) -o $@
	test -x $@
	@echo "Gloat ($(GLOAT-ENGINE)) CLI created at: $@"

$(CLI-DEFAULT-SRC):
	@mkdir -p $(dir $@)
	@if test -n '$(CLI-DEFAULT-REQUIRE)'; then \
	  printf '%s\n' \
	    '(ns yamlstar.cli-default' \
	    '  (:require $(CLI-DEFAULT-REQUIRE)))' \
	    '' \
	    '(def default-parser "$(PARSER-NAME)")' \
	    > $@; \
	else \
	  printf '%s\n' \
	    '(ns yamlstar.cli-default)' \
	    '' \
	    '(def default-parser "$(PARSER-NAME)")' \
	    > $@; \
	fi

ifeq ($(YAMLSTAR_ENGINE),graalvm)
$(CLI-JAR): $(CORE-JAR) $(LEIN) $(CLI-DEFAULT-SRC) src/yamlstar/cli.clj
	$(LEIN) update-in :source-paths conj '"$(CLI-DEFAULT-DIR)"' -- uberjar
	cp $(CLI-UBERJAR) $@
endif

$(CORE-JAR):
	$(MAKE) -C ../core install

ifeq ($(YAMLSTAR_ENGINE),graalvm)
$(CLI-BIN): $(CLI-JAR) $(GRAALVM)
	mkdir -p $(dir $@)
	$(NATIVE-IMAGE) \
	  $(NATIVE-OPTS) \
	  -jar $< \
	  -o $(CLI-OUT)
	test -f $@
	@echo "Native binary created at: $@"
endif
