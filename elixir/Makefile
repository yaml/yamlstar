include ../common/init.mk

# Inside the test container erlang, elixir and hex come from the image:
ifneq (,$(wildcard /.dockerenv))
ELIXIR-DOCKER-INNER := true
export MIX_HOME := /opt/mix
export HEX_HOME := /opt/hex
else
include $M/elixir.mk
export MIX_HOME := $(ROOT)/.cache/mix
export HEX_HOME := $(ROOT)/.cache/hex
endif

include $(COMMON)/common.mk

ELIXIR-IMAGE ?= ghcr.io/yaml/yamlscript-elixir-test
GIT-COMMON-DIR := $(shell git rev-parse --path-format=absolute --git-common-dir 2>/dev/null)
GIT-DOCKER-VOLUME := $(if $(GIT-COMMON-DIR),--volume $(GIT-COMMON-DIR):$(GIT-COMMON-DIR):ro,)

HEX-API-KEY ?= $(HEX_API_KEY)
ifeq (,$(HEX-API-KEY))
HEX-API-KEY := \
  $(shell ys -e '.hex.token:say' ~/.yamlstar-secrets.yaml 2>/dev/null)
endif


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

# Run the tests in the prebaked container when docker is available
# (locally and on CI Linux runners); erlang never compiles:
ifneq (,$(and $(if $(ELIXIR-DOCKER-INNER),,x),$(shell command -v docker)))
test:: $(LIBYAMLSTAR-SO-VERSION) docker-image
	docker run --rm \
	  --user $(USER-UID):$(USER-GID) \
	  --env HOME=/tmp/home \
	  --volume $(ROOT):$(ROOT) \
	  $(GIT-DOCKER-VOLUME) \
	  --workdir $(CURDIR) \
	  $(ELIXIR-IMAGE) \
	  make test

docker-image:
	docker image inspect $(ELIXIR-IMAGE) > /dev/null 2>&1 || \
	docker pull $(ELIXIR-IMAGE)

docker-image-build:
	$(error Elixir tests use the published $(ELIXIR-IMAGE) image)

docker-image-push: docker-image-build
	docker push $(ELIXIR-IMAGE)

release: export HEX_API_KEY := $(HEX-API-KEY)
release: docker-image
ifeq (,$(HEX-API-KEY))
	$(error Can't determine Hex API key)
endif
	docker run --rm \
	  --user $(USER-UID):$(USER-GID) \
	  --env HOME=/tmp/home \
	  --env HEX_API_KEY \
	  --volume $(ROOT):$(ROOT) \
	  $(GIT-DOCKER-VOLUME) \
	  --workdir $(CURDIR) \
	  $(ELIXIR-IMAGE) \
	  make release
else
test:: $(LIBYAMLSTAR-SO-VERSION) $(ERL) $(ELIXIR)
	mix local.hex --force --if-missing
	mix deps.get
	mix test

release: $(ERL) $(ELIXIR) README.md
ifeq (,$(HEX-API-KEY))
	$(error Can't determine Hex API key)
endif
	mix local.hex --force --if-missing
	mix deps.get
	HEX_API_KEY=$(HEX-API-KEY) mix hex.publish --yes
	$(RM) README.md
endif

README.md: ReadMe.md
	cp $< $@

clean::
	$(RM) -r _build deps priv README.md .docs
