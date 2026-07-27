include ../common/init.mk
include $M/go.mk
include $(COMMON)/binding.mk


build: $(GO)
	go build

test: $(GO)
	go test$(if $v, -v)
	chmod -R u+w $(GOPATH) 2>/dev/null || true

check: $(GO)
	go vet

example: $(GO)
	go run examples/load_yaml.go

clean::
	[[ ! -d $(GOPATH) ]] || chmod -R u+w $(GOPATH)
	$(RM) -r $(GOPATH)

release-deps:: $(GO)
