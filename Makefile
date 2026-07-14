M := .cache/makes
include common/init.mk
include $M/gh.mk
include $M/gloat.mk
include $M/yamlscript.mk
include $M/clojure.mk
include $M/lein.mk
include $M/babashka.mk
include $M/clean.mk
include $M/perl.mk
include $M/bpan.mk
include $M/shellcheck.mk
include $M/shell.mk

# Extract version from Meta file
VERSION := $(shell grep '^version:' Meta | cut -d' ' -f2)
RELEASE-LOG := release-$n.log
RELEASE-SECRETS := $(wildcard $(HOME)/.yamlstar-secrets.yaml)
RELEASE-AUTH := $(strip $(GH_TOKEN)$(GITHUB_TOKEN)$(RELEASE-SECRETS))

MAKES-CLEAN := \
  META-INF/ \
  release-* \

MAKES-REALCLEAN := \
  $(MAVEN-REPOSITORY) \
  python/.eggs/ \
  www/site/ \
  www/venv/ \
  yaml-test-suite/ \

MAKES-DISTCLEAN += \
  .clj-kondo/ \
  .lsp/ \
  $(INGY-LOCAL-DIR) \

BINDING-LANGS ?= \
  clojure \
  crystal \
  csharp \
  d \
  dart \
  delphi \
  elixir \
  fortran \
  go \
  haskell \
  java \
  julia \
  kotlin \
  lua \
  nim \
  nodejs \
  perl \
  php \
  python \
  r \
  raku \
  ruby \
  rust \
  swift \
  zig \

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
TEST-TIME ?=

build install::
	$(MAKE) -C libyamlstar $@

