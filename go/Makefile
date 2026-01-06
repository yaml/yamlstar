include ../common/init.mk
include $M/go.mk
include $(COMMON)/common.mk

# CGO flags for building and testing
export CGO_CFLAGS := -I $(ROOT)/libyamlstar/lib
export CGO_LDFLAGS := -L $(ROOT)/libyamlstar/lib


build: $(GO) $(LIBYAMLSTAR-SO-VERSION)
	go build

test: $(GO) $(LIBYAMLSTAR-SO-VERSION)
	go test$(if $v, -v)
	chmod -R u+w $(GOPATH) 2>/dev/null || true

check: $(GO)
	go vet

example: $(GO) $(LIBYAMLSTAR-SO-VERSION)
	go run examples/load_yaml.go

clean::
	[[ ! -d $(GOPATH) ]] || chmod -R u+w $(GOPATH)
	$(RM) -r $(GOPATH)
