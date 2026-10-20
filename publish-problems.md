# Publishing Problems

Publication issues affecting YAMLStar and YAMLScript.

## Dart: CI publishing is skipped

Status: unresolved as of 2026-10-20.
Manual publishing works for both projects.

Both release helpers explicitly skip Dart when `YS_RELEASE_CI` is set:

```text
Skipping dart binding: pub.dev requires interactive OAuth
```

The condition appears in `util/release-yamlstar` and
`repos/yamlscript/util/release-yamlscript`.
The release workflows do not configure pub.dev publishing authentication.
Local publishing can use the developer's existing Dart login.
The `--force` option suppresses confirmation, but does not authenticate.

### Current workaround

Run this locally from the repository being released:

```bash
make -C dart release
```

This publishes the version currently declared in `dart/pubspec.yaml`.

### Proposed fix

Use pub.dev's GitHub Actions OIDC authentication instead of an interactive
login or copied developer credentials.

- Enable automated publishing in each package's pub.dev Admin tab.
- Configure `yaml/yamlstar` with the tag pattern `v{{version}}`.
- Configure `yaml/yamlscript` with the tag pattern `{{version}}`.
- Add a publishing workflow with `id-token: write` and Dart's OIDC setup.
- Replace the unconditional CI skip with the authenticated publishing path.

Pub.dev requires GitHub Actions publishing to originate from a tag push.
Both current release workflows use `workflow_dispatch`.
Checking out a release tag within those workflows does not satisfy that
requirement.
The fix therefore needs a dedicated tag-triggered workflow or a change to
release triggering, while preserving release validation and publication order.

Reference: [Dart automated publishing documentation][dart-publishing].

[dart-publishing]: https://dart.dev/tools/pub/automated-publishing
