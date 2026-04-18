M := .cache/makes
include common/init.mk
include $M/babashka.mk
include $M/gh.mk
include $M/gloat.mk
include $M/yamlscript.mk
include $M/clean.mk
include $M/shell.mk

# Extract version from Meta file
VERSION := $(shell grep '^version:' Meta | cut -d' ' -f2)

MAKES-CLEAN := \
  META-INF/ \
  release-* \

MAKES-REALCLEAN := \
  $(MAVEN-REPOSITORY) \

MAKES-DISTCLEAN += \
  .clj-kondo/ \
  .lsp/ \
  $(INGY-LOCAL-DIR) \
  ext/ \

BINDING-LANGS ?= \
  clojure \
  csharp \
  delphi \
  fortran \
  go \
  java \
  nodejs \
  perl \
  python \
  rust \

# Gloat build cannot run JVM-based bindings (clojure, java)
ifdef YAMLSTAR_GLOJURE
BINDING-SKIP ?= clojure java
else
BINDING-SKIP ?=
endif

BINDING-LANGS := $(filter-out $(BINDING-SKIP),$(BINDING-LANGS))

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


build install::
	$(MAKE) -C libyamlstar $@

test:: test-core

test-all: $(ALL-TESTS)

test-bindings: $(BINDING-TESTS)

test-examples:
	$(MAKE) --no-pr -C example test

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

serve:
	$(MAKE) -C www serve

publish:
	$(MAKE) -C www publish

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
  release-github \
  check-release \

$(RELEASE-TARGETS):
	util/release $@

# Legacy target for backward compatibility
release-tag-legacy:
	util/release release-tag

RELEASE-BINDINGS := $(BINDING-LANGS:%=release-%)

release-bindings: check-release $(RELEASE-BINDINGS)

$(filter-out release-perl, $(RELEASE-BINDINGS)): $(GH)
	$(MAKE) -C $(@:release-%=%) release

release-perl:
	$(MAKE) -C perl release-cpan

# Interactive release workflow targets
release:
	util/release release

release-list:
	util/release release-list

release-sanity-check:
	util/release sanity-check

release-version-bump:
	util/release version-bump-files

release-changelog:
	util/release changelog

release-commit:
	util/release commit

release-tag:
	util/release tag

release-push:
	util/release push

release-build-github:
	util/release build-github

#------------------------------------------------------------------------------
# External dependencies
#------------------------------------------------------------------------------

YRP-REPO-HTTPS := https://github.com/yaml/yaml-reference-parser.git
YRP-REPO-SSH := git@github.com:yaml/yaml-reference-parser.git
YRP-BRANCH := perf
YRP-DIR := ext/yaml-reference-parser
YRP-CLJ := $(YRP-DIR)/parser-1.2/clojure/src/yaml_parser
YRP-BIN := $(YRP-DIR)/parser-1.2/clojure/bin
YRP-SPEC := $(YRP-DIR)/parser-1.2/build/yaml-spec-1.2-patched.yaml

PARSER-DIR := core/src/yamlstar/parser
PARSER-NS := yamlstar.parser

$(YRP-DIR):
	git clone -q -b $(YRP-BRANCH) $(YRP-REPO-HTTPS) $@
	git -C $@ remote add push $(YRP-REPO-SSH)

$(PARSER-DIR): $(YRP-DIR)
	@mkdir -p $@
	@for f in $(YRP-CLJ)/prelude.cljc $(YRP-CLJ)/parser.cljc $(YRP-CLJ)/receiver.cljc; do \
	  sed 's/yaml-parser/$(PARSER-NS)/g' "$$f" > $@/$$(basename "$$f"); \
	done
	$(BB) $(YRP-BIN)/generate-yaml-grammar \
	  --from $(YRP-SPEC) \
	  --namespace $(PARSER-NS) > $@/grammar.cljc

ext: $(PARSER-DIR)

.PHONY: cli core libyamlstar test
