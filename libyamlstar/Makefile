include ../common/init.mk
include ../common/backend.mk

# Load platform tools before common.mk (which includes shell.mk last)
# Avoids graalvm.mk/java.mk conflict when both platforms are present
ifdef YAMLSTAR-BACKEND-IS-GLOAT
include $M/gloat.mk
else
include $M/graalvm.mk
include $M/clojure.mk
include $M/lein.mk
endif

include ../common/common.mk

ifndef YAMLSTAR-BACKEND-IS-GLOAT
include ../common/native.mk
endif

MAKES-CLEAN := \
  graalvm/target \
  graalvm/.lein-* \
  cache/yaml-parser-* \
  cache/gloat-srcs \
  lib/ \

MAKES-REALCLEAN := \
  lib \

# Library paths and names
SO ?= so
LIB-DIR := lib
LIB-NAME := libyamlstar
LIB-NAME-GLOAT ?= libyamlstar
LIB-NAME-JVM := libyamlstar-jvm
LIB-NAME-GLJ := libyamlstar-glj
LIB-NAME-CPP := libyamlstar-cpp
LIB-SO-JVM := $(LIB-DIR)/$(LIB-NAME-JVM).$(SO)
LIB-SO-GLJ := $(LIB-DIR)/$(LIB-NAME-GLJ).$(SO)
LIB-SO-CPP := $(LIB-DIR)/$(LIB-NAME-CPP).$(SO)
LIB-SO := $(LIB-DIR)/$(LIB-NAME).$(SO)
LIB-SO-VERSION := $(LIB-SO).$(VERSION)
LIB-HEADER := $(LIB-DIR)/$(LIB-NAME).h
LIB-HEADER-JVM := $(LIB-DIR)/$(LIB-NAME-JVM).h
LIB-HEADER-GLJ := $(LIB-DIR)/$(LIB-NAME-GLJ).h
LIB-HEADER-CPP := $(LIB-DIR)/$(LIB-NAME-CPP).h
ifeq ($(YAMLSTAR-BACKEND),jvm)
SELECTED-LIB-SO := $(LIB-SO-JVM)
SELECTED-LIB-HEADER := $(LIB-HEADER-JVM)
endif
ifeq ($(YAMLSTAR-BACKEND),glj)
SELECTED-LIB-SO := $(LIB-SO-GLJ)
SELECTED-LIB-HEADER := $(LIB-HEADER-GLJ)
endif
ifeq ($(YAMLSTAR-BACKEND),cpp)
SELECTED-LIB-SO := $(LIB-SO-CPP)
SELECTED-LIB-HEADER := $(LIB-HEADER-CPP)
endif
GLOAT-PLATFORM-OPT := \
  $(if $(GLOAT_PLATFORM),--platform=$(GLOAT_PLATFORM))
GLOAT-WINDOWS-TARGET := $(filter windows/%,$(GLOAT_PLATFORM))
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
GLOAT-LIB-HEADER := gloat/include/libyamlstar.h
GLOAT-ISOLATE-HEADER := gloat/include/graal_isolate.h
GO-GENERATED-WORK := $(abspath ../.cache/go-generate)
GO-GENERATED-DIR := $(abspath ../internal/glojure)
GO-GENERATED-MODULE := github.com/yaml/yamlstar/internal/glojure

CORE-YAMLSTAR := ../core/src/yamlstar
RAPIDYAML-DIR := ../rapidyaml
RAPIDYAML-STAMP := $(RAPIDYAML-DIR)/.yamlstar-v0.16.0
RYMLSHIM-MODULE := yamlstar.local/rymlshim
RYMLSHIM-DIR := $(abspath gloat/rymlshim)
RYMLSHIM-SRCS := \
  gloat/rymlshim/go.mod \
  gloat/rymlshim/go.sum \
  gloat/rymlshim/rymlshim.go \
  gloat/rymlshim/rymlshim_test.go \
  gloat/rymlshim/rymlshim.h \
  gloat/rymlshim/rymlshim.cpp \

