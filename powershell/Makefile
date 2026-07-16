include ../common/init.mk
include $M/clean.mk
include $M/dotnet.mk
ifneq (,$(wildcard $M/powershell.mk))
include $M/powershell.mk
endif
include $(COMMON)/common.mk

export DOTNET_CLI_HOME := $(ROOT)/.cache/dotnet
PWSH ?= pwsh

PSGALLERY-API-KEY ?= $(PSGALLERY_API_KEY)
ifeq (,$(PSGALLERY-API-KEY))
PSGALLERY-API-KEY := \
  $(shell \
    file=~/.yamlscript-secrets.yaml; \
    value=$$(ys -e '.powershell.token:say' $$file 2>/dev/null); \
    [[ "$$value" != nil ]] && printf '%s' "$$value")
endif

PUBLISH-DIR := .publish
MODULE-DIR := $(PUBLISH-DIR)/YAMLStar
MODULE-FILES := YAMLStar.psd1 YAMLStar.psm1 ReadMe.md

#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION)
	@command -v $(PWSH) >/dev/null || \
	  { echo "'$(PWSH)' is required to test the PowerShell binding" >&2; exit 2; }
	$(PWSH) -NoLogo -NoProfile -File test/test-yamlstar.ps1

release:: $(DOTNET) build-doc
ifndef PSGALLERY-API-KEY
	$(error Can't determine PowerShell Gallery API key)
endif
	@command -v $(PWSH) >/dev/null || \
	  { echo "'$(PWSH)' is required to publish the PowerShell binding" >&2; exit 2; }
	$(RM) -r $(PUBLISH-DIR)
	mkdir -p $(MODULE-DIR)
	cp $(MODULE-FILES) $(MODULE-DIR)/
	@$(PWSH) -NoLogo -NoProfile -Command \
	  "Publish-Module -Path $(MODULE-DIR) -Repository PSGallery \
	    -NuGetApiKey '$(PSGALLERY-API-KEY)' -Force"

clean::
	$(RM) -r $(PUBLISH-DIR)
