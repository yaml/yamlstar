include ../common/init.mk
include $M/clean.mk
ifneq (,$(wildcard $M/alire.mk))
include $M/alire.mk
endif
include $M/gh.mk
include $M/gnat.mk
include $(COMMON)/common.mk

MAKES-CLEAN += alire

ADA-ORIGIN = \
  https://github.com/yaml/yamlstar/archive/refs/tags/$(n).tar.gz
ADA-MANIFEST := ada/alire.toml
ADA-ALIRE-INDEX-REPO := alire-project/alire-index
ADA-ALIRE-INDEX-BRANCH = release/yamlstar-$(n)
ADA-GITHUB-USER ?= $(or $(GITHUB_ACTOR),\
  $(shell ys -e '.github.user:say' ~/.yamlscript-secrets.yaml 2>/dev/null))
ADA-GITHUB-TOKEN ?= $(or $(GH_TOKEN),$(GITHUB_TOKEN),\
  $(shell ys -e '.github.token:say' ~/.yamlscript-secrets.yaml 2>/dev/null))
ALR ?= $(shell command -v alr 2>/dev/null)

#------------------------------------------------------------------------------
build:: build-doc obj/yamlstar.o

build-doc:: ReadMe.md

obj/yamlstar.o: src/yamlstar.adb src/yamlstar.ads | obj
	gnatmake -c -Dobj -Isrc src/yamlstar.adb

test:: $(LIBYAMLSTAR-SO-VERSION) $(GNATMAKE) | obj
	gnatmake -Dobj -Isrc test/test_yamlstar.adb \
	    -o test/test_yamlstar \
	    -largs -L$(ROOT)/libyamlstar/lib -lyamlstar
	test/test_yamlstar

release:: $(ALR) $(GH) test build-doc
ifndef ALR
	$(error 'alr' is required to publish the Ada binding)
endif
ifndef n
	$(error 'make $@' needs the n variable set to the new version)
endif
ifndef ADA-GITHUB-USER
	$(error Can't determine GitHub user)
endif
ifndef ADA-GITHUB-TOKEN
	$(error Can't determine GitHub token)
endif
	mkdir -p $(XDG_RUNTIME_DIR)
	$(ALR) settings --global --set user.github_login $(ADA-GITHUB-USER)
	@GH_TOKEN=$(ADA-GITHUB-TOKEN) \
	  $(ALR) publish --manifest $(ADA-MANIFEST) $(ADA-ORIGIN)
	@set -eu; \
	  pr_data=$$(GH_TOKEN=$(ADA-GITHUB-TOKEN) $(GH) pr list \
	    --repo $(ADA-ALIRE-INDEX-REPO) \
	    --state open \
	    --author $(ADA-GITHUB-USER) \
	    --head $(ADA-ALIRE-INDEX-BRANCH) \
	    --json number,isDraft \
	    --jq 'if length == 1 then "\(.[0].number) \(.[0].isDraft)" else empty end'); \
	  set -- $$pr_data; \
	  if [ "$$#" -ne 2 ]; then \
	    echo "ERROR: Expected one open Alire index PR for $(ADA-ALIRE-INDEX-BRANCH)" >&2; \
	    exit 1; \
	  fi; \
	  if [ "$$2" = true ]; then \
	    GH_TOKEN=$(ADA-GITHUB-TOKEN) $(GH) pr ready "$$1" \
	      --repo $(ADA-ALIRE-INDEX-REPO); \
	  else \
	    echo "Alire index PR #$$1 is already ready for review"; \
	  fi

obj:
	mkdir -p $@

clean::
	$(RM) -r obj test/test_yamlstar *.ali *.o
