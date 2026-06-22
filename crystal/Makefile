include ../common/init.mk
include $M/crystal.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

BUILD-DEPS := $(CRYSTAL) $(LIBYAMLSTAR-SO)

CRYSTAL-ENV := \
  CRYSTAL_CACHE_DIR=$(ROOT)/.cache/crystal \
  LIBRARY_PATH=$(ROOT)/libyamlstar/lib \
  LD_LIBRARY_PATH=$(ROOT)/libyamlstar/lib

build:: build-doc

build-doc:: ReadMe.md

test:: test-example test-crystal test-ffi

test-example: $(BUILD-DEPS)
	$(CRYSTAL-ENV) crystal run examples/simple.cr

test-crystal: $(BUILD-DEPS)
	$(CRYSTAL-ENV) crystal spec

test-ffi: $(BUILD-DEPS)
	$(CRYSTAL-ENV) crystal run test/ffi.cr

clean::
	$(RM) -r lib bin .crystal .shards shard.lock libyamlstar.so
