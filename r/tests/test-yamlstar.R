# Copyright 2023-2026 Ingy dot Net
# This code is licensed under MIT license (See License for details)

# Test the yamlstar R binding.
# Run with: Rscript tests/test-yamlstar.R

library(yamlstar)

fails <- 0

check <- function(cond, label) {
  if (isTRUE(cond)) {
    cat("ok -", label, "\n")
  } else {
    cat("not ok -", label, "\n")
    fails <<- fails + 1
  }
}

# Load YAML mapping:
data <- yamlstar_load("test: 42")
check(data$test == 42, "load mapping")

# Load plain YAML:
data <- yamlstar_load("foo: bar")
check(data$foo == "bar", "load plain yaml")

# Load invalid input raises:
threw <- tryCatch(
  {
    yamlstar_load(":")
    FALSE
  },
  error = function(e) TRUE
)
check(threw, "load error raises")

# Load multiple times:
data <- yamlstar_load("test: 42")
check(data$test == 42, "load multiple times")

if (fails > 0) {
  cat(fails, "test(s) failed\n")
  quit(status = 1)
}
