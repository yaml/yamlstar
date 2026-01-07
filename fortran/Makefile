include ../common/init.mk
include $M/fortran.mk
include $(COMMON)/common.mk

MAKES-CLEAN := \
  build \


test: $(FPM) $(LIBYAMLSTAR-SO)
	fpm test --flag "-L$(LIBYS)/lib"

build: $(FPM) $(LIBYAMLSTAR-SO)
	fpm build --flag "-L$(LIBYS)/lib"
