M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/git.mk
include $M/clean.mk

ROOT := $(GIT-REPO-DIR)
COMMON := $(ROOT)/common
LIBYS := $(ROOT)/libyamlstar
