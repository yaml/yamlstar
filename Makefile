M := .cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/graalvm.mk
include $M/lein.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := \
  META-INF/ \

MAKES-REALCLEAN := \
  $(MAVEN-REPOSITORY) \

MAKES-DISTCLEAN += \
  .clj-kondo/ \
  .lsp/ \

BINDINGS := \
  go \
  python \
  rust \

TESTS := $(BINDINGS:%=test-%)


build test jar install::
	$(MAKE) -C core $@
	$(MAKE) -C cli $@
	$(MAKE) -C libyamlstar $@

test:: $(TESTS)

$(TESTS):
	$(MAKE) -C $(@:test-%=%) test

core:
	$(MAKE) -C core install

cli:
	$(MAKE) -C cli build

libyamlstar:
	$(MAKE) -C libyamlstar build

clean::
	$(MAKE) --no-pr -C cli clean
	$(MAKE) --no-pr -C core clean
	$(MAKE) --no-pr -C libyamlstar clean
	$(MAKE) --no-pr -C python clean

.PHONY: cli core libyamlstar test
