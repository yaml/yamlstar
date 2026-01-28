# YAMLStar Release Process

This document describes how to release YAMLStar shared libraries and language
bindings.

## Overview

YAMLStar uses a two-phase release process:

1. **Shared Library Release** (GitHub Actions) - Builds and publishes platform
   binaries to GitHub Releases
2. **Language Binding Release** (Local) - Publishes to package registries after
   shared libraries are released

## Platform Support

Currently building and releasing for:
- Linux x64 (primary build platform)
- macOS ARM64 (Apple Silicon)
- Windows x64

Linux ARM64 support exists but is disabled due to slow GitHub Actions runners.

## Step 1: Bump Version and Update Changelog

Update the version number across all files and create a changelog entry:

```bash
# Run version bump (handles versions AND changelog)
make version-bump o=0.1.0 n=0.1.1
```

This command will:
1. Update version strings in all files:
   - `Meta` (single source of truth)
   - All language binding files (`setup.py`, `package.json`, `Cargo.toml`, etc.)
   - Clojure project files
2. Generate a changelog entry from git commits since v0.1.0
3. Open your editor to review/edit the changelog entry
4. Prompt you to save the entry to the `Changes` file

```bash
# Review all changes
git diff

# Commit and push
git add -A
git commit -m "Bump version to 0.1.1"
git push
```

### Changelog Format

The `Changes` file uses YAML format:
```yaml
- version: 0.1.1
  date:    Mon Jan  9 10:00:00 AM PST 2026
  changes:
  - core: Add new feature X
  - bindings: Fix issue Y in Python binding
  - make: Improve build system
```

These changelog entries are automatically used as GitHub Release notes.

## Step 2: Release Shared Libraries

This step builds shared libraries for all platforms and creates a GitHub Release.

### Via GitHub Actions (Recommended)

1. Go to your GitHub repository
2. Click **Actions** tab
3. Select **Release Shared Libraries** workflow
4. Click **Run workflow** button
5. Enter the version (e.g., `0.1.1`)
6. Click **Run workflow**

The workflow will:
- Verify the version matches the `Meta` file
- Build `libyamlstar.so` on Linux x64
- Build `libyamlstar.dylib` on macOS ARM64
- Build `libyamlstar.dll` on Windows x64
- Create git tag `v0.1.1`
- Create GitHub Release with all platform binaries attached

### Artifacts Created

The release includes:
- `libyamlstar-0.1.1-linux-x64.tar.gz`
- `libyamlstar-0.1.1-macos-arm64.tar.gz`
- `libyamlstar-0.1.1-windows-x64.zip`

## Step 3: Release Language Bindings

After shared libraries are released, publish language bindings locally.

## Step 4: Publish Website

After releasing bindings, deploy the updated website to GitHub Pages.

```bash
# Publish website to yamlstar.org
make publish
```

This will:
- Build the MkDocs site from `www/docs/`
- Deploy to GitHub Pages using `mkdocs gh-deploy`
- Update https://yamlstar.org with the new version

The website version is automatically updated during the version bump step (Step 1),
as `www/docs/index.md` is included in the `.version.ys` file list.

### Setup Secrets File

Create `~/.yamlstar-secrets.yaml`:

```yaml
pypi:
  token: pypi-AgEIcHl...your-token-here

ossrh:
  user: your-maven-central-username
  token: your-maven-central-token

gpg:
  key-id: YOUR_GPG_KEY_ID
```

To get a PyPI API token:
1. Go to https://pypi.org/manage/account/token/
2. Create a token scoped to the `yamlstar` project (or all projects initially)
3. Copy the token to your secrets file

To get Maven Central credentials:
1. See [doc/MAVEN-CENTRAL.md](MAVEN-CENTRAL.md) for complete setup instructions
2. Generate a user token at https://central.sonatype.com/usertoken
3. Add your GPG key ID (see `gpg --list-secret-keys --keyid-format=long`)

### Publish Python Binding

```bash
# This checks for shared library assets, then publishes to PyPI
make release-bindings VERSION=0.1.1
```

The `release-bindings` target:
1. Verifies GitHub Release `v0.1.1` exists
2. Checks for all required shared library assets
3. Builds Python package
4. Uploads to PyPI using `twine`

### Verify Publication

```bash
# Check PyPI
pip install yamlstar==0.1.1

# Verify it works
python -c "import yamlstar; print(yamlstar.__version__)"
```

## Makefile Targets

For advanced usage or debugging:

```bash
# Check version matches Meta file
make check-version VERSION=0.1.1

# Build shared library for current platform only
make release-lib VERSION=0.1.1

# Create and push git tag (normally done by workflow)
make release-tag VERSION=0.1.1

# Create GitHub release (normally done by workflow)
make release-github VERSION=0.1.1

# Check that GitHub release exists with all assets
make check-release VERSION=0.1.1

# Publish Python only
make release-python VERSION=0.1.1

# Publish Java only (requires Maven Central credentials)
make release-java

# Publish all enabled bindings
make release-bindings VERSION=0.1.1
```

## Troubleshooting

### Version Mismatch Error

```
ERROR: VERSION=0.1.1 does not match Meta file version: 0.1.0
```

Solution: Run `./util/version-bump` to update all version strings.

### Missing Shared Library Assets

```
ERROR: Missing linux-x64 asset
```

Solution: Wait for the GitHub Actions workflow to complete, or check if it
failed.

### PyPI Upload Fails

```
ERROR: ~/.yamlstar-secrets.yaml not found
```

Solution: Create the secrets file with your PyPI token (see Step 3 above).

### Tag Already Exists

```
ERROR: Tag v0.1.1 already exists
```

Solution: Either delete the tag and release, or bump to a new version.

## Future Enhancements

- Automated version bump and commit in workflow
- Additional language binding releases (npm, crates.io, etc.)
- Python wheels with embedded shared libraries
- CDN distribution for faster downloads
- Automated triggering on version tag push
