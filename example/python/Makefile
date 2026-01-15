M := .cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/python.mk
include $M/libyamlstar.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := \
  __pycache__/ \

LIBYS := ../../libyamlstar/lib

test: $(PYTHON-VENV) $(LIBYAMLSTAR)
	python yaml-to-json.py sample.yaml
	@echo
	@echo 'Success!'
	@echo 'YAMLStar loaded and working correctly.'
