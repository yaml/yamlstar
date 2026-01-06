M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/node.mk
include $M/clean.mk

LIBYAMLSTAR_SO := ../libyamlstar/lib/libyamlstar.so

NODE-MODULES := node_modules

MAKES-CLEAN := \
  package-lock.json \

MAKES-REALCLEAN := \
  $(NODE-MODULES) \


test: $(NODE-MODULES) $(LIBYAMLSTAR_SO)
	node test/test.js

build: $(LIBYAMLSTAR_SO)

node_modules: package.json $(NODE)
	npm install

$(LIBYAMLSTAR_SO):
	$(MAKE) -C ../libyamlstar build
