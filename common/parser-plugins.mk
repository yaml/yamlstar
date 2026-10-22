# Optional parser source checkouts for local cross-repository development.
REFERENCE-PARSER-DIR ?= $(ROOT)/repos/yamlstar-plugin-parser-reference
JSON-COMMENTS-PARSER-DIR ?= $(ROOT)/repos/yamlstar-plugin-parser-json-comments
PARSER-GO-REPLACE := $(if $(wildcard $(REFERENCE-PARSER-DIR)/go.mod),\
  github.com/yamlstar/yamlstar-plugin-parser-reference => $(abspath $(REFERENCE-PARSER-DIR)))
PARSER-GO-REPLACE += $(if $(wildcard $(JSON-COMMENTS-PARSER-DIR)/go.mod),\
  $(if $(strip $(PARSER-GO-REPLACE)),\nreplace )github.com/yamlstar/yamlstar-plugin-parser-json-comments => $(abspath $(JSON-COMMENTS-PARSER-DIR)))
GLOAT-PARSER-ENV := $(if $(strip $(PARSER-GO-REPLACE)),\
  GLOAT_GO_REPLACE=$$'$(strip $(PARSER-GO-REPLACE))')
