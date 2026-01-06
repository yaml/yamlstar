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

BINDING-DIRS := \
  go \
  nodejs \
  perl \
  python \
  rust \

ALL-DIRS := \
  cli \
  core \
  libyamlstar \
  $(BINDING-DIRS) \

ALL-CLEAN := $(ALL-DIRS:%=clean-%)
ALL-REALCLEAN := $(ALL-DIRS:%=realclean-%)
ALL-DISTCLEAN := $(ALL-DIRS:%=distclean-%)

BINDING-TESTS := $(BINDING-DIRS:%=test-%)
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

clean:: $(ALL-CLEAN)
realclean:: $(ALL-REALCLEAN)
distclean:: $(ALL-DISTCLEAN)

$(ALL-CLEAN):
	$(MAKE) --no-pr -C $(@:clean-%=%) clean

$(ALL-REALCLEAN):
	$(MAKE) --no-pr -C $(@:realclean-%=%) realclean

$(ALL-DISTCLEAN):
	$(MAKE) --no-pr -C $(@:distclean-%=%) distclean

.PHONY: cli core libyamlstar test
