include ../common/init.mk
include $M/lein.mk
include $(COMMON)/binding.mk

MAKES-CLEAN =: \
  target/ \
  pom.xml \


test install deploy:: $(LEIN)
	lein $@

release-deps:: $(LEIN)
