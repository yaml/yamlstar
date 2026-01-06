include ../common/init.mk
include $M/lein.mk
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
