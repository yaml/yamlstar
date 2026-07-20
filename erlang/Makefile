include ../common/init.mk
include $M/clean.mk
PERL := /usr/bin/perl
export PERL

ifneq (,$(wildcard /.dockerenv))
ERLANG-DOCKER-INNER := true
ERL := erl
ERLC := erlc
ERLANG-DEPS :=
ERLANG-ROOT := $(shell erl -noshell \
  -eval 'io:format("~s", [code:root_dir()]).' -s init stop)
ERLANG-INCLUDE := $(ERLANG-ROOT)/usr/include
else
ifneq (,$(wildcard $M/rebar3.mk))
include $M/rebar3.mk
endif
include $M/erlang.mk
ERLANG-DEPS := $(ERL)
ERLANG-INCLUDE := $(ERLANG-LOCAL)/lib/erlang/usr/include
endif

include $(COMMON)/common.mk

export REBAR_CACHE_DIR := $(ROOT)/.cache/rebar3
REBAR3 ?= rebar3

HEX-API-KEY ?= $(HEX_API_KEY)
ifeq (,$(HEX-API-KEY))
HEX-API-KEY := \
  $(shell ys -e '.hex.token:say' ~/.yamlscript-secrets.yaml 2>/dev/null)
endif

ERLANG-PRIV := priv
ERLANG-NIF := $(ERLANG-PRIV)/yamlstar_nif.$(SO)
NIF-CFLAGS := -DLIBYAMLSTAR_VERSION=\"$(LIBYAMLSTAR-VERSION)\"
ERLANG-IMAGE ?= ghcr.io/yaml/yamlscript-elixir-test
GIT-COMMON-DIR := $(shell git rev-parse --path-format=absolute \
  --git-common-dir 2>/dev/null)
GIT-DOCKER-VOLUME := $(if $(GIT-COMMON-DIR),--volume \
  $(GIT-COMMON-DIR):$(GIT-COMMON-DIR):ro,)
ERLANG-DOCKER-OUTER := $(and \
  $(IS-LINUX), \
  $(IS-INTEL), \
  $(if $(ERLANG-DOCKER-INNER),,x), \
  $(shell command -v docker))

ifeq ($(OS-NAME),macos)
NIF-LDFLAGS := -bundle -undefined dynamic_lookup
else
NIF-LDFLAGS := -shared
endif


#------------------------------------------------------------------------------
build:: build-doc $(ERLANG-NIF) ebin/yamlstar.beam

build-doc:: ReadMe.md

ifneq (,$(ERLANG-DOCKER-OUTER))
test:: $(LIBYAMLSTAR-SO-VERSION) docker-image
	docker run --rm \
	  --user $(USER-UID):$(USER-GID) \
	  --env HOME=/tmp/home \
	  --volume $(ROOT):$(ROOT) \
	  $(GIT-DOCKER-VOLUME) \
	  --workdir $(CURDIR) \
	  $(ERLANG-IMAGE) \
	  make test

docker-image:
	docker image inspect $(ERLANG-IMAGE) > /dev/null 2>&1 || \
	docker pull $(ERLANG-IMAGE) 2> /dev/null || \
	$(MAKE) -C $(ROOT)/elixir docker-image
else
test:: $(LIBYAMLSTAR-SO-VERSION) $(ERLANG-DEPS) build
	erl -noshell -pa ebin -eval 'yamlstar_test:run().' -s init stop
endif

release:: build-doc
ifndef HEX-API-KEY
	$(error Can't determine Hex API key)
endif
	@command -v $(REBAR3) >/dev/null || \
	  { echo "'$(REBAR3)' is required to publish the Erlang binding" >&2; exit 2; }
	HEX_API_KEY=$(HEX-API-KEY) $(REBAR3) \
	  hex publish package --repo hexpm --yes

ebin/yamlstar.beam: src/yamlstar.erl | ebin
	erlc -o ebin $<

ebin/yamlstar_test.beam: test/yamlstar_test.erl ebin/yamlstar.beam
	erlc -pa ebin -o ebin $<

$(ERLANG-NIF): c_src/yamlstar_nif.c $(ERLANG-DEPS) | $(ERLANG-PRIV)
	$(CC) -fPIC $(NIF-LDFLAGS) $(NIF-CFLAGS) \
	  -I$(ERLANG-INCLUDE) -o $@ $< -ldl

ebin $(ERLANG-PRIV):
	mkdir -p $@

build:: ebin/yamlstar_test.beam

clean::
	$(RM) -r _build ebin priv
	$(RM) rebar.lock
