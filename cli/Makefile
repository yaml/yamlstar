include ../common/init.mk
include ../common/backend.mk
M := ../.cache/makes

ifdef YAMLSTAR-BACKEND-IS-GLOAT
include $M/gloat.mk
include $M/go.mk
include $M/lein.mk
else
include $M/graalvm.mk
include $M/lein.mk
endif

include $(COMMON)/common.mk

ifndef YAMLSTAR-BACKEND-IS-GLOAT
include $(COMMON)/native.mk
endif

MAKES-CLEAN := \
  target \
  .lein-* \
  cache \
  bin \

CLI-NAME ?= yaml
ifeq ($(OS-NAME),windows)
CLI-BIN := bin/$(CLI-NAME).exe
CLI-BIN-JVM := bin/yamlstar-jvm.exe
CLI-BIN-GLJ := bin/yamlstar-glj.exe
CLI-BIN-CPP := bin/yamlstar-cpp.exe
else
CLI-BIN := bin/$(CLI-NAME)
CLI-BIN-JVM := bin/yamlstar-jvm
CLI-BIN-GLJ := bin/yamlstar-glj
CLI-BIN-CPP := bin/yamlstar-cpp
endif
CLI-OUT := bin/$(CLI-NAME)
CLI-OUT-JVM := bin/yamlstar-jvm
CLI-JAR := target/cli-$(LIBYAMLSTAR-VERSION)-SNAPSHOT-standalone.jar
ifeq ($(YAMLSTAR-BACKEND),jvm)
SELECTED-CLI-BIN := $(CLI-BIN-JVM)
endif
ifeq ($(YAMLSTAR-BACKEND),glj)
SELECTED-CLI-BIN := $(CLI-BIN-GLJ)
endif
ifeq ($(YAMLSTAR-BACKEND),cpp)
SELECTED-CLI-BIN := $(CLI-BIN-CPP)
endif

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
CLI-GLOAT-BUILD-DIR := cache/gloat-build
CLI-GO-ENV := \
  GOPATH=$(ROOT)/.cache/local/go \
  GOCACHE=$(ROOT)/.cache/local/cache/go-build \
  GOMODCACHE=$(ROOT)/.cache/local/go/pkg/mod
GLOAT-CACHE := ../libyamlstar/cache/gloat-srcs
GLOAT-CORE := ../core/src/yamlstar
RAPIDYAML-DIR := ../rapidyaml
RAPIDYAML-STAMP := $(RAPIDYAML-DIR)/.yamlstar-v0.16.0
RYMLSHIM-MODULE := yamlstar.local/rymlshim
RYMLSHIM-DIR := $(abspath ../libyamlstar/gloat/rymlshim)
RYMLSHIM-SRCS := \
  ../libyamlstar/gloat/rymlshim/go.mod \
  ../libyamlstar/gloat/rymlshim/go.sum \
  ../libyamlstar/gloat/rymlshim/rymlshim.go \
  ../libyamlstar/gloat/rymlshim/rymlshim_test.go \
  ../libyamlstar/gloat/rymlshim/rymlshim.h \
  ../libyamlstar/gloat/rymlshim/rymlshim.cpp
GLOAT-TEMPLATE-DEPS := _gloat-template-base
ifdef YAMLSTAR-BACKEND-IS-CPP
GLOAT-TEMPLATE-DEPS += $(RYMLSHIM-SRCS) $(RAPIDYAML-STAMP) \
  _gloat-cpp-template
endif
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


test: validate-backend select-backend $(CORE-JAR) $(LEIN)
	lein test

build: validate-backend build-$(YAMLSTAR-BACKEND) select-backend

jar: $(CLI-JAR)

install:

build-graalvm:
	YAMLSTAR_JVM=1 $(MAKE) build-jvm select-backend

build-gloat:
	YAMLSTAR_GLJ=1 $(MAKE) _build-gloat \
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

build-jvm: $(CLI-BIN-JVM)

build-glj:
	YAMLSTAR_GLJ=1 $(MAKE) _build-gloat CLI-GLOAT-NAME=yamlstar-glj

build-cpp:
	YAMLSTAR_CPP=1 $(MAKE) _build-gloat CLI-GLOAT-NAME=yamlstar-cpp

_build-gloat: $(CLI-BIN-GLOAT)

.PHONY: _prepare-gloat
_prepare-gloat:
	YAMLSTAR_GLJ=1 $(MAKE) -C ../libyamlstar prepare-gloat \
	  GLOAT-DIR=$(GLOAT-DIR) \
	  $(if $(YAML-PARSER-DIR),YAML-PARSER-DIR=$(YAML-PARSER-DIR))

