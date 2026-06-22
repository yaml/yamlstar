include ../common/init.mk
include $M/julia.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

HEAD-COMMIT := $(shell git rev-parse HEAD)
export JULIA_DEPOT_PATH := $(ROOT)/.cache/julia

YAMLSTAR_GH_TOKEN ?= $(GH_TOKEN)
ifeq (,$(YAMLSTAR_GH_TOKEN))
YAMLSTAR_GH_TOKEN := $(GITHUB_TOKEN)
endif

build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-FQNP) deps $(JULIA)
	julia --project=. test/runtests.jl

deps:: $(JULIA)
	julia --project=. -e 'using Pkg; Pkg.add("JSON")'

release-deps:: $(JULIA)

repl: $(JULIA)
	julia --project=.

clean::
	$(RM) Manifest.toml
