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

ifeq ($(OS-NAME),windows)
CLI-BIN := bin/yaml.exe
else
CLI-BIN := bin/yaml
endif
CLI-OUT := bin/yaml
CLI-JAR := target/cli-$(LIBYAMLSTAR-VERSION)-SNAPSHOT-standalone.jar

GLOAT-ENGINE := $(or $(GLOAT_ENGINE),glj)
CLI-BIN-GLOAT := bin/yaml-gloat-$(GLOAT-ENGINE)
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

ifdef TEST-WITH-GLOJURE
GLOAT-GLOJURE-ENV := \
  GLOJURE_DIR=$(abspath $(TEST-WITH-GLOJURE)) \
  GOCACHE=$(GLOAT-DIR)/.cache/local/cache/go-build \
  GOMODCACHE=$(GLOAT-DIR)/.cache/local/go/pkg/mod

.PHONY: _prepare-glj
_prepare-glj: | $(GLOAT-DIR)
	$(GLOAT-GLOJURE-ENV) $(GLOAT-BIN)/gloat --glj-build

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

build-gloat:
	YAMLSTAR_GLOJURE=1 $(MAKE) _build-gloat \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  $(if $(GLOAT-DIR),GLOAT-DIR=$(GLOAT-DIR)) \
	  $(if $(YAML-PARSER-DIR),YAML-PARSER-DIR=$(YAML-PARSER-DIR)) \
	  $(if $(TEST-WITH-GLOJURE),TEST-WITH-GLOJURE=$(TEST-WITH-GLOJURE))

build-gloat-glj:
	$(MAKE) build-gloat GLOAT_ENGINE=glj

build-gloat-lgvm:
	$(MAKE) build-gloat GLOAT_ENGINE=lgvm

_build-gloat: $(CLI-BIN-GLOAT)

.PHONY: _prepare-gloat
_prepare-gloat:
	YAMLSTAR_GLOJURE=1 $(MAKE) -C ../libyamlstar prepare-gloat \
	  GLOAT-DIR=$(GLOAT-DIR) \
	  $(if $(YAML-PARSER-DIR),YAML-PARSER-DIR=$(YAML-PARSER-DIR))

$(CLI-BIN-GLOAT): _prepare-gloat \
  $(GLOAT-CLI-GLJ-DEP) \
  $(GLOAT-CORE)/parser.clj \
  $(GLOAT-CORE)/composer.clj \
  $(GLOAT-CORE)/resolver.clj \
  $(GLOAT-CORE)/api.clj \
  gloat/src/yamlstar/cli.clj | $(GLOAT-DIR)
	mkdir -p $(dir $@)
	</dev/null env -u GOROOT $(GLOAT-GLOJURE-ENV) \
	  GLOAT_ENGINE=$(GLOAT-ENGINE) \
	  $(GLOAT-BIN)/gloat --force $(GLOAT-CLI-SRCS) -o $@
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
