include ../common/init.mk
include $M/d.mk
include $M/yamlscript.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

export DUB_HOME := $(ROOT)/.cache/dub
export YS

DUB := $(LDC-BIN)/dub


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(LDC)
	dub test

release-deps:: $(YS)

clean::
	$(RM) -r .dub dub.selections.json yamlstar-test-library
