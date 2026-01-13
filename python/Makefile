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


test: test-pytest

ifeq ($(OS-NAME),windows)
test-pytest: $(LIBYAMLSTAR-SO) $(PYTHON-VENV)
	PATH="$(LIBYS)/lib:$$PATH" $(VENV) && pytest test/
else
test-pytest: $(LIBYAMLSTAR-SO) $(PYTHON-VENV)
	$(VENV) && pytest test/
endif

build: $(LIBYAMLSTAR-SO)

dist: $(PYTHON-VENV)
	$(VENV) && python setup.py sdist

install: $(PYTHON-VENV)
	$(VENV) && pip install -e .

release-deps:: $(PYTHON-VENV)
