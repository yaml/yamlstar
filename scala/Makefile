include ../common/init.mk
include $M/clean.mk
include $M/maven.mk
include $(COMMON)/common.mk

export COURSIER_CACHE := $(ROOT)/.cache/coursier
export JAVA_TOOL_OPTIONS := -Djna.tmpdir=$(ROOT)/.cache/jna

SCALA-CLI ?= scala-cli
SCALA-YAMLSTAR-JAR := target/scala-yamlstar-$(VERSION).jar
MAVEN-CENTRAL-ARTIFACT := scala-yamlstar
MAVEN-CENTRAL-JAR := $(SCALA-YAMLSTAR-JAR)
MAVEN-CENTRAL-SOURCE-FILES := $(wildcard src/*.scala)
MAVEN-CENTRAL-JAVADOC-TEXT := \
  YAMLStar Scala API documentation is in the sources JAR.

include $(COMMON)/maven-central.mk


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION)
	@command -v $(SCALA-CLI) >/dev/null || \
	  { echo "'$(SCALA-CLI)' is required to test the Scala binding" >&2; exit 2; }
	mkdir -p $(ROOT)/.cache/jna
	$(SCALA-CLI) run --server=false src test

package: $(MAVEN)
	$(MAVEN) package -DskipTests

release:: $(MAVEN) package
ifndef n
	$(error 'make $@' needs the n variable set to the new version)
endif
	$(MAKE) maven-central-release

clean::
	$(RM) -r .bsp .scala-build src/.bsp src/.scala-build target
