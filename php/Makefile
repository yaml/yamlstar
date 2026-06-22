include ../common/init.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

PHP := $(shell command -v php)
PHP-RUN := $(PHP) -d ffi.enable=1

test: $(LIBYAMLSTAR-SO)
	@test -n "$(PHP)" || { echo "php not found"; exit 1; }
	@if $(PHP-RUN) -r 'exit(class_exists("FFI") ? 0 : 1);'; then \
	  $(PHP-RUN) tests/YAMLStarTest.php; \
	else \
	  echo "Skipping PHP tests: PHP FFI extension not enabled"; \
	fi

build:
	@test -f composer.json

release-deps::
	@true
