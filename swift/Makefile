include ../common/init.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

SWIFT := $(shell command -v swift)
SWIFT-FLAGS := --cache-path $(ROOT)/.cache/swiftpm


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION)
	@test -n "$(SWIFT)" || { echo "swift not found"; exit 1; }
	swift test $(SWIFT-FLAGS)

release-deps::

clean::
	$(RM) -r .build
