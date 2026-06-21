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
YAML-TEST-SUITE-NS := yamlstar.yaml-test-suite-test
YAML-TEST-SUITE-LOAD := $(YAML-TEST-SUITE-NS)/yaml-test-suite-loader-conformance
YAML-TEST-SUITE-ROUNDTRIP := $(YAML-TEST-SUITE-NS)/yaml-test-suite-roundtrip-conformance
YAML-TEST-SUITE-EMIT := $(YAML-TEST-SUITE-NS)/yaml-test-suite-emit-conformance


build:

test jar: $(LEIN)
	lein $@

test-suite-fixture:
	@if [ ! -d '$(YAML-TEST-SUITE-DIR)/.git' ]; then \
	  rm -rf '$(YAML-TEST-SUITE-DIR)'; \
	  git clone --no-checkout '$(YAML-TEST-SUITE-URL)' \
	    '$(YAML-TEST-SUITE-DIR)'; \
	fi
	git -C '$(YAML-TEST-SUITE-DIR)' fetch --depth 1 origin \
	  '$(YAML-TEST-SUITE-COMMIT)'
	git -C '$(YAML-TEST-SUITE-DIR)' checkout -q \
	  '$(YAML-TEST-SUITE-COMMIT)'

test-suite: $(LEIN) test-suite-fixture
	YAMLSTAR_TEST_SUITE_DIR='$(YAML-TEST-SUITE-DIR)' \
	  lein with-profile +test-suite test $(YAML-TEST-SUITE-NS)

test-suite-load: $(LEIN) test-suite-fixture
	YAMLSTAR_TEST_SUITE_DIR='$(YAML-TEST-SUITE-DIR)' \
	  lein with-profile +test-suite test :only $(YAML-TEST-SUITE-LOAD)

test-suite-roundtrip: $(LEIN) test-suite-fixture
	YAMLSTAR_TEST_SUITE_DIR='$(YAML-TEST-SUITE-DIR)' \
	  lein with-profile +test-suite test :only $(YAML-TEST-SUITE-ROUNDTRIP)

test-suite-emit: $(LEIN) test-suite-fixture
	YAMLSTAR_TEST_SUITE_DIR='$(YAML-TEST-SUITE-DIR)' \
	  lein with-profile +test-suite test :only $(YAML-TEST-SUITE-EMIT)

install: $(LEIN)
	lein with-profile +uberjar $@

.PHONY: test-suite test-suite-fixture test-suite-load test-suite-roundtrip test-suite-emit
