include ../common/init.mk
include $M/gloat.mk
include ../common/common.mk

MAKES-CLEAN := \
  lib/ \
  target/ \

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


# --- TEST-WITH-GLOJURE support ---
# Usage: make build TEST-WITH-GLOJURE=/path/to/glojure
# Rebuilds glj from local source and patches go.mod to use
# local glojure runtime. Works from clean slate (after distclean).
ifdef TEST-WITH-GLOJURE

_twg-patch: $(GO) | $(GLOAT-DIR)
	@# Bootstrap gloat's own deps (bb, glj, go, ys) into its internal cache.
	@# Unset MAKES_LOCAL_DIR so gloat always installs to its own .cache/.local/,
	@# matching what the gloat script expects when it checks for bb on startup.
	@env -u MAKES_LOCAL_DIR make --no-print-directory -C $(GLOAT-DIR) path-deps
	@# Overwrite the released glj with our custom build.
	@# When gloat runs, setup() sees bb already present and returns early,
	@# so path-deps is skipped and our custom glj is preserved.
	@echo "Building glj from $(TEST-WITH-GLOJURE)..."
	@cd $(TEST-WITH-GLOJURE) && $(GO) build -o $(GLOAT-DIR)/.cache/.local/bin/glj ./cmd/glj
	@# Patch go.mod template (idempotent)
	@grep -q 'replace.*gloathub/glojure' \
	    $(GLOAT-DIR)/template/go.mod 2>/dev/null || \
	  ( echo '' >> $(GLOAT-DIR)/template/go.mod && \
	    echo 'replace github.com/gloathub/glojure => $(TEST-WITH-GLOJURE)' \
	      >> $(GLOAT-DIR)/template/go.mod && \
	    echo "Patched template/go.mod" )
	@# Patch ys/pkg/go.mod (idempotent)
	@grep -q 'replace.*gloathub/glojure' \
	    $(GLOAT-DIR)/ys/pkg/go.mod 2>/dev/null || \
	  ( echo '' >> $(GLOAT-DIR)/ys/pkg/go.mod && \
	    echo 'replace github.com/gloathub/glojure => $(TEST-WITH-GLOJURE)' \
	      >> $(GLOAT-DIR)/ys/pkg/go.mod && \
	    echo "Patched ys/pkg/go.mod" )

# Wire as prerequisite of the main build
$(LIB-SO): _twg-patch

endif
# --- end TEST-WITH-GLOJURE ---

build: $(LIB-SO)

test:

install: $(LIB-SO)
	mkdir -p $(PREFIX)/lib/
	install -m 644 $(LIB-SO) $(PREFIX)/lib/
	install -m 644 $(LIB-HEADER) $(PREFIX)/include/ 2>/dev/null || true

$(LIB-SO): $(GLOAT-SRCS) | $(GLOAT-DIR)
	@echo "Building shared library with gloat..."
	mkdir -p $(LIB-DIR)
	</dev/null env -u GOROOT $(GLOAT-BIN)/gloat $(GLOAT-SRCS) -o $@
	@echo "Shared library created at: $@"
