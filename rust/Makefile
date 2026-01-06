include ../common/init.mk
include $M/rust.mk
include $(COMMON)/common.mk

MAKES-CLEAN := \
  Cargo.lock \
  target \


test: $(CARGO) $(LIBYAMLSTAR-SO)
	cargo $@

build check clippy fmt: $(CARGO)
	cargo $@

example: $(CARGO) $(LIBYAMLSTAR-SO)
	cargo run --example load_yaml
