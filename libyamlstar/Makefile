include ../common/init.mk

YAMLSTAR_GLOJURE ?= 1

# Load platform tools before common.mk (which includes shell.mk last)
# Avoids graalvm.mk/java.mk conflict when both platforms are present
ifdef YAMLSTAR_GLOJURE
include $M/gloat.mk
else
include $M/graalvm.mk
include $M/clojure.mk
include $M/lein.mk
endif

include ../common/common.mk

ifndef YAMLSTAR_GLOJURE
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
LIB-SO-GLOAT := $(LIB-DIR)/$(LIB-NAME-GLOAT).$(SO)
LIB-SO := $(LIB-DIR)/$(LIB-NAME).$(SO)
GLOAT-LIB-HEADER := gloat/include/libyamlstar.h
GLOAT-ISOLATE-HEADER := gloat/include/graal_isolate.h

CORE-YAMLSTAR := ../core/src/yamlstar

# Gloat compiles Clojure sources directly, so use the sources shipped in the
# exact yaml-parser artifact consumed by the JVM build.
YAML-PARSER-VERSION := 0.2.1
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

_twg-build: | $(GLOAT-DIR)
	@echo "Building glj from $(TEST-WITH-GLOJURE)..."
	$(GLOAT-GLOJURE-ENV) $(GLOAT-BIN)/gloat --glj-build

$(LIB-SO-GLOAT): _twg-build

endif
# --- end TEST-WITH-GLOJURE ---


ifdef YAMLSTAR_GLOJURE
build: build-gloat
else
build: build-graalvm
endif

LIB-HEADER := $(LIB-DIR)/$(LIB-NAME).h
LIB-HEADER-GLOAT := $(LIB-DIR)/$(LIB-NAME-GLOAT).h

build-gloat: $(LIB-SO-GLOAT)
	cp $(GLOAT-LIB-HEADER) $(LIB-HEADER)
	cp $(GLOAT-ISOLATE-HEADER) $(LIB-DIR)/graal_isolate.h
ifneq ($(OS-NAME),windows)
	ln -sf $(notdir $(LIB-SO-GLOAT)) \
	  $(LIB-DIR)/$(LIB-NAME-GLOAT).$(SO).$(VERSION)
endif

build-graalvm: $(LIB-SO)
ifneq ($(OS-NAME),windows)
	ln -sf $(notdir $(LIB-SO)) $(LIB-DIR)/$(LIB-NAME).$(SO).$(VERSION)
endif

# Build both: each requires separate make invocations (tool includes conflict)
build-all:
	YAMLSTAR_GLOJURE=1 $(MAKE) build-gloat
	env -u YAMLSTAR_GLOJURE $(MAKE) build-graalvm \
	  LIB-NAME=libyamlstar-graalvm

test:

INSTALL-LIB := $(LIB-SO)

install: build
	mkdir -p $(PREFIX)/lib/
	install -m 644 $(INSTALL-LIB) $(PREFIX)/lib/

ifdef YAMLSTAR_GLOJURE
$(LIB-SO-GLOAT): $(GLOAT-SRCS) $(GLOAT-LINKS) | $(GLOAT-DIR)
	@echo "Building gloat shared library..."
	mkdir -p $(LIB-DIR)
	</dev/null env -u GOROOT $(GLOAT-GLOJURE-ENV) $(GLOAT-BIN)/gloat --force $(GLOAT-SRCS) -o $@
	@echo "Gloat shared library created at: $@"
endif

ifndef YAML-PARSER-DIR
$(CORE-JAR):
	$(MAKE) -C ../core install
endif

$(LIB-JAR): $(CORE-JAR) $(LEIN)
	cd graalvm && $(LEIN) $(LEIN-YAML-PARSER) uberjar

ifndef YAMLSTAR_GLOJURE
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
