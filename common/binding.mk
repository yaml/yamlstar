BINDING-NAME := $(shell basename $(shell pwd))

ifeq ($(OS-NAME),windows)
  override export PATH := $(LIBYS)/lib:$(PATH)
endif

release:: release-deps
	$(ROOT)/util/release release-$(BINDING-NAME)

release-deps::
	@true
