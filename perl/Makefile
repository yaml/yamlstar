include ../common/init.mk
include $M/perl.mk
include $(COMMON)/common.mk

CPAN-DEPS := \
  Cpanel::JSON::XS \
  FFI::CheckLib \
  FFI::Platypus \
  Moo \
  Test2::V0 \

PERL-LOCAL-LIB := local

MAKES-REALCLEAN := \
  $(PERL-LOCAL-LIB) \

export PERL5LIB := $(PERL-LOCAL-LIB)/lib/perl5
export PERL5OPT :=


test: $(PERL-LOCAL-LIB) $(LIBYAMLSTAR-SO)
	prove -l$(if $v,v) test/

build: $(LIBYAMLSTAR-SO)

$(PERL-LOCAL-LIB): $(PERL)
	cpanm -L $@ -n $(CPAN-DEPS)
