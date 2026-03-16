include ../common/init.mk
include $M/fpc.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

MAKES-CLEAN := \
  *.o \
  *.ppu \
  src/*.o \
  src/*.ppu \
  test/*.o \
  test/*.ppu \
  test/test_yamlstar \

DEPS := \
  $(LIBYAMLSTAR-SO) \
  $(FPC) \

test: $(DEPS)
	fpc -Fusrc -Fl$(LIBYS)/lib -otest/test_yamlstar test/test_yamlstar.pas
	LD_LIBRARY_PATH=$(LIBYS)/lib test/test_yamlstar

build: $(DEPS)
	fpc -Fusrc -Fl$(LIBYS)/lib src/yamlstar.pas

# Delphi/FPC has no standard package registry.
# The binding is distributed via GitHub releases alongside the shared library.
release::
	../util/release-delphi
