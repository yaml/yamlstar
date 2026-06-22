include ../common/init.mk
include $M/ruby.mk
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

