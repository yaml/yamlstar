include ../common/init.mk
SWIFT-VERSION := 6.3.3
include $M/swift.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

SWIFT-FLAGS := --cache-path $(ROOT)/.cache/swiftpm
SWIFT-TEST := $(SWIFT)
SWIFT-TEST-DEPS := $(SWIFT)

# Swiftly's macOS toolchains do not include the Xcode XCTest framework.
# Use Xcode's selected toolchain for tests on macOS.
ifeq ($(OS-NAME),macos)
SWIFT-TEST := xcrun swift
SWIFT-TEST-DEPS :=
endif


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(SWIFT-TEST-DEPS)
	$(SWIFT-TEST) test $(SWIFT-FLAGS)

release-deps::
	@true

clean::
	$(RM) -r .build
	$(RM) $(ROOT)/.swift-version
