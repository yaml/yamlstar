# Copyright 2024 yaml.org
# MIT License

M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/go.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := go

LIBYAMLSTAR_VERSION := 0.1.0-SNAPSHOT
LIBYAMLSTAR_SO := ../libyamlstar/lib/libyamlstar.so.$(LIBYAMLSTAR_VERSION)
LIBYAMLSTAR_HEADER := ../libyamlstar/lib/libyamlstar.h

# CGO flags for building and testing
export CGO_CFLAGS := -I $(shell cd .. && pwd)/libyamlstar/lib
export CGO_LDFLAGS := -L $(shell cd .. && pwd)/libyamlstar/lib

# Keep Go paths local to project
unexport GOROOT GOPATH GOBIN GOMODCACHE
export GOROOT := $(GO-LOCAL)
export GOPATH := $(shell pwd)/go
export GOBIN := $(GOROOT)/bin
export PATH := $(GOBIN):$(PATH)

# LD_LIBRARY_PATH for running tests
export LD_LIBRARY_PATH := $(shell cd .. && pwd)/libyamlstar/lib:$(LD_LIBRARY_PATH)


build: $(GO) $(LIBYAMLSTAR_SO)
	go build

test: $(GO) $(LIBYAMLSTAR_SO)
	go test$(if $v, -v)
	chmod -R u+w $(GOPATH) 2>/dev/null || true

check: $(GO)
	go vet

example: $(GO) $(LIBYAMLSTAR_SO)
	go run examples/load_yaml.go

$(LIBYAMLSTAR_SO):
	@echo "Building libyamlstar shared library..."
	$(MAKE) -C ../libyamlstar build

clean::
	[[ ! -d $(GOPATH) ]] || chmod -R u+w $(GOPATH)
	$(RM) -r $(GOPATH)
