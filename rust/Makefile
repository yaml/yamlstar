include ../common/init.mk
include $M/rust.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

MAKES-CLEAN := \
  Cargo.lock \
  target \


ifeq ($(OS-NAME),windows)
test: $(CARGO) $(LIBYAMLSTAR-SO)
	PATH="$(LIBYS)/lib:$$PATH" cargo $@
else
test: $(CARGO) $(LIBYAMLSTAR-SO)
	cargo $@
endif

build check clippy fmt: $(CARGO)
	cargo $@

example: $(CARGO) $(LIBYAMLSTAR-SO)
	cargo run --example load_yaml

release-deps:: $(CARGO)
