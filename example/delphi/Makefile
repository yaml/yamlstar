LIBYAMLSTAR-VERSION := 0.1.2

M := $(abspath ../../.cache/makes)
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/fpc.mk
include $M/libyamlstar.mk
include $M/clean.mk
include $M/shell.mk


MAKES-CLEAN := \
  *.o \
  *.ppu \
  link*.res \
  yaml-to-json \

test: $(FPC) $(LIBYAMLSTAR)
	fpc -Fu../../delphi/src -Fl$(LOCAL-LIB) -oyaml-to-json yaml-to-json.pas
	LD_LIBRARY_PATH=$(LOCAL-LIB):$$LD_LIBRARY_PATH ./yaml-to-json ../sample.yaml
	@echo
	@echo 'Success!'
	@echo 'YAMLStar loaded and working correctly.'
