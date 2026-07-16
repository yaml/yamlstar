include ../common/init.mk
include $M/maven.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

KOTLIN-YAMLSTAR-JAR := target/kotlin-yamlstar-0.1.16.jar

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

# The Kotlin wrapper depends on the Java binding artifact, so install
# that into the local Maven repository first:
test:: $(MAVEN) $(LIBYAMLSTAR-SO-VERSION)
	$(MAKE) -C $(ROOT)/java install
	$(MAVEN) test

release-deps:: $(MAVEN)

clean::
	$(RM) -r reports/ target/
