M := $(abspath ../../.cache/makes)
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/go.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := \
  yaml-to-json \

test: $(GO)
	go run yaml-to-json.go ../sample.yaml
	@echo
	@echo 'Success!'
	@echo 'YAMLStar loaded and working correctly.'
