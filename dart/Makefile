include ../common/init.mk
include $M/dart.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

export PUB_CACHE := $(ROOT)/.cache/pub


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(DART)
	dart pub get
	dart run test/main.dart

release-deps:: $(DART)

README.md: ReadMe.md
	cp $< $@

CHANGELOG.md:
	printf '## %s\n\nSee the YAMLStar change log:\n%s\n' \
	  "$(YAMLSTAR_VERSION)" \
	  "https://github.com/yaml/yamlstar/blob/main/Changes" > $@

clean::
	$(RM) -r .dart_tool pubspec.lock README.md CHANGELOG.md
