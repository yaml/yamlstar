SHELL := bash

NAMES := \
  clojure \
  go \
  python \

TEST-NAMES := $(NAMES:%=test-%)
CLEAN-NAMES := $(NAMES:%=clean-%)


test:: $(TEST-NAMES)

clean:: $(CLEAN-NAMES)

$(TEST-NAMES):
	$(MAKE) --no-pr -C $(@:test-%=%) test

$(CLEAN-NAMES):
	$(MAKE) --no-pr -C $(@:clean-%=%) clean
