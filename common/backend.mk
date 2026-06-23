ifndef YAMLSTAR-BACKEND-MK
YAMLSTAR-BACKEND-MK := 1

YAMLSTAR-BACKEND-COUNT := $(words \
  $(if $(YAMLSTAR_JVM),jvm,) \
  $(if $(YAMLSTAR_GLJ),glj,) \
  $(if $(YAMLSTAR_CPP),cpp,))

YAMLSTAR-BACKEND := $(strip \
  $(if $(YAMLSTAR_JVM),jvm,\
    $(if $(YAMLSTAR_GLJ),glj,\
      $(if $(YAMLSTAR_CPP),cpp,glj))))
YAMLSTAR-BACKEND-SUFFIX := $(YAMLSTAR-BACKEND)
YAMLSTAR-BACKEND-IS-GLOAT := $(filter glj cpp,$(YAMLSTAR-BACKEND))
YAMLSTAR-BACKEND-IS-CPP := $(filter cpp,$(YAMLSTAR-BACKEND))
YAMLSTAR_GLOJURE := $(if $(YAMLSTAR-BACKEND-IS-GLOAT),1,)

.PHONY: validate-backend
validate-backend:
	@test '$(YAMLSTAR-BACKEND-COUNT)' != '2' \
	  && test '$(YAMLSTAR-BACKEND-COUNT)' != '3' || { \
	  echo 'Set at most one of YAMLSTAR_JVM=1, YAMLSTAR_GLJ=1, YAMLSTAR_CPP=1' >&2; \
	  exit 2; \
	}

endif
