M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/graalvm.mk
include $M/lein.mk
include $M/clean.mk
include $M/shell.mk

ROOT := $(shell cd .. && pwd -P)
include $(ROOT)/common/native.mk

MAKES-CLEAN := \
  target \
  .lein-* \
  bin \

# Version and paths
YAMLSTAR_VERSION := 0.1.0-SNAPSHOT
CLI-BIN := bin/yaml
CLI-JAR := target/cli-$(YAMLSTAR_VERSION)-standalone.jar

# Core dependency jar in local maven repository
CORE-JAR := $(MAVEN-REPOSITORY)/yamlstar/core/$(YAMLSTAR_VERSION)/core-$(YAMLSTAR_VERSION).jar


build: $(CLI-BIN)

test: $(CORE-JAR) $(LEIN)
	lein test

jar: $(CLI-JAR)

install:

$(CLI-JAR): $(CORE-JAR) $(LEIN) $(GRAAVM)
	lein uberjar

$(CORE-JAR):
	$(MAKE) -C ../core install

$(CLI-BIN): $(CLI-JAR) $(REFLECTION-JSON) $(GRAALVM)
	@echo "Building native CLI binary..."
	mkdir -p $(dir $@)
	$(GRAALVM) \
	  $(NATIVE-OPTS) \
	  -jar $< \
	  -o $@
	@echo "Native binary created at: $@"

.PHONY: test
