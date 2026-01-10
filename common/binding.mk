BINDING-NAME := $(shell basename $(shell pwd))

release:
	$(ROOT)/util/release release-$(BINDING-NAME)
