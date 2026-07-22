include ../common/init.mk
include $M/maven.mk
include $(COMMON)/common.mk

KOTLIN-YAMLSTAR-JAR := target/kotlin-yamlstar-0.1.17.jar
MAVEN-CENTRAL-ARTIFACT := kotlin-yamlstar
MAVEN-CENTRAL-JAR := $(KOTLIN-YAMLSTAR-JAR)
MAVEN-CENTRAL-SOURCE-FILES := \
  $(wildcard src/main/kotlin/org/yamlstar/yamlstar/*.kt)
MAVEN-CENTRAL-JAVADOC-TEXT := \
  YAMLStar Kotlin API documentation is in the sources JAR.

include $(COMMON)/maven-central.mk

MVN-COMMANDS := \
  compile \
  install \
  package \
  validate \

#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

$(MVN-COMMANDS):: $(MAVEN) $(LIBYAMLSTAR-SO-VERSION)
	$(MAVEN) $@

$(KOTLIN-YAMLSTAR-JAR): package
	test -f $@

# The Kotlin wrapper depends on the Java binding artifact, so install
# that into the local Maven repository first:
test:: $(MAVEN) $(LIBYAMLSTAR-SO-VERSION)
	$(MAKE) -C $(ROOT)/java install
	$(MAVEN) test

ifdef YS_RELEASE_CI
release: $(MAVEN) $(LIBYAMLSTAR-SO-VERSION)
	$(MAKE) -C $(ROOT)/java install
	$(MAVEN) package -DskipTests
else
release: test package
endif
ifndef n
	$(error 'make $@' needs the n variable set to the new version)
endif
	$(MAKE) maven-central-release

clean::
	$(RM) -r reports/ target/
