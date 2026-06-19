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

YAMLSTAR-PERL-LOCAL := $(ROOT)/perl/local
YAMLSTAR-PERL-LOCAL-LIB := $(YAMLSTAR-PERL-LOCAL)/lib/perl5
YAMLSTAR-PERL-LOCAL-BIN := $(YAMLSTAR-PERL-LOCAL)/bin

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
  $(YAMLSTAR-PERL-LOCAL) \

override export PATH := $(YAMLSTAR-PERL-LOCAL-BIN):$(PATH)
export PERL5LIB := $(YAMLSTAR-PERL-LOCAL-LIB)
export PERL5OPT :=

TEST-DEPS := \
  $(YAMLSTAR-PERL-LOCAL) \
  $(LIBYAMLSTAR-SO) \

ifeq ($(OS-NAME),windows)
test:
	@echo "Skipping Perl tests on Windows (Strawberry Perl cpanm issues)"
else
test: $(TEST-DEPS)
	prove -l$(if $v,v) test/
endif

build: $(LIBYAMLSTAR-SO)

$(YAMLSTAR-PERL-LOCAL): $(PERL)
	MAKES_CACHE_NAME='yamlstar-perl-$(PERL-VERSION)-local-cpan' \
	makes-cache '$@' \
	  cpanm -L '$@' -n $(CPAN-DEPS)

release-cpan: cpan-test cpan-dist
	$(ROOT)/util/release release-perl

release: release-cpan

cpan-build:
	mkdir -p cpan/lib cpan/t
	cp lib/YAMLStar.pm cpan/lib/
	cp test/yamlstar.t cpan/t/
	cp License cpan/LICENSE

cpan-test: cpan-build $(TEST-DEPS)
	cd cpan && perl Makefile.PL && make && make test

cpan-dist: cpan-build $(PERL) $(YAMLSTAR-PERL-LOCAL)
	cd cpan && perl Makefile.PL && make manifest && make dist

cpan-clean:
	rm -rf cpan/lib cpan/t cpan/LICENSE cpan/Makefile cpan/MYMETA.* cpan/MANIFEST cpan/blib cpan/pm_to_blib cpan/*.tar.gz
