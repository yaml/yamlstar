include ../common/init.mk
include $M/python.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

MAKES-CLEAN := \
  build \
  dist \
  .pytest_cache \
  *.egg-info \
  lib/*.egg-info \
  lib/yamlstar/__pycache__ \
  test/__pycache__ \

PYTHON-VENV-SETUP := pip install pytest setuptools twine wheel

WHEEL-PLAT-linux-int64 := manylinux_2_31_x86_64
WHEEL-PLAT-macos-arm64 := macosx_14_0_arm64
WHEEL-PLAT := $(WHEEL-PLAT-$(OS-ARCH))
WHEEL-LIB-DIR := lib/yamlstar/libyamlstar
WHEEL-LIB = $(WHEEL-LIB-DIR)/$(notdir $(PYTHON-SO))
RELEASE-WHEEL-VERSION := $(or $(n),$(VERSION))
RELEASE-WHEEL-DIR ?= dist/release-assets
RELEASE-WHEEL-TARS := \
  $(wildcard $(RELEASE-WHEEL-DIR)/libyamlstar-$(RELEASE-WHEEL-VERSION)-*.tar.xz)

# Python uses the gloat library when YAMLSTAR_GLOJURE=1, GraalVM otherwise
ifdef YAMLSTAR_GLOJURE
PYTHON-SO := $(LIBYS)/lib/libyamlstarglj.$(SO)
$(PYTHON-SO):
	$(MAKE) -C $(LIBYS) build YAMLSTAR_GLOJURE=1
else
PYTHON-SO := $(LIBYAMLSTAR-SO)
endif


test: test-pytest

test-pytest: $(PYTHON-SO) $(PYTHON-VENV)
	$(VENV) && pytest test/

build: $(PYTHON-SO)

dist: $(PYTHON-VENV) MANIFEST.in Meta
	$(VENV) && python setup.py sdist

wheel: $(PYTHON-VENV) MANIFEST.in Meta $(WHEEL-LIB)
	@test -n "$(WHEEL-PLAT)" || { \
	  echo "No Python wheel platform tag for $(OS-ARCH)"; \
	  exit 1; \
	}
	$(VENV) && python setup.py bdist_wheel --plat-name $(WHEEL-PLAT)

$(WHEEL-LIB): $(PYTHON-SO)
	mkdir -p $(dir $@)
	cp -p $< $@

wheels-from-release: $(PYTHON-VENV) MANIFEST.in Meta
	@test -n "$(RELEASE-WHEEL-TARS)" || { \
	  echo 'No libyamlstar release archives found in $(RELEASE-WHEEL-DIR)'; \
	  exit 1; \
	}
	@set -e; \
	  for tar in $(RELEASE-WHEEL-TARS); do \
	    name=$$(basename "$$tar" .tar.xz); \
	    relplat=$${name#libyamlstar-$(RELEASE-WHEEL-VERSION)-}; \
	    case "$$relplat" in \
	      linux-x64) plat=manylinux_2_31_x86_64; libpat='libyamlstar.so*' ;; \
	      macos-arm64) plat=macosx_14_0_arm64; libpat='libyamlstar.dylib*' ;; \
	      *) echo "Skipping unsupported Python wheel asset: $$name"; \
	         continue ;; \
	    esac; \
	    rm -rf build $(WHEEL-LIB-DIR); \
	    mkdir -p $(WHEEL-LIB-DIR); \
	    tmp=$$(mktemp -d); \
	    tar -C "$$tmp" -xf "$$tar"; \
	    lib=$$(find "$$tmp" -type f -name "$$libpat" | sort | head -n1); \
	    test -n "$$lib" || { echo "No $$libpat found in $$tar"; exit 1; }; \
	    cp -p "$$lib" $(WHEEL-LIB-DIR)/; \
	    echo "Building Python wheel for $$relplat"; \
	    $(VENV) && python setup.py bdist_wheel --plat-name "$$plat"; \
	    rm -rf "$$tmp"; \
	  done; \
	  rm -rf build $(WHEEL-LIB-DIR)

install: $(PYTHON-VENV)
	$(VENV) && pip install -e .

publish: dist
	$(VENV) && \
	  TWINE_USERNAME=__token__ \
	  TWINE_PASSWORD=$${PYPI_API_TOKEN:-$${PYPI_TOKEN:-}} \
	  twine upload dist/yamlstar-*.tar.gz

publish-wheels: $(PYTHON-VENV)
	@test "$$(find dist -maxdepth 1 -name '*.whl' | wc -l)" -gt 0 || { \
	  echo 'No Python wheels found in python/dist'; \
	  exit 1; \
	}
	$(VENV) && \
	  TWINE_USERNAME=__token__ \
	  TWINE_PASSWORD=$${PYPI_API_TOKEN:-$${PYPI_TOKEN:-}} \
	  twine upload --skip-existing dist/*.whl

pkg-test: $(PYTHON-VENV) dist
	$(VENV) && pip install --force-reinstall dist/yamlstar-*.tar.gz

MANIFEST.in:
	echo 'include ReadMe.md' > $@
	echo 'include Meta' >> $@

Meta: ../Meta
	cp $< $@

release-deps:: $(PYTHON-VENV)

clean::
	$(RM) MANIFEST.in Meta
	$(RM) -r $(WHEEL-LIB-DIR)
