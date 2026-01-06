M := ../.cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/perl.mk
include $M/shell.mk
include $M/clean.mk

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

LIBYAMLSTAR_SO := ../libyamlstar/lib/libyamlstar.so


test: $(PERL-LOCAL-LIB) $(LIBYAMLSTAR_SO)
	prove -l$(if $v,v) test/

build: $(LIBYAMLSTAR_SO)

$(PERL-LOCAL-LIB): $(PERL)
	cpanm -L $@ -n $(CPAN-DEPS)

$(LIBYAMLSTAR_SO):
	$(MAKE) -C ../libyamlstar build
