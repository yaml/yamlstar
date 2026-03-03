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

PYTHON-VENV-SETUP := pip install pytest setuptools twine

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

dist: $(PYTHON-VENV)
	$(VENV) && python setup.py sdist

install: $(PYTHON-VENV)
	$(VENV) && pip install -e .

release-deps:: $(PYTHON-VENV)
