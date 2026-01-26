M := $(abspath ../../.cache/makes)
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk
include $M/java.mk
include $M/maven.mk
include $M/clean.mk
include $M/shell.mk

MAKES-CLEAN := \
  target/ \


test: $(MAVEN)
	mvn -q compile exec:java -Dexec.mainClass=example.YamlToJson -Dexec.args="../sample.yaml"
	@echo
	@echo 'Success!'
	@echo 'YAMLStar loaded from Maven Central and working correctly.'
