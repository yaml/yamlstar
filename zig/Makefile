include ../common/init.mk
include $M/zig.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

export ZIG_GLOBAL_CACHE_DIR := $(ROOT)/.cache/zig


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(ZIG)
	zig build test

release-deps:: $(ZIG)

clean::
	$(RM) -r .zig-cache zig-out
