include ../common/init.mk
include $M/lein.mk
include $M/shell.mk

MAKES-CLEAN := \
  target \
  .lein-* \
  .cpcache \
  pom.xml \

YAML-TEST-SUITE-URL := https://github.com/yaml/yaml-test-suite.git
YAML-TEST-SUITE-COMMIT := 6ad3d2c62885d82fc349026c136ef560838fdf3d
YAML-TEST-SUITE-DIR ?= ../yaml-test-suite


build:

test jar: $(LEIN)
	lein $@

test-suite: $(LEIN)
	@if [ ! -d '$(YAML-TEST-SUITE-DIR)/.git' ]; then \
	  rm -rf '$(YAML-TEST-SUITE-DIR)'; \
	  git clone --no-checkout '$(YAML-TEST-SUITE-URL)' \
	    '$(YAML-TEST-SUITE-DIR)'; \
	fi
	git -C '$(YAML-TEST-SUITE-DIR)' fetch --depth 1 origin \
	  '$(YAML-TEST-SUITE-COMMIT)'
	git -C '$(YAML-TEST-SUITE-DIR)' checkout -q \
	  '$(YAML-TEST-SUITE-COMMIT)'
	YAMLSTAR_TEST_SUITE_DIR='$(YAML-TEST-SUITE-DIR)' \
	  lein with-profile +test-suite test yamlstar.yaml-test-suite-test

install: $(LEIN)
	lein with-profile +uberjar $@
