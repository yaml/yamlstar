include ../common/init.mk
include $M/raku.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

test ?= t/*

export RAKULIB=$(ROOT)/raku/lib
export LD_LIBRARY_PATH := $(ROOT)/libyamlstar/lib:$(LD_LIBRARY_PATH)

FEZ-UPLOAD = fez upload

ifdef YS_RELEASE_CI
FEZ-UPLOAD = timeout 60s fez upload --unattended --force || \
	echo 'Ignoring failed or timed out Raku fez upload'
endif

test:: $(RAKU) $(LIBYAMLSTAR-SO)
	prove $${TEST_VERBOSE:+'-v'} $(test)

raku-install: $(RAKU)
ifeq (,$(shell find $(LOCAL-TMP) | grep -F 'LibraryMake.rakumod'))
	zef install LibraryMake fez
endif

release-deps:: raku-install

dist::
	fez upload --dry-run

clean::
	$(RM) -r .precomp/ lib/.precomp/ sdist/
