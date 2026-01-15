M := $(abspath ../../.cache/makes)
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/go.mk
include $M/libyamlstar.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := \
  yaml-to-json \
  go.mod \
  go.sum \

# libyamlstar.mk installs headers and libraries to .cache/.local/
# Use absolute paths for CGO since go compiles from module cache
override export CGO_CFLAGS := \
  -I $(dir $M)/.local/include/libyamlstar-0.1.0
override export CGO_LDFLAGS := \
  -L $(dir $M)/.local/lib


test: $(GO) $(LIBYAMLSTAR) go.mod go.sum
	go run yaml-to-json.go ../sample.yaml
	@echo
	@echo 'Success!'
	@echo 'YAMLStar loaded and working correctly.'

go.sum: go.mod
	go mod download github.com/yaml/yamlstar-go

go.mod:
	go mod init yamlstar-example
	go get github.com/yaml/yamlstar-go@v0.1.0
