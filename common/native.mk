REFLECTION-JSON := $(COMMON)/reflection.json

GRAALVM-O ?= 1

NATIVE-OPTS := \
  -O$(GRAALVM-O) \
  --verbose \
  --native-image-info \
  --no-fallback \
  --initialize-at-build-time \
  --initialize-at-run-time=clojure.lang.Compiler \
  --emit=build-report \
  -march=compatibility \
  -H:+UnlockExperimentalVMOptions \
  -H:ReflectionConfigurationFiles=$(REFLECTION-JSON) \
  -H:+ReportExceptionStackTraces \
  -J-Dclojure.spec.skip-macros=true \
  -J-Dclojure.compiler.direct-linking=true \
  -J-Xmx3g \
