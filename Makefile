M := .cache/makes
$(shell [ -d $M ] || git clone -q https://github.com/makeplus/makes $M)
include $M/init.mk

# GraalVM no longer supports macOS Intel - error out early
ifeq ($(OS-NAME)-$(ARCH-NAME),macos-int64)
$(error GraalVM no longer supports macOS Intel (x64))
endif

include $M/graalvm.mk
include $M/lein.mk
include $M/yamlscript.mk
include $M/gh.mk
include $M/clean.mk
include $M/shell.mk

# Extract version from Meta file
VERSION := $(shell grep '^version:' Meta | cut -d' ' -f2)

MAKES-CLEAN := \
  META-INF/ \

MAKES-REALCLEAN := \
  $(MAVEN-REPOSITORY) \

MAKES-DISTCLEAN += \
  .clj-kondo/ \
  .lsp/ \

BINDING-DIRS := \
  csharp \
  fortran \
  go \
  nodejs \
  perl \
  python \
  rust \

ALL-DIRS := \
  cli \
  core \
  libyamlstar \
  $(BINDING-DIRS) \

ALL-CLEAN := $(ALL-DIRS:%=clean-%)
ALL-REALCLEAN := $(ALL-DIRS:%=realclean-%)
ALL-DISTCLEAN := $(ALL-DIRS:%=distclean-%)

BINDING-TESTS := $(BINDING-DIRS:%=test-%)
ALL-TESTS := \
  test-core \
  test-cli \
  test-libyamlstar \
  $(BINDING-TESTS)


build jar install::
	$(MAKE) -C core $@ v=$v
	$(MAKE) -C cli $@ v=$v
	$(MAKE) -C libyamlstar $@ v=$v

test:: $(ALL-TESTS)

$(ALL-TESTS):
	@echo '--------------------------------------------------'
	@echo '   $@'
	@echo '--------------------------------------------------'
	$(MAKE) -C $(@:test-%=%) test v=$v

core:
	$(MAKE) -C core install

cli:
	$(MAKE) -C cli build

libyamlstar:
	$(MAKE) -C libyamlstar build

clean:: $(ALL-CLEAN)
realclean:: $(ALL-REALCLEAN)
distclean:: $(ALL-DISTCLEAN)

$(ALL-CLEAN):
	$(MAKE) --no-pr -C $(@:clean-%=%) clean

$(ALL-REALCLEAN):
	$(MAKE) --no-pr -C $(@:realclean-%=%) realclean

$(ALL-DISTCLEAN):
	$(MAKE) --no-pr -C $(@:distclean-%=%) distclean

#------------------------------------------------------------------------------
# Release targets
#------------------------------------------------------------------------------

# Bump version and update changelog
# Usage: make version-bump o=0.1.0 n=0.2.0
version-bump:
	@if [ -z "$(o)" ] || [ -z "$(n)" ]; then \
		echo "ERROR: Usage: make version-bump o=OLD_VERSION n=NEW_VERSION"; \
		exit 1; \
	fi
	@echo "Bumping version from $(o) to $(n)..."
	@export ROOT=$(PWD) YS_RELEASE_VERSION_OLD=$(o) YS_RELEASE_VERSION_NEW=$(n); \
	./util/version-bump
	@echo ""
	@echo "Generating changelog entry from git history..."
	@git log --oneline v$(o)..HEAD 2>/dev/null | tac | sed 's/^[a-f0-9]* /  - /' > release-changes.txt || \
		(echo "  - Update to version $(n)" > release-changes.txt)
	@echo "- version: $(n)" > release-entry.txt
	@echo "  date:    $$(date)" >> release-entry.txt
	@echo "  changes:" >> release-entry.txt
	@cat release-changes.txt >> release-entry.txt
	@echo ""
	@echo "Opening editor to review changelog entry..."
	@$${EDITOR:-vi} release-changes.txt
	@echo ""
	@read -p "Save this entry to Changes file? [Y/n] " answer; \
	if [ "$${answer:-Y}" = "Y" ] || [ "$${answer:-Y}" = "y" ]; then \
		sed 's/^[a-f0-9]* /  - /' release-changes.txt > release-changes-formatted.txt; \
		echo "- version: $(n)" > release-entry.txt; \
		echo "  date:    $$(date)" >> release-entry.txt; \
		echo "  changes:" >> release-entry.txt; \
		cat release-changes-formatted.txt >> release-entry.txt; \
		cat release-entry.txt Changes > Changes.new; \
		mv Changes.new Changes; \
		rm -f release-changes.txt release-changes-formatted.txt release-entry.txt; \
		echo "Changelog updated!"; \
	else \
		echo "Changelog NOT updated. Entry saved in release-changes.txt"; \
	fi

# Check that VERSION input matches Meta file
check-version:
	@META_VERSION=$$(grep '^version:' Meta | cut -d' ' -f2); \
	if [ "$$META_VERSION" != "$(VERSION)" ]; then \
		echo "ERROR: VERSION=$(VERSION) does not match Meta file version: $$META_VERSION"; \
		exit 1; \
	fi
	@echo "Version check passed: $(VERSION)"

# Build shared library for current platform
release-lib:
	$(MAKE) -C libyamlstar build
	mkdir -p dist
	@if [ "$(OS-NAME)" = "linux" ]; then \
		tar -czf dist/libyamlstar-$(VERSION)-linux-x64.tar.gz -C libyamlstar/lib libyamlstar.so; \
	elif [ "$(OS-NAME)" = "macos" ]; then \
		tar -czf dist/libyamlstar-$(VERSION)-macos-arm64.tar.gz -C libyamlstar/lib libyamlstar.dylib; \
	elif [ "$(OS-NAME)" = "windows" ]; then \
		cd libyamlstar/lib && zip ../../dist/libyamlstar-$(VERSION)-windows-x64.zip libyamlstar.dll; \
	fi
	@echo "Built release artifact in dist/"

# Create and push git tag
release-tag:
	@if git rev-parse "v$(VERSION)" >/dev/null 2>&1; then \
		echo "ERROR: Tag v$(VERSION) already exists"; \
		exit 1; \
	fi
	git tag "v$(VERSION)"
	git push origin "v$(VERSION)"
	@echo "Created and pushed tag v$(VERSION)"

# Create GitHub release with artifacts
release-github:
	@if ! gh release view "v$(VERSION)" >/dev/null 2>&1; then \
		echo "Extracting release notes from Changes file..."; \
		ys -e "load('Changes').find(\#(%[:version] == '$(VERSION)')).get(:changes).map(\#(str '- ' %)).join(str/lf):say" > release-notes.txt 2>/dev/null || \
			echo "Release $(VERSION)" > release-notes.txt; \
		gh release create "v$(VERSION)" \
			--title "YAMLStar $(VERSION)" \
			--notes-file release-notes.txt \
			dist/libyamlstar-$(VERSION)-*.tar.gz \
			dist/libyamlstar-$(VERSION)-*.zip; \
		rm -f release-notes.txt; \
		echo "Created GitHub release v$(VERSION)"; \
	else \
		echo "Release v$(VERSION) already exists"; \
	fi

# Check that GitHub release exists with all shared library assets
check-release:
	@echo "Checking for GitHub release v$(VERSION)..."
	@gh release view "v$(VERSION)" >/dev/null 2>&1 || (echo "ERROR: Release v$(VERSION) not found"; exit 1)
	@echo "Checking for required assets..."
	@gh release view "v$(VERSION)" --json assets --jq '.assets[].name' | grep -q "libyamlstar-$(VERSION)-linux-x64.tar.gz" || (echo "ERROR: Missing linux-x64 asset"; exit 1)
	@gh release view "v$(VERSION)" --json assets --jq '.assets[].name' | grep -q "libyamlstar-$(VERSION)-macos-arm64.tar.gz" || (echo "ERROR: Missing macos-arm64 asset"; exit 1)
	@gh release view "v$(VERSION)" --json assets --jq '.assets[].name' | grep -q "libyamlstar-$(VERSION)-windows-x64.zip" || (echo "ERROR: Missing windows-x64 asset"; exit 1)
	@echo "All required assets found for v$(VERSION)"

# Release Python binding to PyPI
release-python:
	@echo "Publishing Python package to PyPI..."
	$(MAKE) -C python build
	@if [ -f ~/.yamlstar-secrets.yaml ]; then \
		PYPI_TOKEN=$$(ys -e '.pypi.token:say' ~/.yamlstar-secrets.yaml); \
		cd python && TWINE_PASSWORD=$$PYPI_TOKEN twine upload --repository yamlstar dist/*.tar.gz; \
	else \
		echo "ERROR: ~/.yamlstar-secrets.yaml not found"; \
		exit 1; \
	fi

# Release all bindings (currently just Python)
release-bindings: check-release release-python

.PHONY: cli core libyamlstar test check-version release-lib release-tag release-github check-release release-python release-bindings
