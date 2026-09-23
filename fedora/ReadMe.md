# Fedora packaging

The Fedora package is built from a Git source archive and a source-only Go
vendor archive.
The `rpmbuild` step runs in Fedora Rawhide with networking disabled.

Build the RPM and SRPM with either Docker or Podman:

```sh
make fedora-rpm
```

Build, lint, install, and smoke-test the package:

```sh
make test-fedora-rpm
```

Set `DOCKER-OR-PODMAN=docker` or `DOCKER-OR-PODMAN=podman` to select a
container runtime explicitly.
Artifacts are written below `.cache/fedora/RPMS` and
`.cache/fedora/SRPMS`.

The package contains `/usr/bin/yaml`, documentation, and license files.
It does not package the YAMLStar shared library, headers, bindings, or plugin
installer.
The RPM build consumes the checked-in generated Go sources and does not run
Gloat, Clojure, a JVM, a JAR, Makes, or any checkout under `repos/`.

Go's vendor command omits the mixed-case license file from `ys-v0-glj` and
the `gojava` release references a license file that is missing from its tag.
The files below `fedora/licenses/` preserve those upstream MIT notices and are
added to the source-only vendor archive.

Packit, COPR, Koji, Bodhi, and package publication are intentionally outside
this initial packaging workflow.
