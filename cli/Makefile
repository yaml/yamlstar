include ../common/init.mk
M := ../.cache/makes
include $M/graalvm.mk
include $M/lein.mk
include $(COMMON)/common.mk
include $(COMMON)/native.mk

MAKES-CLEAN := \
  target \
  .lein-* \
  bin \

CLI-BIN := bin/yaml
CLI-JAR := target/cli-$(LIBYAMLSTAR-VERSION)-standalone.jar

# Core dependency jar in local maven repository
CORE-JAR := $(MAVEN-REPOSITORY)/yamlstar/core/$(LIBYAMLSTAR-VERSION)/core-$(LIBYAMLSTAR-VERSION).jar


test: $(CORE-JAR) $(LEIN)
	lein test

build: $(CLI-BIN)

jar: $(CLI-JAR)

install:

$(CLI-JAR): $(CORE-JAR) $(LEIN)
	lein uberjar

$(CORE-JAR):
	$(MAKE) -C ../core install

$(CLI-BIN): $(CLI-JAR) $(GRAALVM)
	mkdir -p $(dir $@)
	native-image \
	  $(NATIVE-OPTS) \
	  -jar $< \
	  -o $@
	@echo "Native binary created at: $@"
