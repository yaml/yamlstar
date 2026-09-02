include ../common/init.mk

YAMLSTAR_ENGINE ?= $(if $(YAMLSTAR_GLOJURE),glojure,graalvm)

# Load platform tools before common.mk (which includes shell.mk last)
# Avoids graalvm.mk/java.mk conflict when both platforms are present
ifeq ($(YAMLSTAR_ENGINE),glojure)
include $M/gloat.mk
else ifeq ($(YAMLSTAR_ENGINE),graalvm)
include $M/graalvm.mk
include $M/clojure.mk
include $M/lein.mk
else
$(error Unknown YAMLSTAR_ENGINE '$(YAMLSTAR_ENGINE)')
endif
include $M/perl.mk

include ../common/common.mk

ifeq ($(YAMLSTAR_ENGINE),graalvm)
include ../common/native.mk
endif

MAKES-CLEAN := \
  graalvm/target \
  graalvm/.lein-* \
  cache/yaml-parser-* \
  cache/go-yaml-* \
  cache/libyamlstar-glojure-abi-* \
  cache/gloat-srcs \
  ../internal/goyamlparser \
  lib/ \

MAKES-REALCLEAN := \
  lib \

# Library paths and names
SO ?= so
LIB-DIR := lib
LIB-NAME ?= libyamlstar-graalvm
LIB-NAME-GLOAT ?= libyamlstar-glojure
LIB-SO-GLOAT := $(LIB-DIR)/$(LIB-NAME-GLOAT).$(SO)
LIB-SO-GLOAT-DEFAULT := $(LIB-DIR)/libyamlstar.$(SO)
LIB-SO := $(LIB-DIR)/$(LIB-NAME).$(SO)
LIBYAMLSTAR-GLOAT-ABI := export-load-opts-v2
LIBYAMLSTAR-GLOAT-ABI-STAMP := cache/libyamlstar-glojure-abi-$(LIBYAMLSTAR-GLOAT-ABI).stamp
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
CORE-SRCS := \
  $(wildcard $(CORE-YAMLSTAR)/*.clj) \
  $(wildcard $(CORE-YAMLSTAR)/*.cljc) \
  $(wildcard $(CORE-YAMLSTAR)/plugin/*.clj) \
  $(wildcard $(CORE-YAMLSTAR)/plugin/parser/*.clj) \
  $(wildcard $(CORE-YAMLSTAR)/plugin/parser/*.cljc)

# Gloat compiles Clojure sources directly, so use the sources shipped in the
# exact yaml-parser artifact consumed by the JVM build.
YAML-PARSER-VERSION := 0.2.3
YAML-PARSER-JAR := cache/yaml-parser-$(YAML-PARSER-VERSION).jar
YAML-PARSER-URL := https://repo.clojars.org/org/yamlstar/yaml-parser/$(YAML-PARSER-VERSION)/yaml-parser-$(YAML-PARSER-VERSION).jar
YAML-PARSER-GLOAT-PREPARE := gloat/prepare-yaml-parser
GO-YAML-REF ?= 643e93b9c9bec2be8ae2b842a3492c54532549c9
GO-YAML-REPO ?= https://github.com/yaml/go-yaml
GO-YAML-SRC-DIR := cache/go-yaml-$(GO-YAML-REF)
GO-YAML-STAMP := $(GO-YAML-SRC-DIR)/.fetched
GO-YAML-GENERATED-DIR := ../internal/goyamlparser
GO-YAML-GENERATED-STAMP := $(GO-YAML-GENERATED-DIR)/.generated
GO-YAML-PREPARE := gloat/prepare-go-yaml-parser
GLOAT-GO-YAML-PATCH := gloat/patch-gloat-extra-go-dir
GLOAT-GO-YAML-PATCH-STAMP := $(GLOAT-DIR)/.yamlstar-go-yaml-extra-patched

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
$(YAML-PARSER-SRC-STAMP): $(YAML-PARSER-JAR) $(YAML-PARSER-GLOAT-PREPARE) \
  $(PERL)
	@mkdir -p $(YAML-PARSER-SRC-DIR)
	unzip -oq $< 'yaml_parser/*.clj' 'yaml_parser/*.cljc' -d $(YAML-PARSER-SRC-DIR)
	$(PERL) $(YAML-PARSER-GLOAT-PREPARE) $(YAML-PARSER-SRC-DIR)
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

$(GLOAT-LINK-DIR)/plugin/parser/go_yaml.clj: $(CORE-YAMLSTAR)/plugin/parser/go_yaml.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GO-YAML-STAMP):
	rm -rf '$(GO-YAML-SRC-DIR).tmp'
	mkdir -p cache
	git init '$(GO-YAML-SRC-DIR).tmp'
	git -C '$(GO-YAML-SRC-DIR).tmp' fetch --depth=1 '$(GO-YAML-REPO)' '$(GO-YAML-REF)'
	git -C '$(GO-YAML-SRC-DIR).tmp' checkout --detach FETCH_HEAD
	rm -rf '$(GO-YAML-SRC-DIR)'
	mv '$(GO-YAML-SRC-DIR).tmp' '$(GO-YAML-SRC-DIR)'
	touch '$@'

$(GO-YAML-GENERATED-STAMP): $(GO-YAML-STAMP) $(GO-YAML-PREPARE) $(PERL)
	PERL=$(PERL) bash $(GO-YAML-PREPARE) $(GO-YAML-SRC-DIR) \
	  $(GO-YAML-GENERATED-DIR)

prepare-go-yaml-parser: $(GO-YAML-GENERATED-STAMP)

$(GLOAT-GO-YAML-PATCH-STAMP): $(GLOAT) $(GLOAT-GO-YAML-PATCH)
	bash $(GLOAT-GO-YAML-PATCH) $(GLOAT-DIR)/src/gloat.clj
	touch $@

patch-gloat-go-yaml: $(GLOAT-GO-YAML-PATCH-STAMP)

$(LIBYAMLSTAR-GLOAT-ABI-STAMP):
	@mkdir -p $(dir $@)
	printf '%s\n' '$(LIBYAMLSTAR-GLOAT-ABI)' > $@

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
  $(GLOAT-LINK-DIR)/plugin/parser/go_yaml.clj \

# Clojure source files compiled by gloat (order matters for namespaces)
GLOAT-SRCS := \
  $(GLOAT-LINK-DIR)/yaml_parser/prelude.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/parser.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/receiver.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/grammar.clj \
  $(GLOAT-LINK-DIR)/yaml_parser/core.clj \
  $(CORE-YAMLSTAR)/plugin.clj \
  $(CORE-YAMLSTAR)/options.clj \
  $(CORE-YAMLSTAR)/plugin/parser.clj \
  $(CORE-YAMLSTAR)/plugin/parser/reference.clj \
  $(GLOAT-LINK-DIR)/plugin/parser/go_yaml.clj \
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

prepare-gloat: $(GLOAT-LINKS) prepare-go-yaml-parser patch-gloat-go-yaml
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

_yaml-parser-core: $(LEIN) $(YAML-PARSER-SOURCE-FILES)
	cd ../core && $(LEIN) $(LEIN-YAML-PARSER) with-profile +uberjar install

$(CORE-JAR): _yaml-parser-core $(CORE-SRCS)
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

$(LIB-SO-GLOAT): _twg-build

endif
# --- end TEST-WITH-GLOJURE ---


LIB-HEADER := $(LIB-DIR)/libyamlstar.h
LIB-HEADER-GLOAT := $(LIB-DIR)/$(LIB-NAME-GLOAT).h

build: build-graalvm build-glojure

build-glojure:
	$(MAKE) _build-glojure YAMLSTAR_ENGINE=glojure

_build-glojure: $(LIB-SO-GLOAT)
	cp $(GLOAT-LIB-HEADER) $(LIB-HEADER)
	cp $(GLOAT-LIB-HEADER) $(LIB-HEADER-GLOAT)
	cp $(GLOAT-ISOLATE-HEADER) $(LIB-DIR)/graal_isolate.h
ifeq ($(OS-NAME),windows)
	cp $(LIB-SO-GLOAT) $(LIB-SO-GLOAT-DEFAULT)
else ifneq (,$(GLOAT-WINDOWS-TARGET))
	cp $(LIB-SO-GLOAT) $(LIB-SO-GLOAT-DEFAULT)
else
	ln -sf $(notdir $(LIB-SO-GLOAT)) $(LIB-SO-GLOAT-DEFAULT)
endif
ifneq ($(OS-NAME),windows)
ifeq (,$(GLOAT-WINDOWS-TARGET))
	ln -sf $(notdir $(LIB-SO-GLOAT)) \
	  $(LIB-DIR)/$(LIB-NAME-GLOAT).$(SO).$(VERSION)
	ln -sf $(notdir $(LIB-SO-GLOAT)) \
	  $(LIB-DIR)/libyamlstar.$(SO).$(VERSION)
endif
endif

build-graalvm:
	$(MAKE) _build-graalvm YAMLSTAR_ENGINE=graalvm

_build-graalvm: $(LIB-SO)
	if test -f $(LIB-DIR)/$(LIB-NAME).h; then \
	  cp $(LIB-DIR)/$(LIB-NAME).h $(LIB-HEADER); \
	fi
ifeq ($(OS-NAME),windows)
	cp $(LIB-SO) $(LIB-DIR)/libyamlstar.$(SO)
else
	ln -sf $(notdir $(LIB-SO)) $(LIB-DIR)/libyamlstar.$(SO)
endif
ifneq ($(OS-NAME),windows)
	ln -sf $(notdir $(LIB-SO)) $(LIB-DIR)/$(LIB-NAME).$(SO).$(VERSION)
endif

test:

ifeq ($(YAMLSTAR_ENGINE),glojure)
INSTALL-LIB := $(LIB-SO-GLOAT)
install: build-glojure
else
INSTALL-LIB := $(LIB-SO)
install: build-graalvm
endif

install:
	mkdir -p $(PREFIX)/lib/
	install -m 644 $(INSTALL-LIB) $(PREFIX)/lib/

ifeq ($(YAMLSTAR_ENGINE),glojure)
$(LIB-SO-GLOAT): $(GLOAT-SRCS) $(GLOAT-LINKS) $(GO-YAML-GENERATED-STAMP) \
  $(GLOAT-GO-YAML-PATCH-STAMP) $(GLOAT-LIB-HEADER) $(GLOAT-RUN-DEPS) \
  $(LIBYAMLSTAR-GLOAT-ABI-STAMP) | $(GLOAT-DIR)
	@echo "Building gloat shared library..."
	mkdir -p $(LIB-DIR)
	</dev/null env -u GOROOT \
	  GLOAT_MODULE=github.com/yaml/yamlstar \
	  GLOAT_EXTRA_GO_DIR=$(abspath $(GO-YAML-GENERATED-DIR)) \
	  $(if $(GLOAT_PATH),PATH='$(GLOAT_PATH)') \
	  $(GLOAT-GLOJURE-ENV) \
	  $(GLOAT-RUN) --force $(GLOAT-PLATFORM-OPT) \
	  $(GLOAT-SRCS) -o $@
	@echo "Gloat shared library created at: $@"
endif

ifndef YAML-PARSER-DIR
$(CORE-JAR): $(CORE-SRCS)
	$(MAKE) -C ../core install
endif

$(LIB-JAR): $(CORE-JAR) $(LEIN)
	cd graalvm && $(LEIN) $(LEIN-YAML-PARSER) uberjar

ifeq ($(YAMLSTAR_ENGINE),graalvm)
$(LIB-SO): $(LIB-JAR) $(REFLECTION-JSON) $(GRAALVM)
	@echo "Building GraalVM shared library..."
	mkdir -p $(LIB-DIR)
	$(NATIVE-IMAGE) \
	  $(NATIVE-OPTS) \
	  --shared \
	  -jar $< \
	  -o $(LIB-DIR)/$(LIB-NAME)
	test -f $@
	@echo "GraalVM shared library created at: $@"
	@echo "Header: $(LIB-HEADER)"

# graal_isolate.h is a side effect of building the GraalVM SO
$(LIB-DIR)/graal_isolate.h: $(LIB-SO)
endif
