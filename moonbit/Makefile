include ../common/init.mk
include $M/clean.mk
include $M/moonbit.mk
include $(COMMON)/common.mk

MOONBIT-RELEASE-VERSION := $(or $(YS_RELEASE_VERSION_NEW),$n)

ifeq ($(OS-NAME),windows)
override export PATH := $(ROOT)/libyamlstar/lib:$(PATH)
endif

ifeq ($(OS-NAME),macos)
export DYLD_LIBRARY_PATH := $(ROOT)/libyamlstar/lib:$(DYLD_LIBRARY_PATH)
endif


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(MOON)
	moon test --target native

release: build-doc $(MOON)
ifeq (,$(MOONBIT-RELEASE-VERSION))
	@echo "Error: release version not set"
	@exit 1
endif
	moon check --target native
	$(call moon-publish)

clean::
	$(RM) -r _build target

define moon-publish
	@user="$$MOONCAKES_USERNAME"; \
	token="$$MOONCAKES_TOKEN"; \
	secrets="$${YS_SECRETS_FILE:-}"; \
	if [[ -z "$$secrets" && -r "$$HOME/.yamlstar-secrets" ]]; then \
	  secrets="$$HOME/.yamlstar-secrets"; \
	fi; \
	secrets="$${secrets:-$$HOME/.yamlstar-secrets.yaml}"; \
	if [[ -r "$$secrets" ]]; then \
	  user="$${user:-$$($(YS) -l "$$secrets" -e '.moonbit.user:say')}"; \
	  token="$${token:-$$($(YS) -l "$$secrets" -e '.moonbit.token:say')}"; \
	fi; \
	if [[ -n "$$user$$token" ]]; then \
	  test -n "$$user" || { \
	    echo "Error: MOONCAKES_USERNAME not set"; exit 1; }; \
	  test -n "$$token" || { \
	    echo "Error: MOONCAKES_TOKEN not set"; exit 1; }; \
	  mkdir -p "$$MOON_HOME"; \
	  printf '{"username":"%s","token":"%s"}\n' \
	    "$$user" "$$token" > "$$MOON_HOME/credentials.json"; \
	fi
	moon publish
endef
