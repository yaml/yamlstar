include ../common/init.mk
LUA-VERSION := 5.4.8
export UV_CACHE_DIR := $(ROOT)/.cache/uv
include $M/lua.mk
include $M/luarocks.mk
include $M/python.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

LUA-RELEASE-REVISION ?= 1
LUA-API-VERSION := \
  $(word 1,$(subst ., ,$(LUA-VERSION))).$(word 2,$(subst ., ,$(LUA-VERSION)))
LUA-ROCKSPEC := yamlstar-$(VERSION)-$(LUA-RELEASE-REVISION).rockspec
LUA-MESON := $(PYTHON-VENV)/bin/meson
LUA-CFFI := $(LOCAL-LIB)/lua/$(LUA-API-VERSION)/cffi.so
LUA-CJSON := $(LOCAL-LIB)/lua/$(LUA-API-VERSION)/cjson.so
LUAROCKS-API-KEY ?= $(LUAROCKS_API_KEY)
ifeq (,$(LUAROCKS-API-KEY))
LUAROCKS-API-KEY := \
  $(shell ys -e '.lua.token:say' ~/.yamlstar-secrets.yaml 2>/dev/null)
endif

export LUA_PATH := $(ROOT)/lua/lib/?.lua;;
export LUA_CPATH := $(LOCAL-LIB)/lua/$(LUA-API-VERSION)/?.so;;

LUA-DEPS := \
  $(LUA) \
  $(LUAROCKS) \
  $(LIBYAMLSTAR-SO) \
  $(LUA-CFFI) \
  $(LUA-CJSON) \

test: $(LUA-DEPS)
	lua test/test_basic.lua

build: $(LUA-DEPS)

release-deps:: $(LUAROCKS)

$(LUA-ROCKSPEC): yamlstar.rockspec
	perl -pe 's/%VERSION%/$(VERSION)/g; s/%REVISION%/$(LUA-RELEASE-REVISION)/g;' $< > $@

$(LUA-CFFI): $(LUAROCKS) $(LUA-MESON)
	$(LUAROCKS) install cffi-lua MESON_BINDIR=$(PYTHON-VENV)/bin

$(LUA-CJSON): $(LUAROCKS) $(LUA-MESON)
	$(LUAROCKS) install lua-cjson MESON_BINDIR=$(PYTHON-VENV)/bin

$(LUA-MESON): $(PYTHON-VENV)
	uv pip install --python $(PYTHON-VENV) meson ninja

clean::
	$(RM) yamlstar-*.rock
	$(RM) yamlstar-*.rockspec
	$(RM) yamlstar-*.tar.gz
