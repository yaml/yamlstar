include ../common/init.mk
include $M/fortran.mk
include $(COMMON)/common.mk

MAKES-CLEAN := \
  build \

test: $(FPM) $(GCC) $(LIBYAMLSTAR-SO)
	fpm test --flag "-L$(LIBYS)/lib"

build: $(FPM) $(GCC) $(LIBYAMLSTAR-SO)
	fpm build --flag "-L$(LIBYS)/lib"
