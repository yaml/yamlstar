include ../common/init.mk

# Load platform tools before common.mk (which includes shell.mk last)
# Avoids graalvm.mk/java.mk conflict when both platforms are present
ifdef YAMLSTAR_GLOJURE
include $M/gloat.mk
else
include $M/graalvm.mk
include $M/lein.mk
endif

include ../common/common.mk

ifndef YAMLSTAR_GLOJURE
include ../common/native.mk
endif

MAKES-CLEAN := \
  graalvm/target \
  graalvm/.lein-* \
  cache/gloat-srcs \
  lib/ \

MAKES-REALCLEAN := \
  lib \

# Library paths and names
SO ?= so
LIB-DIR := lib
LIB-NAME := libyamlstar
LIB-NAME-GLOAT := libyamlstarglj
LIB-SO-GLOAT := $(LIB-DIR)/$(LIB-NAME-GLOAT).$(SO)
LIB-SO := $(LIB-DIR)/$(LIB-NAME).$(SO)

CORE-YAMLSTAR := ../core/src/yamlstar

# .cljc source files that need .clj symlinks for gloat
GLOAT-LINK-DIR := cache/gloat-srcs

$(GLOAT-LINK-DIR)/parser/prelude.clj: $(CORE-YAMLSTAR)/parser/prelude.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/parser/parser.clj: $(CORE-YAMLSTAR)/parser/parser.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/parser/receiver.clj: $(CORE-YAMLSTAR)/parser/receiver.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

$(GLOAT-LINK-DIR)/constructor.clj: $(CORE-YAMLSTAR)/constructor.cljc
	@mkdir -p $(dir $@)
	ln -sf $(abspath $<) $@

GLOAT-LINKS := \
  $(GLOAT-LINK-DIR)/parser/prelude.clj \
  $(GLOAT-LINK-DIR)/parser/parser.clj \
  $(GLOAT-LINK-DIR)/parser/receiver.clj \
  $(GLOAT-LINK-DIR)/constructor.clj \

# Clojure source files compiled by gloat (order matters for namespaces)
GLOAT-SRCS := \
  $(GLOAT-LINK-DIR)/parser/prelude.clj \
  $(GLOAT-LINK-DIR)/parser/parser.clj \
  $(GLOAT-LINK-DIR)/parser/receiver.clj \
  $(CORE-YAMLSTAR)/parser/grammar.clj \
  $(CORE-YAMLSTAR)/parser.clj \
  $(CORE-YAMLSTAR)/composer.clj \
  $(CORE-YAMLSTAR)/resolver.clj \
  $(GLOAT-LINK-DIR)/constructor.clj \
  $(CORE-YAMLSTAR)/core.clj \
  gloat/src/libyamlstar.clj \

# GraalVM build
LIBYAMLSTAR-VERSION := $(VERSION)
LIB-JAR := graalvm/target/libyamlstar-$(LIBYAMLSTAR-VERSION)-SNAPSHOT-standalone.jar
CORE-JAR := $(MAVEN-REPOSITORY)/yamlstar/core/$(LIBYAMLSTAR-VERSION)-SNAPSHOT/core-$(LIBYAMLSTAR-VERSION)-SNAPSHOT.jar

# --- TEST-WITH-GLOJURE support ---
ifdef TEST-WITH-GLOJURE

_twg-patch: $(GO) | $(GLOAT-DIR)
	@env -u MAKES_LOCAL_DIR make --no-print-directory -C $(GLOAT-DIR) path-deps
	@echo "Building glj from $(TEST-WITH-GLOJURE)..."
	@cd $(TEST-WITH-GLOJURE) && $(GO) build -o $(GLOAT-DIR)/.cache/.local/bin/glj ./cmd/glj
	@grep -q 'replace.*gloathub/glojure' \
	    $(GLOAT-DIR)/template/go.mod 2>/dev/null || \
	  ( echo '' >> $(GLOAT-DIR)/template/go.mod && \
	    echo 'replace github.com/gloathub/glojure => $(TEST-WITH-GLOJURE)' \
	      >> $(GLOAT-DIR)/template/go.mod && \
	    echo "Patched template/go.mod" )
	@grep -q 'replace.*gloathub/glojure' \
	    $(GLOAT-DIR)/ys/pkg/go.mod 2>/dev/null || \
	  ( echo '' >> $(GLOAT-DIR)/ys/pkg/go.mod && \
	    echo 'replace github.com/gloathub/glojure => $(TEST-WITH-GLOJURE)' \
	      >> $(GLOAT-DIR)/ys/pkg/go.mod && \
	    echo "Patched ys/pkg/go.mod" )

$(LIB-SO-GLOAT): _twg-patch

endif
# --- end TEST-WITH-GLOJURE ---


ifdef YAMLSTAR_GLOJURE
build: build-gloat
else
build: build-graalvm
endif

LIB-HEADER := $(LIB-DIR)/$(LIB-NAME).h
LIB-HEADER-GLOAT := $(LIB-DIR)/$(LIB-NAME-GLOAT).h

build-gloat: $(LIB-SO-GLOAT)
	ln -sf $(notdir $(LIB-SO-GLOAT)) $(LIB-DIR)/$(LIB-NAME-GLOAT).$(SO).$(VERSION)

build-graalvm: $(LIB-SO)
	ln -sf $(notdir $(LIB-SO)) $(LIB-DIR)/$(LIB-NAME).$(SO).$(VERSION)

# Build both: each requires separate make invocations (tool includes conflict)
build-all:
	YAMLSTAR_GLOJURE=1 $(MAKE) build-gloat
	env -u YAMLSTAR_GLOJURE $(MAKE) build-graalvm

test:

install: $(LIB-SO)
	mkdir -p $(PREFIX)/lib/
	install -m 644 $(LIB-SO) $(PREFIX)/lib/

$(LIB-SO-GLOAT): $(GLOAT-SRCS) $(GLOAT-LINKS) | $(GLOAT-DIR)
	@echo "Building gloat shared library..."
	mkdir -p $(LIB-DIR)
	</dev/null env -u GOROOT $(GLOAT-BIN)/gloat $(GLOAT-SRCS) -o $@
	@echo "Gloat shared library created at: $@"

$(CORE-JAR):
	$(MAKE) -C ../core install

$(LIB-JAR): $(CORE-JAR) $(LEIN)
	cd graalvm && lein uberjar

$(LIB-SO): $(LIB-JAR) $(REFLECTION-JSON) $(GRAALVM)
	@echo "Building GraalVM shared library..."
	mkdir -p $(LIB-DIR)
	$(GRAALVM) \
	  $(NATIVE-OPTS) \
	  --shared \
	  -jar $< \
	  -o $(LIB-DIR)/$(LIB-NAME)
	@echo "GraalVM shared library created at: $@"
	@echo "Header: $(LIB-HEADER)"

# graal_isolate.h is a side effect of building the GraalVM SO
$(LIB-DIR)/graal_isolate.h: $(LIB-SO)
