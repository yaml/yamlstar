M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/lein.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := \
  target \
  .lein-* \
  .cpcache \
  pom.xml \

build:

test jar: $(LEIN)
	lein $@

install: $(LEIN)
	lein with-profile +uberjar $@

.PHONY: test