# Gloat compiles Clojure sources directly, so use the sources shipped in the
# exact yaml-parser artifact consumed by the JVM build.
YAML-PARSER-VERSION := 0.2.3
YAML-PARSER-JAR := cache/yaml-parser-$(YAML-PARSER-VERSION).jar
YAML-PARSER-URL := https://repo.clojars.org/org/yamlstar/yaml-parser/$(YAML-PARSER-VERSION)/yaml-parser-$(YAML-PARSER-VERSION).jar
YAML-PARSER-GLOAT-PREPARE := gloat/prepare-yaml-parser

ifdef YAML-PARSER-DIR
YAML-PARSER-SRC-DIR := $(abspath $(YAML-PARSER-DIR))/src
YAML-PARSER-SRC-STAMP := $(YAML-PARSER-SRC-DIR)/yaml_parser/parser.cljc
YAML-PARSER-SOURCE-FILES := \
  $(wildcard $(YAML-PARSER-SRC-DIR)/yaml_parser/*.clj) \
  $(wildcard $(YAML-PARSER-SRC-DIR)/yaml_parser/*.cljc) \

else
YAML-PARSER-SRC-DIR := cache/yaml-parser-$(YAML-PARSER-VERSION)
YAML-PARSER-SRC-STAMP := $(YAML-PARSER-SRC-DIR)/.extracted
endif

# .cljc source files that need .clj symlinks for gloat
GLOAT-LINK-DIR := cache/gloat-srcs

$(YAML-PARSER-JAR):
	@mkdir -p $(dir $@)
	curl --fail --location --silent --show-error '$(YAML-PARSER-URL)' -o '$@.tmp'
	mv '$@.tmp' '$@'

ifndef YAML-PARSER-DIR
$(YAML-PARSER-SRC-STAMP): $(YAML-PARSER-JAR) $(YAML-PARSER-GLOAT-PREPARE)
	@mkdir -p $(YAML-PARSER-SRC-DIR)
	unzip -oq $< 'yaml_parser/*.clj' 'yaml_parser/*.cljc' -d $(YAML-PARSER-SRC-DIR)
	perl $(YAML-PARSER-GLOAT-PREPARE) $(YAML-PARSER-SRC-DIR)
	touch $@
endif

ifdef YAML-PARSER-DIR
$(GLOAT-LINK-DIR)/yaml_parser/%.clj: \
  $(YAML-PARSER-SRC-DIR)/yaml_parser/%.cljc
	@mkdir -p $(dir $@)
	ln -sf $< $@
else
$(GLOAT-LINK-DIR)/yaml_parser/%.clj: $(YAML-PARSER-SRC-STAMP)
	@mkdir -p $(dir $@)
	ln -sf $(abspath $(YAML-PARSER-SRC-DIR)/yaml_parser/$*.cljc) $@
endif

$(GLOAT-LINK-DIR)/yaml_parser/core.clj: $(YAML-PARSER-SRC-STAMP)
	@mkdir -p $(dir $@)
	ln -sf $(abspath $(YAML-PARSER-SRC-DIR)/yaml_parser/core.clj) $@

$(GLOAT-LINK-DIR)/constructor.clj: $(CORE-YAMLSTAR)/constructor.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/numbers.clj: $(CORE-YAMLSTAR)/numbers.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/representer.clj: $(CORE-YAMLSTAR)/representer.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/desolver.clj: $(CORE-YAMLSTAR)/desolver.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/serializer.clj: $(CORE-YAMLSTAR)/serializer.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/emitter.clj: $(CORE-YAMLSTAR)/emitter.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

GLOAT-LINKS := \
  $(GLOAT-LINK-DIR)/yaml_parser/prelude.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/parser.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/receiver.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/grammar.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/core.clj \
  $(GLOAT-LINK-DIR)/constructor.clj \
  $(GLOAT-LINK-DIR)/numbers.clj \
  $(GLOAT-LINK-DIR)/representer.clj \
  $(GLOAT-LINK-DIR)/desolver.clj \
  $(GLOAT-LINK-DIR)/serializer.clj \
  $(GLOAT-LINK-DIR)/emitter.clj \

# Clojure source files compiled by gloat (order matters for namespaces)
GLOAT-SRCS := \
  $(GLOAT-LINK-DIR)/yaml_parser/prelude.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/parser.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/receiver.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/grammar.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/core.clj \
  $(CORE-YAMLSTAR)/parser.clj \
  $(CORE-YAMLSTAR)/composer.clj \
  $(CORE-YAMLSTAR)/resolver.clj \
  $(GLOAT-LINK-DIR)/numbers.clj \
  $(GLOAT-LINK-DIR)/constructor.clj \
  $(GLOAT-LINK-DIR)/representer.clj \
  $(GLOAT-LINK-DIR)/desolver.clj \
  $(GLOAT-LINK-DIR)/serializer.clj \
  $(GLOAT-LINK-DIR)/emitter.clj \
  $(CORE-YAMLSTAR)/api.clj \
  gloat/src/libyamlstar.clj \

.PHONY: prepare-gloat
prepare-gloat: $(GLOAT-LINKS)
ifdef YAML-PARSER-DIR
	ln -sf $(YAML-PARSER-SRC-DIR)/yaml_parser/prelude.cljc $(GLOAT-LINK-DIR)/yaml_parser/prelude.clj
	ln -sf $(YAML-PARSER-SRC-DIR)/yaml_parser/parser.cljc $(GLOAT-LINK-DIR)/yaml_parser/parser.clj
	ln -sf $(YAML-PARSER-SRC-DIR)/yaml_parser/receiver.cljc $(GLOAT-LINK-DIR)/yaml_parser/receiver.clj
	ln -sf $(YAML-PARSER-SRC-DIR)/yaml_parser/grammar.cljc $(GLOAT-LINK-DIR)/yaml_parser/grammar.clj
	ln -sf $(YAML-PARSER-SRC-DIR)/yaml_parser/core.clj $(GLOAT-LINK-DIR)/yaml_parser/core.clj
endif

generate-go: prepare-gloat $(GLOAT)
	rm -rf $(GO-GENERATED-WORK)
	mkdir -p $(GO-GENERATED-WORK)
	$(GLOAT) --force \
	  --module $(GO-GENERATED-MODULE) \
	  -o $(GO-GENERATED-WORK)/ \
	  $(GLOAT-SRCS)
	test -f $(GO-GENERATED-WORK)/pkg/yamlstar/api/loader.go
	rm -rf $(GO-GENERATED-DIR)
	mkdir -p $(GO-GENERATED-DIR)
	cp -R $(GO-GENERATED-WORK)/pkg $(GO-GENERATED-DIR)/
	rm -rf $(GO-GENERATED-WORK)

# GraalVM build
LIBYAMLSTAR-VERSION := $(VERSION)
LIB-JAR := graalvm/target/libyamlstar-$(LIBYAMLSTAR-VERSION)-SNAPSHOT-standalone.jar
CORE-JAR := $(MAVEN-REPOSITORY)/yamlstar/core/$(LIBYAMLSTAR-VERSION)-SNAPSHOT/core-$(LIBYAMLSTAR-VERSION)-SNAPSHOT.jar

ifdef YAML-PARSER-DIR
LEIN-YAML-PARSER := \
  update-in :source-paths conj '"$(YAML-PARSER-SRC-DIR)"' --

.PHONY: _yaml-parser-core
_yaml-parser-core: $(LEIN) $(YAML-PARSER-SOURCE-FILES)
	cd ../core && $(LEIN) $(LEIN-YAML-PARSER) with-profile +uberjar install

$(CORE-JAR): _yaml-parser-core
endif

# --- TEST-WITH-GLOJURE support ---
ifdef TEST-WITH-GLOJURE

GLOAT-GLOJURE-ENV := \
  GLOJURE_DIR=$(abspath $(TEST-WITH-GLOJURE)) \
  GOCACHE=$(GLOAT-DIR)/.cache/local/cache/go-build \
  GOMODCACHE=$(GLOAT-DIR)/.cache/local/go/pkg/mod

_twg-build: $(GLOAT-RUN-DEPS) | $(GLOAT-DIR)
	@echo "Building glj from $(TEST-WITH-GLOJURE)..."
	env -u GOROOT $(GLOAT-GLOJURE-ENV) \
	  $(GLOAT-RUN) --glj-build
endif
# --- end TEST-WITH-GLOJURE ---


build: validate-backend build-$(YAMLSTAR-BACKEND) select-backend

build-glj: $(LIB-SO-GLJ)
	cp $(GLOAT-LIB-HEADER) $(LIB-HEADER-GLJ)
	cp $(GLOAT-ISOLATE-HEADER) $(LIB-DIR)/graal_isolate.h
ifneq ($(OS-NAME),windows)
ifeq (,$(GLOAT-WINDOWS-TARGET))
	ln -sf $(notdir $(LIB-SO-GLJ)) $(LIB-SO-GLJ).$(VERSION)
endif
endif

build-cpp: $(LIB-SO-CPP)
	cp $(GLOAT-LIB-HEADER) $(LIB-HEADER-CPP)
	cp $(GLOAT-ISOLATE-HEADER) $(LIB-DIR)/graal_isolate.h
ifneq ($(OS-NAME),windows)
ifeq (,$(GLOAT-WINDOWS-TARGET))
	ln -sf $(notdir $(LIB-SO-CPP)) $(LIB-SO-CPP).$(VERSION)
endif
endif

build-jvm: $(LIB-SO-JVM)
ifneq ($(OS-NAME),windows)
	ln -sf $(notdir $(LIB-SO-JVM)) $(LIB-SO-JVM).$(VERSION)
endif

# Compatibility entry points retained for benchmark and external callers.
build-gloat:
	YAMLSTAR_GLJ=1 $(MAKE) build-glj LIB-NAME-GLJ=$(LIB-NAME-GLOAT)

build-graalvm:
	YAMLSTAR_JVM=1 $(MAKE) build-jvm LIB-NAME-JVM=$(LIB-NAME)

# Build both: each requires separate make invocations (tool includes conflict)
build-all:
	YAMLSTAR_GLJ=1 $(MAKE) build-glj
	YAMLSTAR_CPP=1 $(MAKE) build-cpp
	YAMLSTAR_JVM=1 $(MAKE) build-jvm

test:

INSTALL-LIB := $(LIB-SO)

install: build
	mkdir -p $(PREFIX)/lib/
	install -m 644 $(INSTALL-LIB) $(PREFIX)/lib/

$(RAPIDYAML-STAMP):
	$(MAKE) -C .. rapidyaml

_gloat-template-base: | $(GLOAT-DIR)
	@perl -0pi -e 's/\n\tyamlstar_ryml "$(subst /,\/,$(RYMLSHIM-MODULE))"//g; s/\n\tyamlstar_ryml\.Install\(\)\n//g' \
	  '$(GLOAT-DIR)/template/lib-main.go'
	@perl -0pi -e 's|replace github\.com/gloathub/gloat/ys/pkg => [^\n]*|replace github.com/gloathub/gloat/ys/pkg => GLOAT-ROOT/ys/pkg\n|; s|replace $(subst /,\/,$(RYMLSHIM-MODULE)) => [^\n]*||g; s/\nrequire $(subst /,\/,$(RYMLSHIM-MODULE)) v0\.0\.0\n//g; s/\n\n+/\n/g' \
	  '$(GLOAT-DIR)/template/go.mod'

_gloat-cpp-template: _gloat-template-base
	@if ! grep -q '$(RYMLSHIM-MODULE)' '$(GLOAT-DIR)/template/lib-main.go'; then \
	  perl -0pi -e 's/import \(\n/import (\n\tyamlstar_ryml "$(subst /,\/,$(RYMLSHIM-MODULE))"\n/' \
	    '$(GLOAT-DIR)/template/lib-main.go'; \
	  perl -0pi -e 's/(\t\/\/ Set \*ns\* to the user'"'"'s namespace)/\tyamlstar_ryml.Install()\n\n$$1/' \
	    '$(GLOAT-DIR)/template/lib-main.go'; \
	fi
	@printf '\nrequire $(RYMLSHIM-MODULE) v0.0.0\n' \
	  >> '$(GLOAT-DIR)/template/go.mod'
	@printf 'replace $(RYMLSHIM-MODULE) => $(RYMLSHIM-DIR)\n' \
	  >> '$(GLOAT-DIR)/template/go.mod'

$(LIB-SO-GLJ): $(GLOAT-SRCS) $(GLOAT-LINKS) $(GLOAT-RUN-DEPS) | $(GLOAT-DIR)
	@echo "Building gloat shared library..."
	mkdir -p $(LIB-DIR)
ifdef TEST-WITH-GLOJURE
	$(MAKE) _twg-build
endif
	$(MAKE) _gloat-template-base
	</dev/null env -u GOROOT \
	  $(if $(GLOAT_PATH),PATH='$(GLOAT_PATH)') \
	  $(GLOAT-GLOJURE-ENV) \
	  $(GLOAT-RUN) --force $(GLOAT-PLATFORM-OPT) \
	  $(GLOAT-SRCS) -o $@
	@echo "Gloat shared library created at: $@"

$(LIB-SO-CPP): $(GLOAT-SRCS) $(GLOAT-LINKS) $(GLOAT-RUN-DEPS) \
  $(RYMLSHIM-SRCS) $(RAPIDYAML-STAMP) | $(GLOAT-DIR)
	@echo "Building rapidyaml Gloat shared library..."
	mkdir -p $(LIB-DIR)
ifdef TEST-WITH-GLOJURE
	$(MAKE) _twg-build
endif
	$(MAKE) _gloat-cpp-template
	</dev/null env -u GOROOT \
	  $(if $(GLOAT_PATH),PATH='$(GLOAT_PATH)') \
	  $(GLOAT-GLOJURE-ENV) \
	  $(GLOAT-RUN) --force $(GLOAT-PLATFORM-OPT) \
	  $(GLOAT-SRCS) -o $@
	@echo "Rapidyaml Gloat shared library created at: $@"

ifndef YAML-PARSER-DIR
$(CORE-JAR):
	$(MAKE) -C ../core install
endif

$(LIB-JAR): $(CORE-JAR) $(LEIN)
	cd graalvm && $(LEIN) $(LEIN-YAML-PARSER) uberjar

$(LIB-SO-JVM): $(LIB-JAR) $(REFLECTION-JSON) $(GRAALVM)
	@echo "Building GraalVM shared library..."
	mkdir -p $(LIB-DIR)
	$(NATIVE-IMAGE) \
	  $(NATIVE-OPTS) \
	  --shared \
	  -jar $< \
	  -o $(LIB-DIR)/$(LIB-NAME-JVM)
	test -f $@
	@echo "GraalVM shared library created at: $@"
	@echo "Header: $(LIB-HEADER-JVM)"

# graal_isolate.h is a side effect of building the GraalVM SO
$(LIB-DIR)/graal_isolate.h: $(LIB-SO-JVM)

select-backend: validate-backend $(SELECTED-LIB-SO)
	mkdir -p $(LIB-DIR)
ifeq ($(OS-NAME),windows)
	cp -f $(SELECTED-LIB-SO) $(LIB-SO)
	cp -f $(SELECTED-LIB-HEADER) $(LIB-HEADER)
else
	ln -sf $(notdir $(SELECTED-LIB-SO)) $(LIB-SO)
	ln -sf $(notdir $(SELECTED-LIB-SO)) $(LIB-SO-VERSION)
	ln -sf $(notdir $(SELECTED-LIB-SO)) $(LIB-SO).0
	ln -sf $(notdir $(SELECTED-LIB-HEADER)) $(LIB-HEADER)
endif
