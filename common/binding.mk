BINDING-NAME := $(shell basename $(shell pwd))

release: release-deps
	$(ROOT)/util/release release-$(BINDING-NAME)

release-deps::
	@true
