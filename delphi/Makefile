include ../common/init.mk
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

FPC := $(shell command -v fpc 2>/dev/null)

test: $(DEPS)
	@if [ -z "$(FPC)" ]; then \
		echo "Error: Free Pascal Compiler (fpc) not found."; \
		echo "Please install FPC: https://www.freepascal.org/"; \
		exit 1; \
	fi
	fpc -Fusrc -Fl$(LIBYS)/lib -otest/test_yamlstar test/test_yamlstar.pas
	LD_LIBRARY_PATH=$(LIBYS)/lib test/test_yamlstar

build: $(DEPS)
	@if [ -z "$(FPC)" ]; then \
		echo "Error: Free Pascal Compiler (fpc) not found."; \
		echo "Please install FPC: https://www.freepascal.org/"; \
		exit 1; \
	fi
	fpc -Fusrc -Fl$(LIBYS)/lib src/yamlstar.pas

# Delphi/FPC has no standard package registry.
# The binding is distributed via GitHub releases alongside the shared library.
release::
	../util/release-delphi
