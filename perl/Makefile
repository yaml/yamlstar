include ../common/init.mk
include $M/perl.mk
include $(COMMON)/common.mk

CPAN-DEPS := \
  CPAN::Uploader \
  Cpanel::JSON::XS \
  FFI::CheckLib \
  FFI::Platypus \
  Moo \
  Test2::V0 \

PERL-LOCAL-LIB := local

MAKES-CLEAN := \
  cpan/LICENSE \
  cpan/MANIFEST \
  cpan/MYMETA.json \
  cpan/MYMETA.yml \
  cpan/Makefile \
  cpan/YAMLStar-*.tar.gz \
  cpan/lib/ \
  cpan/t/ \

MAKES-REALCLEAN := \
  $(PERL-LOCAL-LIB) \

export PERL5LIB := $(PERL-LOCAL-LIB)/lib/perl5
export PERL5OPT :=


ifeq ($(OS-NAME),windows)
test:
	@echo "Skipping Perl tests on Windows (Strawberry Perl cpanm issues)"
else
test: $(PERL-LOCAL-LIB) $(LIBYAMLSTAR-SO)
	prove -l$(if $v,v) test/
endif

build: $(LIBYAMLSTAR-SO)

$(PERL-LOCAL-LIB): $(PERL)
	cpanm -L $@ -n $(CPAN-DEPS)

release-cpan: $(PERL)
	$(ROOT)/util/release release-perl

cpan-build:
	mkdir -p cpan/lib cpan/t
	cp lib/YAMLStar.pm cpan/lib/
	cp test/yamlstar.t cpan/t/
	cp License cpan/LICENSE

cpan-test: cpan-build $(PERL)
	cd cpan && perl Makefile.PL && make && make test

cpan-dist: cpan-build $(PERL)
	cd cpan && perl Makefile.PL && make manifest && make dist

cpan-clean:
	rm -rf cpan/lib cpan/t cpan/LICENSE cpan/Makefile cpan/MYMETA.* cpan/MANIFEST cpan/blib cpan/pm_to_blib cpan/*.tar.gz
