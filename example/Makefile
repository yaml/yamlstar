SHELL := bash

NAMES := $(wildcard */Makefile)
NAMES := $(NAMES:%/Makefile=%)

TEST-NAMES := $(NAMES:%=test-%)
CLEAN-NAMES := $(NAMES:%=clean-%)


test:: $(TEST-NAMES)
	@echo 'All tests passed for: $(NAMES)'
	@echo

clean:: $(CLEAN-NAMES)

$(TEST-NAMES):
	@echo '--------------------------------------------------------------------------------'
	@echo $@
	@echo '--------------------------------------------------------------------------------'
	$(MAKE) --no-pr -C $(@:test-%=%) test
	@echo

$(CLEAN-NAMES):
	$(MAKE) --no-pr -C $(@:clean-%=%) clean
