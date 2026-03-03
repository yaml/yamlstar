# XXX Avoiding ingy's KVM bug here
ifeq (vm1,$(and $(filter ingy,$(USER)),$(shell hostname)))
INGY-LOCAL-DIR := /tmp/yamlstar-local
export MAKES_LOCAL_DIR := $(INGY-LOCAL-DIR)
endif

M ?= ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/git.mk
include $M/clean.mk

# Use --show-toplevel so worktrees resolve to their own path (not the main repo)
ROOT := $(shell git rev-parse --show-toplevel)
COMMON := ../common
LIBYS := ../libyamlstar
