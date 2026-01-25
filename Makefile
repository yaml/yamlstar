M := .cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/gh.mk
include $M/graalvm.mk
include $M/lein.mk
include $M/yamlscript.mk
include $M/clean.mk
include $M/shell.mk

# Extract version from Meta file
VERSION := $(shell grep '^version:' Meta | cut -d' ' -f2)

MAKES-CLEAN := \
  META-INF/ \

MAKES-REALCLEAN := \
  $(MAVEN-REPOSITORY) \

MAKES-DISTCLEAN += \
  .clj-kondo/ \
  .lsp/ \

BINDING-LANGS := \
  clojure \
  csharp \
  fortran \
  go \
  java \
  nodejs \
  perl \
  python \
  rust \

ALL-DIRS := \
  cli \
  core \
  libyamlstar \
  $(BINDING-LANGS) \

ALL-CLEAN := $(ALL-DIRS:%=clean-%)
ALL-REALCLEAN := $(ALL-DIRS:%=realclean-%)
ALL-SHELL := $(BINDING-LANGS:%=shell-%)

BINDING-TESTS := $(BINDING-LANGS:%=test-%)
ALL-TESTS := \
  test-core \
  test-cli \
  test-libyamlstar \
  $(BINDING-TESTS)


build jar install::
	$(MAKE) -C core $@ v=$v
	$(MAKE) -C cli $@ v=$v
	$(MAKE) -C libyamlstar $@ v=$v

test:: test-core

test-all: $(ALL-TESTS)

test-bindings: $(BINDING-TESTS)

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
	$(MAKE) -C example $@

realclean:: $(ALL-REALCLEAN)

$(ALL-CLEAN):
	$(MAKE) --no-pr -C $(@:clean-%=%) clean

$(ALL-REALCLEAN):
	$(MAKE) --no-pr -C $(@:realclean-%=%) realclean

$(ALL-SHELL):
	$(MAKE) -C $(@:shell-%=%) shell

#------------------------------------------------------------------------------
# Release targets
#------------------------------------------------------------------------------

export OLD_VERSION := $o
export NEW_VERSION := $n
RELEASE-TARGETS := \
  version-bump \
  check-version \
  check-tag \
  release-lib \
  release-tag \
  release-github \
  check-release \

$(RELEASE-TARGETS):
	util/release $@

RELEASE-BINDINGS := $(BINDING-LANGS:%=release-%)

release-bindings: check-release $(RELEASE-BINDINGS)

$(filter-out release-perl, $(RELEASE-BINDINGS)): $(GH)
	$(MAKE) -C $(@:release-%=%) release

release-perl:
	$(MAKE) -C perl release-cpan

.PHONY: cli core libyamlstar test
