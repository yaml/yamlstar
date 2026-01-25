include ../common/init.mk
include $M/go.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

# CGO flags for building and testing
ifeq ($(OS-NAME),windows)
export CGO_CFLAGS := -I $(abspath $(LIBYS)/lib)
export CGO_LDFLAGS := -L $(abspath $(LIBYS)/lib)
else
export CGO_CFLAGS := -I $(ROOT)/libyamlstar/lib
export CGO_LDFLAGS := -L $(ROOT)/libyamlstar/lib
endif


build: $(GO) $(LIBYAMLSTAR-SO-VERSION) $(LIBYAMLSTAR-HEADER)
	go build

test: $(GO) $(LIBYAMLSTAR-SO-VERSION) $(LIBYAMLSTAR-HEADER)
	go test$(if $v, -v)
	chmod -R u+w $(GOPATH) 2>/dev/null || true

check: $(GO)
	go vet

example: $(GO) $(LIBYAMLSTAR-SO-VERSION)
	go run examples/load_yaml.go

clean::
	[[ ! -d $(GOPATH) ]] || chmod -R u+w $(GOPATH)
	$(RM) -r $(GOPATH)
