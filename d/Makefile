include ../common/init.mk
include $M/d.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

export DUB_HOME := $(ROOT)/.cache/dub

DUB := $(LDC-BIN)/dub


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(LDC)
	dub test

release-deps::
	@true

clean::
	$(RM) -r .dub dub.selections.json yamlstar-test-library
