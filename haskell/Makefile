include ../common/init.mk
ifneq (,$(wildcard /.dockerenv))
HASKELL-DOCKER-INNER := true
else
include $M/cabal.mk
endif
include $M/yamlscript.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

CABAL-INDEX := $(CABAL_DIR)/packages/hackage.haskell.org/01-index.tar.gz
HASKELL-IMAGE ?= ghcr.io/yaml/yamlscript-haskell-test
GIT-COMMON-DIR := $(shell git rev-parse --git-common-dir 2>/dev/null)
GIT-DOCKER-VOLUME := $(if $(GIT-COMMON-DIR),--volume $(GIT-COMMON-DIR):$(GIT-COMMON-DIR):ro,)
LIBYAMLSTAR-TMP := /tmp/libyamlstar
LIBYAMLSTAR-COPY := $(LIBYAMLSTAR-TMP)/lib/$(notdir $(LIBYAMLSTAR-SO-VERSION))
HASKELL-DIST := dist-newstyle/sdist/yamlstar-$(VERSION).0.tar.gz

ifdef HASKELL-DOCKER-INNER
HASKELL-DEPS := \
  yamlstar.cabal \
  License \

else
HASKELL-DEPS := \
  $(GHC) \
  $(CABAL) \
  $(CABAL-INDEX) \
  yamlstar.cabal \
  License \

endif

HACKAGE-TOKEN ?= $(HACKAGE_TOKEN)
ifeq (,$(HACKAGE-TOKEN))
HACKAGE-TOKEN := \
  $(shell ys -e '(.haskell.token || .hackage.token):say' ~/.yamlstar-secrets.yaml 2>/dev/null)
endif

build:: build-doc

build-doc:: ReadMe.md

# Run the tests in the prebaked container when docker is available
# (locally and on CI Linux runners); dependencies never compile:
ifneq (,$(and $(if $(HASKELL-DOCKER-INNER),,x),$(shell command -v docker)))
test:: $(LIBYAMLSTAR-SO-VERSION) yamlstar.cabal License docker-image
	docker run --rm \
	  --user $(USER-UID):$(USER-GID) \
	  --env HOME=/tmp/home \
	  --env CABAL_DIR=/opt/cabal \
	  --volume $(ROOT):$(ROOT) \
	  $(GIT-DOCKER-VOLUME) \
	  --workdir $(CURDIR) \
	  $(HASKELL-IMAGE) \
	  make test

docker-image: yamlstar.cabal
	docker image inspect $(HASKELL-IMAGE) > /dev/null 2>&1 || \
	docker pull $(HASKELL-IMAGE)

docker-image-build: yamlstar.cabal
	$(error Haskell tests use the published $(HASKELL-IMAGE) image)

docker-image-push: docker-image-build
	docker push $(HASKELL-IMAGE)
else
test:: test-cabal test-ffi
endif

test-cabal: $(HASKELL-DEPS) $(LIBYAMLSTAR-COPY)
	cabal test --test-show-details=direct

test-ffi: $(HASKELL-DEPS) $(LIBYAMLSTAR-COPY)
	cabal run -v0 yamlstar-test -- 'key: value' | grep -q '"key": "value"'

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

$(LIBYAMLSTAR-COPY): $(LIBYAMLSTAR-SO-VERSION)
	mkdir -p $(LIBYAMLSTAR-TMP)
	$(RM) -r $(LIBYAMLSTAR-TMP)/lib
	cp -r ../libyamlstar/lib $(LIBYAMLSTAR-TMP)/
