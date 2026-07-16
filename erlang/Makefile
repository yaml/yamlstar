include ../common/init.mk
include $M/clean.mk
PERL := /usr/bin/perl
export PERL

ifneq (,$(wildcard $M/rebar3.mk))
include $M/rebar3.mk
else
include $M/erlang.mk
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
ERLANG-INCLUDE := $(ERLANG-LOCAL)/lib/erlang/usr/include
NIF-CFLAGS := -DLIBYAMLSTAR_VERSION=\"$(LIBYAMLSTAR-VERSION)\"

ifeq ($(OS-NAME),macos)
NIF-LDFLAGS := -dynamiclib
else
NIF-LDFLAGS := -shared
endif


#------------------------------------------------------------------------------
build:: build-doc $(ERLANG-NIF) ebin/yamlstar.beam

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(ERL) build
	erl -noshell -pa ebin -eval 'yamlstar_test:run().' -s init stop

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

$(ERLANG-NIF): c_src/yamlstar_nif.c $(ERL) | $(ERLANG-PRIV)
	$(CC) -fPIC $(NIF-LDFLAGS) $(NIF-CFLAGS) \
	  -I$(ERLANG-INCLUDE) -o $@ $< -ldl

ebin $(ERLANG-PRIV):
	mkdir -p $@

build:: ebin/yamlstar_test.beam

clean::
	$(RM) -r _build ebin priv
	$(RM) rebar.lock
