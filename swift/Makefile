include ../common/init.mk
include $M/swift.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

SWIFT-FLAGS := --cache-path $(ROOT)/.cache/swiftpm


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(SWIFT)
	swift test $(SWIFT-FLAGS)

release-deps:: $(SWIFT)

clean::
	$(RM) -r .build
