include ../common/init.mk
include $M/node.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

NODE-MODULES := node_modules

MAKES-CLEAN := \
  .npmrc \
  package-lock.json \

MAKES-REALCLEAN := \
  $(NODE-MODULES) \


test: $(NODE-MODULES) $(LIBYAMLSTAR-SO)
	node test/test.js

build: $(LIBYAMLSTAR-SO)

node_modules: package.json $(NODE)
	npm install

release-deps:: $(NODE)
