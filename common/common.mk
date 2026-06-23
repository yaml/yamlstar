include $M/shell.mk
include $(COMMON)/backend.mk

VERSION := $(shell grep '^version:' $(ROOT)/Meta | cut -d' ' -f2)
LIBYAMLSTAR-VERSION := $(VERSION)
LIBYAMLSTAR-SO := $(LIBYS)/lib/libyamlstar.$(SO)
LIBYAMLSTAR-SO-VERSION := $(LIBYS)/lib/libyamlstar.$(SO).$(LIBYAMLSTAR-VERSION)
LIBYAMLSTAR-HEADER := $(LIBYS)/lib/libyamlstar.h

export LD_LIBRARY_PATH := $(ROOT)/libyamlstar/lib:$(LD_LIBRARY_PATH)
export DYLD_LIBRARY_PATH := $(ROOT)/libyamlstar/lib:$(DYLD_LIBRARY_PATH)

select-libyamlstar-backend: validate-backend
	$(MAKE) -C $(LIBYS) select-backend \
	  YAMLSTAR_JVM='$(YAMLSTAR_JVM)' \
	  YAMLSTAR_GLJ='$(YAMLSTAR_GLJ)' \
	  YAMLSTAR_CPP='$(YAMLSTAR_CPP)'

$(LIBYAMLSTAR-SO) $(LIBYAMLSTAR-SO-VERSION) $(LIBYAMLSTAR-HEADER): select-libyamlstar-backend
	@true
