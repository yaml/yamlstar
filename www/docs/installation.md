# Installing YAMLStar

YAMLStar ships two native release artifacts:

1. The `yaml` command-line loader.
2. The `libyamlstar` shared library used by language bindings.

## Quick Install

Install both the CLI and shared library:

```bash
curl -sSL https://yamlstar.org/install | bash
```

Install only the CLI:

```bash
curl -sSL https://yamlstar.org/install | BIN=1 bash
```

Install only the shared library:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```

Install to a custom prefix:

```bash
curl -sSL https://yamlstar.org/install | PREFIX=/opt/yamlstar bash
```

`PREFIX` defaults to `$HOME/.local` for non-root users and `/usr/local` for
root. Add `PREFIX/bin` to your `PATH` to run `yaml` from any directory.

For custom shared-library prefixes, your runtime loader may also need
`PREFIX/lib` in `LD_LIBRARY_PATH` on Linux or `DYLD_LIBRARY_PATH` on macOS.

## Homebrew

YAMLStar can also be installed with Homebrew on Linux x64 and macOS ARM64:

```bash
brew trust yaml/yamlstar
brew tap yaml/yamlstar

brew install yaml/yamlstar/yamlstar
brew install yaml/yamlstar/libyamlstar
```

To install a specific version:

```bash
brew install yaml/yamlstar/yamlstar@0.1.5
brew install yaml/yamlstar/libyamlstar@0.1.5
```

## Release Archives

Release archives are attached to
[GitHub Releases](https://github.com/yaml/yamlstar/releases):

- `yamlstar-VERSION-linux-x64.tar.xz`
- `yamlstar-VERSION-macos-arm64.tar.xz`
- `libyamlstar-VERSION-linux-x64.tar.xz`
- `libyamlstar-VERSION-macos-arm64.tar.xz`

Each archive contains a `Makefile`, so manual installation is:

```bash
tar -xf yamlstar-0.1.5-linux-x64.tar.xz
cd yamlstar-0.1.5-linux-x64
make install PREFIX=$HOME/.local
```
