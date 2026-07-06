# XXX Avoiding ingy's KVM bug here
ifeq (vm1,$(and $(filter ingy,$(USER)),$(shell hostname)))
INGY-LOCAL-DIR := /tmp/yamlstar-local
export MAKES_LOCAL_DIR := $(INGY-LOCAL-DIR)
endif

COMMON-INIT-DIR := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))

M ?= ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk

# Keep build helper scripts first, then prefer system tools over user-local
# shims that may not be executable in sandboxed build environments.
override PATH := $(MAKES)/util:/usr/local/bin:/usr/bin:/bin:$(PATH)
export PATH

ifneq (,$(shell command -v git))
include $M/git.mk
ROOT := $(shell git rev-parse --show-toplevel)
else
ROOT := $(abspath $(COMMON-INIT-DIR)/..)
endif
include $M/clean.mk

COMMON := $(COMMON-INIT-DIR)
LIBYS := $(ROOT)/libyamlstar
