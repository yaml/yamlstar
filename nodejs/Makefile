include ../common/init.mk
include $M/node.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

NODE-MODULES := node_modules

MAKES-CLEAN := \
  package-lock.json \

MAKES-REALCLEAN := \
  $(NODE-MODULES) \


ifeq ($(OS-NAME),windows)
test: $(NODE-MODULES) $(LIBYAMLSTAR-SO)
	PATH="$(LIBYS)/lib:$$PATH" node test/test.js
else
test: $(NODE-MODULES) $(LIBYAMLSTAR-SO)
	node test/test.js
endif

build: $(LIBYAMLSTAR-SO)

node_modules: package.json $(NODE)
	npm install

release-deps:: $(NODE)
