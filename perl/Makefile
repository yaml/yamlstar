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

PERL-LOCAL := $(ROOT)/perl/local
PERL-LOCAL-LIB := $(PERL-LOCAL)/lib/perl5
PERL-LOCAL-BIN := $(PERL-LOCAL)/bin

MAKES-CLEAN := \
  cpan/LICENSE \
  cpan/MANIFEST \
  cpan/MANIFEST.bak \
  cpan/MYMETA.json \
  cpan/MYMETA.yml \
  cpan/Makefile \
  cpan/YAMLStar-*.tar \
  cpan/YAMLStar-*.tar.gz \
  cpan/lib/ \
  cpan/t/ \
  cpan/blib/ \
  cpan/pm_to_blib \

MAKES-REALCLEAN := \
  $(PERL-LOCAL) \

override export PATH := $(PERL-LOCAL-BIN):$(PATH)
export PERL5LIB := $(PERL-LOCAL-LIB)
export PERL5OPT :=

TEST-DEPS := \
  $(PERL-LOCAL) \
  $(LIBYAMLSTAR-SO) \

ifeq ($(OS-NAME),windows)
test:
	@echo "Skipping Perl tests on Windows (Strawberry Perl cpanm issues)"
else
test: $(TEST-DEPS)
	prove -l$(if $v,v) test/
endif

build: $(LIBYAMLSTAR-SO)

$(PERL-LOCAL): $(PERL)
	cpanm -L $@ -n $(CPAN-DEPS)

release-cpan: cpan-test cpan-dist
	$(ROOT)/util/release release-perl

cpan-build:
	mkdir -p cpan/lib cpan/t
	cp lib/YAMLStar.pm cpan/lib/
	cp test/yamlstar.t cpan/t/
	cp License cpan/LICENSE

cpan-test: cpan-build $(TEST-DEPS)
	cd cpan && perl Makefile.PL && make && make test

cpan-dist: cpan-build $(PERL) $(PERL-LOCAL)
	cd cpan && perl Makefile.PL && make manifest && make dist

cpan-clean:
	rm -rf cpan/lib cpan/t cpan/LICENSE cpan/Makefile cpan/MYMETA.* cpan/MANIFEST cpan/blib cpan/pm_to_blib cpan/*.tar.gz