test ?= test/*.t

unexport PERL5OPT PERL5LIB

test: test-unit test-core test-suite

test-parser:
	$(MAKE) -C core smoke-parser

TEST-UNIT-DEPS := $(PERL) $(BPAN)
ifneq ($(OS-NAME),windows)
TEST-UNIT-DEPS += $(SHELLCHECK)
endif

ifeq ($(OS-NAME),windows)
test-unit:
	@echo 'Skipping Perl unit tests on Windows'
else
test-unit: $(TEST-UNIT-DEPS)
	perl -x "$$(command -v prove)"$(if $(v), -v,) $(test)
endif

test-suite test-suite-load test-suite-roundtrip test-suite-emit:
	$(MAKE) -C core $@

test-all: $(ALL-TESTS)

test-bindings: $(BINDING-TESTS)

test-examples:
	$(MAKE) --no-pr -C example test

ifeq ($(OS-NAME),windows)
shellcheck:
	@echo 'Skipping shellcheck on Windows'
else
shellcheck: $(SHELLCHECK)
	$(SHELLCHECK) -x \
	  -e SC1091,SC2030,SC2031,SC2034,SC2086,SC2154,SC2155,SC2162,SC2231 \
	  util/release \
	  util/release-repo.bash \
	  util/release-binding-published \
	  util/install-release-artifacts \
	  util/release-go \
	  util/release-fortran \
	  util/release-delphi \
	  util/release-crystal \
	  util/release-lua \
	  util/release-php
endif

$(ALL-TESTS):
	@echo '--------------------------------------------------'
	@echo '   $@'
	@echo '--------------------------------------------------'
	$(TEST-TIME) $(MAKE) -C $(@:test-%=%) test v=$v

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
ifdef v
export YS_RELEASE_VERBOSE := 1
endif
ifdef d
export YS_RELEASE_DRYRUN := 1
endif
ifdef n
export YS_RELEASE_VERSION_NEW := $n
endif
ifdef o
export YS_RELEASE_VERSION_OLD := $o
endif
export YAMLSTAR_ROOT := $(ROOT)

RELEASE-TARGETS := \
  version-bump \
  check-version \
  check-tag \
  release-lib \
  release-cli \
  release-github \
  check-release \

$(RELEASE-TARGETS):
	util/release $@

# Legacy target for backward compatibility
release-tag-legacy:
	util/release release-tag

RELEASE-BINDINGS := $(BINDING-LANGS:%=release-%)

$(filter-out release-perl, $(RELEASE-BINDINGS)): $(GH)
	$(MAKE) -C $(@:release-%=%) release

release-perl:
	$(MAKE) -C perl release-cpan

ifneq (,$(or $s,$(YS_RELEASE_ID),$(YS_RELEASE_NO_CHECK)))
release: _release-yamlstar
else
release: release-check release-pull _release-yamlstar
endif

release-check:
ifndef YS_RELEASE_NO_CHECK
ifneq (,$(filter-out main release-automation,$(shell git rev-parse --abbrev-ref HEAD)))
ifndef YS_RELEASE_ALLOW_BRANCH
	$(error Must be on branch 'main' to release)
endif
endif
ifndef d
ifeq (,$(RELEASE-AUTH))
	$(error YAMLStar release requires GH_TOKEN, GITHUB_TOKEN, or ~/.yamlstar-secrets.yaml)
endif
endif
endif
ifndef d
ifndef YS_RELEASE_VERSION_OLD
	$(error 'make release' needs the 'o' variable set to the old version)
endif
ifndef YS_RELEASE_VERSION_NEW
	$(error 'make release' needs the 'n' variable set to the new version)
endif
endif

release-pull:
ifndef d
	( \
	  set -ex; \
	  head=$$(git rev-parse HEAD); \
	  git pull --rebase; \
	  if [[ $$(git rev-parse HEAD) != $$head ]]; then \
	    echo "Pulled new changes. Please re-run 'make release'."; \
	    exit 1; \
	  fi \
	)
endif

_release-yamlstar: $(YS) $(GH)
	($(TIME) $(YS) $(ROOT)/util/release-yamlstar release $o $n $s) 2>&1 | \
	  tee -a $(RELEASE-LOG)

release-list: $(YS)
	$(YS) $(ROOT)/util/release-yamlstar list

release-sanity-check: $(YS)
ifndef o
	$(error 'make release-sanity-check' requires o=OLD_VERSION n=NEW_VERSION)
endif
ifndef n
	$(error 'make release-sanity-check' requires o=OLD_VERSION n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/release-yamlstar sanity-check $(o) $(n)

release-secrets-update secrets-update: $(YS) $(GH)
	$(YS) $(ROOT)/util/yamlstar-secrets --update=$(or $(SECRETS),all)

release-secrets-list secrets-list: $(YS)
	$(YS) $(ROOT)/util/yamlstar-secrets --list

release-secrets-publish secrets-publish: $(YS) $(GH)
	$(YS) $(ROOT)/util/yamlstar-secrets --publish

release-version-bump: $(YS)
	$(YS) $(ROOT)/util/release-yamlstar version-bump

release-changelog: $(YS)
ifndef o
	$(error 'make release-changelog' requires o=OLD_VERSION n=NEW_VERSION)
endif
ifndef n
	$(error 'make release-changelog' requires o=OLD_VERSION n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/release-yamlstar changelog $(o) $(n)

release-binding-changelogs: $(YS)
	$(YS) $(ROOT)/util/release-yamlstar binding-changelogs

release-commit: $(YS)
ifndef n
	$(error 'make release-commit' requires n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/release-yamlstar commit $(n)

release-tag: $(YS)
ifndef n
	$(error 'make release-tag' requires n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/release-yamlstar tag $(n)

release-push: $(YS)
ifndef n
	$(error 'make release-push' requires n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/release-yamlstar push $(n)

release-build-github: $(YS)
ifndef n
	$(error 'make release-build-github' requires n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/release-yamlstar build-github $(n)

# The t= platform list accepts spaces or commas:
#   t='macos linux'  or  t=macos,linux
comma := ,

# Rerun tests for the platforms in t= using build artifacts from a
# prior run (r=RUN_ID, default: the latest release workflow run on
# the current branch). Example:
#   make release-tests-retry n=0.1.15 t=macos r=12345678
release-tests-retry: t ?= linux macos windows
release-tests-retry: $(GH)
ifndef n
	$(error 'make release-tests-retry' requires n=NEW_VERSION)
endif
	@set -e; \
	  branch=$$(git branch --show-current); \
	  artifact_run_id='$(r)'; \
	  if [[ -z "$$artifact_run_id" ]]; then \
	    artifact_run_id=$$(gh run list --workflow=release.yaml \
	      --repo yaml/yamlstar --branch $$branch --limit=1 \
	      --json databaseId --jq '.[0].databaseId'); \
	  fi; \
	  test -n "$$artifact_run_id"; \
	  gh run view $$artifact_run_id --repo yaml/yamlstar \
	    --json databaseId --jq .databaseId > /dev/null || { \
	    echo "ERROR: run id '$$artifact_run_id' not found"; exit 1; }; \
	  echo "Using build artifacts from run $$artifact_run_id"; \
	  git push origin HEAD:$$branch; \
	  gh workflow run release.yaml \
	    --repo yaml/yamlstar --ref $$branch -f version=$(n) \
	    -f tests_only='$(subst $(comma), ,$(t))' \
	    -f test_artifacts_run_id=$$artifact_run_id; \
	  sleep 5; \
	  run_id=$$(gh run list --workflow=release.yaml \
	    --repo yaml/yamlstar --branch $$branch --limit=1 \
	    --json databaseId --jq '.[0].databaseId'); \
	  gh run watch $$run_id --repo yaml/yamlstar \
	    --exit-status --interval=10

release-retry: $(YS) $(GH)
ifndef n
	$(error 'make release-retry' requires n=NEW_VERSION)
endif
	@if gh release view $(n) --repo yaml/yamlstar >/dev/null 2>&1; then \
	  echo "Deleting existing GitHub release $(n)"; \
	  gh release delete $(n) --repo yaml/yamlstar --yes; \
	fi
	git push origin HEAD:$$(git branch --show-current)
	git tag -f $(n) HEAD
	git push -f origin $(n)
	$(MAKE) release-build-github n=$(n)

release-bindings: $(YS)
ifndef n
	$(error 'make release-bindings' requires n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/release-yamlstar bindings $(n)

release-homebrew: $(YS)
ifndef n
	$(error 'make release-homebrew' requires n=NEW_VERSION)
endif
	$(YS) $(ROOT)/util/brew-update $(n)

release-website: $(YS)
	$(YS) $(ROOT)/util/release-yamlstar website

release-publish-homebrew: $(GH)
ifndef n
	$(error 'make release-publish-homebrew' requires n=NEW_VERSION)
endif
	@set -e; \
	  branch=$$(git branch --show-current); \
	  git push origin HEAD:$$branch; \
	  gh workflow run release.yaml \
	    --repo yaml/yamlstar --ref $$branch -f version=$(n) \
	    -f publish_homebrew_only=true; \
	  sleep 5; \
	  run_id=$$(gh run list --workflow=release.yaml \
	    --repo yaml/yamlstar --branch $$branch --limit=1 \
	    --json databaseId --jq '.[0].databaseId'); \
	  gh run watch $$run_id --repo yaml/yamlstar \
	    --exit-status --interval 10

release-publish-bindings: $(GH)
ifndef n
	$(error 'make release-publish-bindings' requires n=NEW_VERSION)
endif
	@set -e; \
	  branch=$$(git branch --show-current); \
	  git push origin HEAD:$$branch; \
	  gh workflow run release.yaml \
	    --repo yaml/yamlstar --ref $$branch -f version=$(n) \
	    -f publish_bindings_only=true \
	    -f force_bindings='$(if $(YS_RELEASE_FORCE_BINDINGS),true,false)' \
	    -f bindings='$(YS_RELEASE_BINDINGS)' \
	    -f bindings_skip='$(YS_RELEASE_BINDINGS_SKIP)'; \
	  sleep 5; \
	  run_id=$$(gh run list --workflow=release.yaml \
	    --repo yaml/yamlstar --branch $$branch --limit=1 \
	    --json databaseId --jq '.[0].databaseId'); \
	  gh run watch $$run_id --repo yaml/yamlstar \
	    --exit-status --interval=10

publish-python-wheels: $(GH)
ifndef n
	$(error 'make publish-python-wheels' requires n=VERSION)
endif
	rm -fr python/dist
	mkdir -p python/dist/release-assets
	gh release download $(n) \
	  --repo yaml/yamlstar \
	  --pattern 'libyamlstar-$(n)-*.tar.xz' \
	  --dir python/dist/release-assets
	$(MAKE) -C python wheels-from-release n=$(n)
	$(MAKE) -C python publish-wheels

.PHONY: cli core libyamlstar test
