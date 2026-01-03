# Copyright 2024 yaml.org
# MIT License

M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/rust.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := target

LIBYAMLSTAR_SO := ../libyamlstar/lib/libyamlstar.so.$(LIBYAMLSTAR_VERSION)
LIBYAMLSTAR_VERSION := 0.1.0-SNAPSHOT

.PHONY: test build check clippy fmt example

build: $(CARGO)
	cargo build

test: $(LIBYAMLSTAR_SO) $(CARGO)
	LD_LIBRARY_PATH=../libyamlstar/lib cargo test

check: $(CARGO)
	cargo check

clippy: $(CARGO)
	cargo clippy

fmt: $(CARGO)
	cargo fmt

example: $(LIBYAMLSTAR_SO) $(CARGO)
	LD_LIBRARY_PATH=../libyamlstar/lib cargo run --example load_yaml

$(LIBYAMLSTAR_SO):
	@echo "Building libyamlstar shared library..."
	$(MAKE) -C ../libyamlstar build
