include $M/shell.mk

LIBYAMLSTAR-VERSION := $(VERSION)-SNAPSHOT
LIBYAMLSTAR-SO := $(LIBYS)/lib/libyamlstar.so
LIBYAMLSTAR-SO-VERSION := $(LIBYS)/lib/libyamlstar.so.$(LIBYAMLSTAR-VERSION)
LIBYAMLSTAR-HEADER := $(LIBYS)/lib/libyamlstar.h

export LD_LIBRARY_PATH := $(ROOT)/libyamlstar/lib:$(LD_LIBRARY_PATH)


$(LIBYAMLSTAR-SO) $(LIBYAMLSTAR-SO-VERSION):
	$(MAKE) -C $(LIBYS) build
