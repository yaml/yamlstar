include ../common/init.mk
include $M/fortran.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

MAKES-CLEAN := \
  build \

# override LIBYAMLSTAR-SO :=


ifeq ($(OS-NAME),windows)
test: $(FPM) $(LIBYAMLSTAR-SO)
	PATH="$(LIBYS)/lib:$$PATH" fpm test --flag "-L$(LIBYS)/lib"
else
test: $(FPM) $(LIBYAMLSTAR-SO)
	fpm test --flag "-L$(LIBYS)/lib"
endif

ifeq ($(OS-NAME),windows)
build: $(FPM) $(LIBYAMLSTAR-SO)
	PATH="$(LIBYS)/lib:$$PATH" fpm build --flag "-L$(LIBYS)/lib"
else
build: $(FPM) $(LIBYAMLSTAR-SO)
	fpm build --flag "-L$(LIBYS)/lib"
endif
