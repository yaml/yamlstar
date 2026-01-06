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
  perl \
  python \
  rust \

BINDING-TESTS := $(BINDINGS:%=test-%)
ALL-TESTS := \
  test-core \
  test-cli \
  test-libyamlstar \
  $(BINDING-TESTS)


build jar install::
	$(MAKE) -C core $@ v=$v
	$(MAKE) -C cli $@ v=$v
	$(MAKE) -C libyamlstar $@ v=$v

test:: $(ALL-TESTS)

$(ALL-TESTS):
	@echo '--------------------------------------------------'
	@echo '   $@'
	@echo '--------------------------------------------------'
	$(MAKE) -C $(@:test-%=%) test v=$v

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
