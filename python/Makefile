M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/python.mk
include $M/clean.mk

MAKES-CLEAN := \
  build \
  dist \
  .pytest_cache \
  *.egg-info \
  lib/*.egg-info \
  lib/yamlstar/__pycache__ \
  test/__pycache__ \

PYTHON-VENV-SETUP := pip install pytest

LIBYAMLSTAR_SO := ../libyamlstar/lib/libyamlstar.so

.PHONY: test
test: $(PYTHON-VENV) test-pytest test-ffi

build: $(LIBYAMLSTAR_SO)

dist: $(PYTHON-VENV)
	$(VENV) && python setup.py sdist

install: $(PYTHON-VENV)
	$(VENV) && pip install -e .

test-pytest: $(LIBYAMLSTAR_SO) $(PYTHON-VENV)
	$(VENV) && pytest test/

test-ffi: $(LIBYAMLSTAR_SO) $(PYTHON-VENV)
	@echo "Testing FFI with libyamlstar..."
	$(VENV) && python -c 'import sys; sys.path.insert(0,"lib"); import yamlstar; print("Test 1 - Simple scalar:", yamlstar.YAMLStar().load("hello"))'
	$(VENV) && python -c 'import sys; sys.path.insert(0,"lib"); import yamlstar; print("Test 2 - Simple mapping:", yamlstar.YAMLStar().load("key: value"))'
	$(VENV) && python -c 'import sys; sys.path.insert(0,"lib"); import yamlstar; print("Test 3 - Type coercion:", yamlstar.YAMLStar().load("num: 42\nbool: true\nnull: null"))'
	$(VENV) && python -c 'import sys; sys.path.insert(0,"lib"); import yamlstar; print("Test 4 - Load all:", yamlstar.YAMLStar().load_all("---\ndoc1\n---\ndoc2"))'
	$(VENV) && python -c 'import sys; sys.path.insert(0,"lib"); import yamlstar; print("Test 5 - Version:", yamlstar.YAMLStar().version())'
	@echo "✓ All FFI tests passed"

$(LIBYAMLSTAR_SO):
	@echo "Building libyamlstar shared library..."
	$(MAKE) -C ../libyamlstar build
