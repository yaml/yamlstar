# Copyright 2023-2026 Ingy dot Net
# This code is licensed under MIT license (See License for details)

# R binding/API for the libyamlstar shared library.
#
# This package is an R port of the Python 'yamlstar' module, which
# is the reference implementation for YAMLStar FFI bindings to
# libyamlstar.
#
# The current user facing API consists of a single function,
# yamlstar_load(), which takes a YAML string as input and returns
# the R object that YAMLStar loads.

# This value is automatically updated by 'make bump':
YAMLSTAR_VERSION <- "0.1.14"

# Load a YAML string and return the result:
yamlstar_load <- function(input) {
  # Call 'yamlstar_load' in libyamlstar via the C shim:
  json <- .Call(C_yamlstar_load, as.character(input))

  # Decode the JSON response:
  resp <- jsonlite::fromJSON(json, simplifyVector = TRUE)

  # Check for libyamlstar error in JSON response:
  if (!is.null(resp$error)) {
    stop(resp$error$cause)
  }

  # Get the response object from loading the YAML string:
  if (!("data" %in% names(resp))) {
    stop("Unexpected response from 'libyamlstar'")
  }

  resp$data
}
