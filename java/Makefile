include ../common/init.mk
include $M/lein.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

MAKES-CLEAN := \
  target/ \
  pom.xml \
  .lein-failures \


test install deploy:: $(LEIN)
	lein $@

release-deps:: $(LEIN)
