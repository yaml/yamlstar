include ../common/init.mk
include $M/fortran.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

MAKES-CLEAN := \
  build \

DEPS := \
  $(FPM) \
  $(LIBYAMLSTAR-SO) \


test: $(DEPS)
	fpm test --flag "-L$(LIBYS)/lib"

build: $(DEPS)
	fpm build --flag "-L$(LIBYS)/lib"