$(CLI-BIN-GLOAT): _prepare-gloat _prepare-gloat-bin \
  $(GLOAT-TEMPLATE-DEPS) \
  $(GLOAT-RUN-DEPS) \
  $(GLOAT-CLI-GLJ-DEP) \
  $(GLOAT-CORE)/parser.clj \
  $(GLOAT-CORE)/composer.clj \
  $(GLOAT-CORE)/resolver.clj \
  $(GLOAT-CORE)/api.clj \
  gloat/src/yamlstar/cli.clj
	mkdir -p $(dir $@)
ifdef YAMLSTAR-BACKEND-IS-CPP
	rm -rf $(CLI-GLOAT-BUILD-DIR)
	</dev/null env -u GOROOT $(GLOAT-GLOJURE-ENV) \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  $(if $(LET_GO_SRC),LET_GO_SRC=$(abspath $(LET_GO_SRC))) \
	  $(GLOAT-RUN) --force $(GLOAT-PLATFORM-OPT) \
	  $(GLOAT-CLI-SRCS) -o $(CLI-GLOAT-BUILD-DIR)/
	cd $(CLI-GLOAT-BUILD-DIR) && env -u GOROOT CGO_ENABLED=1 \
	  $(CLI-GO-ENV) $(GO) mod tidy
	cd $(CLI-GLOAT-BUILD-DIR) && env -u GOROOT CGO_ENABLED=1 \
	  $(CLI-GO-ENV) $(GO) build -trimpath -ldflags "-s -w" \
	  -tags glj_aot_runtime,glj_no_goimports -o ../../$@ main.go
else
	</dev/null env -u GOROOT $(GLOAT-GLOJURE-ENV) \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  $(if $(LET_GO_SRC),LET_GO_SRC=$(abspath $(LET_GO_SRC))) \
	  $(GLOAT-RUN) --force $(GLOAT-PLATFORM-OPT) \
	  $(GLOAT-CLI-SRCS) -o $@
endif
	test -x $@
	@echo "Gloat ($(GLOAT-ENGINE)) CLI created at: $@"

$(CLI-JAR): $(CORE-JAR) $(LEIN)
	lein uberjar

$(CORE-JAR):
	$(MAKE) -C ../core install

$(CLI-BIN-JVM): $(CLI-JAR) $(GRAALVM)
	mkdir -p $(dir $@)
	$(NATIVE-IMAGE) \
	  $(NATIVE-OPTS) \
	  -jar $< \
	  -o $(CLI-OUT-JVM)
	test -f $@
	@echo "Native binary created at: $@"

$(RAPIDYAML-STAMP):
	$(MAKE) -C .. rapidyaml

_gloat-template-base: | $(GLOAT-DIR)
	@perl -0pi -e 's/\n\tyamlstar_ryml "$(subst /,\/,$(RYMLSHIM-MODULE))"//g; s/\n\tyamlstar_ryml\.Install\(\)\n//g' \
	  '$(GLOAT-DIR)/template/main.go'
	@perl -0pi -e 's|replace $(subst /,\/,$(RYMLSHIM-MODULE)) => [^\n]*||g; s/\nrequire $(subst /,\/,$(RYMLSHIM-MODULE)) v0\.0\.0\n//g; s/\n\n+/\n/g' \
	  '$(GLOAT-DIR)/template/go.mod'

_gloat-cpp-template: _gloat-template-base
	@if ! grep -q '$(RYMLSHIM-MODULE)' '$(GLOAT-DIR)/template/main.go'; then \
	  perl -0pi -e 's/import \(\n/import (\n\tyamlstar_ryml "$(subst /,\/,$(RYMLSHIM-MODULE))"\n/' \
	    '$(GLOAT-DIR)/template/main.go'; \
	  perl -0pi -e 's/(\t\/\/ Set \*ns\* to the user'"'"'s namespace)/\tyamlstar_ryml.Install()\n\n$$1/' \
	    '$(GLOAT-DIR)/template/main.go'; \
	fi
	@printf '\nrequire $(RYMLSHIM-MODULE) v0.0.0\n' \
	  >> '$(GLOAT-DIR)/template/go.mod'
	@printf 'replace $(RYMLSHIM-MODULE) => $(RYMLSHIM-DIR)\n' \
	  >> '$(GLOAT-DIR)/template/go.mod'

select-backend: validate-backend $(SELECTED-CLI-BIN)
	mkdir -p $(dir $(CLI-BIN))
ifeq ($(OS-NAME),windows)
	cp -f $(SELECTED-CLI-BIN) $(CLI-BIN)
else
	ln -sf $(notdir $(SELECTED-CLI-BIN)) $(CLI-BIN)
endif
