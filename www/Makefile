M := ../.cache/makes
include $M/init.mk
include $M/clean.mk
PYTHON-VENV := venv
include $M/python.mk
include $M/typos.mk
SHELL-DEPS += $(PYTHON-VENV)
include $M/shell.mk

REPO ?= git@github.com:yaml/yamlstar

WATCHER-PID := .watcher.pid

MAKES-CLEAN := site
MAKES-REALCLEAN := $(PYTHON-VENV)
MAKES-DISTCLEAN := .cache/

DEPS := \
  $(PYTHON-VENV) \

WATCH-DEPS := $(if $(wildcard src),watch)

default::
# Build main site (production)
site: $(DEPS)
	mkdocs build -d $@
	$(foreach dir,spec type,$(if $(wildcard $(dir)),cp -r $(dir) $@/;))

# Serve locally with MkDocs (starts watcher automatically)
serve: $(DEPS) $(WATCH-DEPS)
	mkdocs serve --livereload

# Build alias for backwards compatibility
build: site

# Lint check
lint: $(TYPOS)
	typos

# Deploy to production
publish: site
	cd $< && \
	  git init && \
	  git add -A && \
	  git commit -m 'Deploy to production' && \
	  git push -f $(REPO) HEAD:gh-pages
	$(RM) -r $<

# Generate pages from YAMLScript sources
pages:
	ys src/libraries.ys > docs/libraries.md
	ys src/tools.ys > docs/tools.md

# Alias for pages (called by watcher)
update: pages
	@touch docs/libraries.md docs/tools.md

# Watch src/ files and regenerate on change (runs in background)
watch: $(WATCHER-PID)

$(WATCHER-PID):
	watchmedo shell-command \
	  --patterns='*.ys;*.yaml' \
	  --recursive \
	  --command='make update' \
	  src/ & echo $$! > $@

# Stop the file watcher
watch-stop:
ifneq (,$(wildcard $(WATCHER-PID)))
	kill $$(< $(WATCHER-PID)) 2>/dev/null || true
	$(RM) $(WATCHER-PID)
endif

clean:: watch-stop

docs/%.md: src/%.ys src/%.yaml
	ys $< > $@
