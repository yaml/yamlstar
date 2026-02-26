include ../common/init.mk
include $M/gloat.mk
include ../common/common.mk

MAKES-CLEAN := \
  lib \

# Library paths and names
LIB-DIR := lib
LIB-NAME := $(LIB-DIR)/libyamlstar
LIB-SO := $(LIB-NAME).$(SO)
LIB-HEADER := $(LIB-NAME).h

CORE-YAMLSTAR := ../core/src/yamlstar

# Clojure source files compiled by gloat (order matters for namespaces)
GLOAT-SRCS := \
  $(CORE-YAMLSTAR)/parser/prelude.clj \
  $(CORE-YAMLSTAR)/parser/parser.clj \
  $(CORE-YAMLSTAR)/parser/receiver.clj \
  $(CORE-YAMLSTAR)/parser/grammar.clj \
  $(CORE-YAMLSTAR)/parser.clj \
  $(CORE-YAMLSTAR)/composer.clj \
  $(CORE-YAMLSTAR)/resolver.clj \
  $(CORE-YAMLSTAR)/constructor.clj \
  $(CORE-YAMLSTAR)/core.clj \
  src/libyamlstar.clj \


build: $(LIB-SO)

test:

install: $(LIB-SO)
	mkdir -p $(PREFIX)/lib/
	install -m 644 $(LIB-SO) $(PREFIX)/lib/
	install -m 644 $(LIB-HEADER) $(PREFIX)/include/ 2>/dev/null || true

$(LIB-SO): $(GLOAT-SRCS) | $(GLOAT-DIR)
	@echo "Building shared library with gloat..."
	mkdir -p $(LIB-DIR)
	env -u GOROOT $(GLOAT-BIN)/gloat $(GLOAT-SRCS) -o $@
	@echo "Shared library created at: $@"
