include ../common/init.mk
include $M/cabal.mk
include $M/yamlscript.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

CABAL-INDEX := $(CABAL_DIR)/packages/hackage.haskell.org/01-index.tar.gz
LIBYAMLSTAR-TMP := /tmp/libyamlstar
HASKELL-DIST := dist-newstyle/sdist/yamlstar-$(VERSION).0.tar.gz
HASKELL-DEPS := $(GHC) $(CABAL) $(CABAL-INDEX) yamlstar.cabal License $(LIBYAMLSTAR-TMP)

HACKAGE-TOKEN ?= $(HACKAGE_TOKEN)
ifeq (,$(HACKAGE-TOKEN))
HACKAGE-TOKEN := \
  $(shell ys -e '(.haskell.token || .hackage.token):say' ~/.yamlstar-secrets.yaml 2>/dev/null)
endif

build:: build-doc

build-doc:: ReadMe.md

test:: test-cabal test-ffi

test-cabal: $(HASKELL-DEPS)
	cabal test --test-show-details=direct

test-ffi: $(HASKELL-DEPS)
	cabal run yamlstar-test -- 'key: value'

pkg-test: $(HASKELL-DEPS)
	$(MAKE) clean
	$(MAKE) test
	cabal sdist
	cabal exec -- cabal install $(HASKELL-DIST)

release-deps:: $(CABAL)

dist: $(HASKELL-DEPS)
	cabal check
	cabal sdist

check: $(HASKELL-DEPS)
	cabal $@

clean::
	$(RM) yamlstar.cabal License
	$(RM) -r dist-newstyle/ $(LIBYAMLSTAR-TMP)

$(CABAL-INDEX): $(CABAL)
	cabal update

yamlstar.cabal: cabal.ys $(YS)
	$(YS) $< > $@

License: ../License
	cp $< $@

$(LIBYAMLSTAR-TMP): $(LIBYAMLSTAR-SO-FQNP)
	mkdir -p $(LIBYAMLSTAR-TMP)
	cp -r ../libyamlstar/lib $(LIBYAMLSTAR-TMP)/
