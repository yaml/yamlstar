include ../common/init.mk
include $M/lein.mk
include $(COMMON)/binding.mk

MAKES-CLEAN =: \
  target/ \
  pom.xml \
  .lein-failures \


test install deploy:: $(LEIN)
	lein $@

example-dump:: $(LEIN)
	lein trampoline run -m clojure.main examples/dump_yaml.clj \
	  <<< '{"name":"Alice","age":30,"active":true,"tags":["admin","dev"]}'

release-deps:: $(LEIN)
