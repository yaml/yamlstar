include $M/shell.mk

VERSION := $(shell grep '^version:' $(ROOT)/Meta | cut -d' ' -f2)
LIBYAMLSTAR-VERSION := $(VERSION)
LIBYAMLSTAR-SO := $(LIBYS)/lib/libyamlstar.$(SO)
LIBYAMLSTAR-SO-VERSION := $(LIBYS)/lib/libyamlstar.$(SO).$(LIBYAMLSTAR-VERSION)
LIBYAMLSTAR-HEADER := $(LIBYS)/lib/libyamlstar.h

export LD_LIBRARY_PATH := $(ROOT)/libyamlstar/lib:$(LD_LIBRARY_PATH)


$(LIBYAMLSTAR-SO) $(LIBYAMLSTAR-SO-VERSION) $(LIBYAMLSTAR-HEADER):
	$(MAKE) -C $(LIBYS) build
