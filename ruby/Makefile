include ../common/init.mk
ifdef YAMLSTAR_SYSTEM_RUBY
RUBY := $(shell command -v ruby)
else
include $M/ruby.mk
endif
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

MAKES-CLEAN := \
  *.gem \
  Gemfile.lock \
  pkg \

test: $(RUBY) $(LIBYAMLSTAR-SO)
	ruby -Ilib test/yamlstar_test.rb

build: $(RUBY)
	gem build yamlstar.gemspec

release-deps:: $(RUBY)
