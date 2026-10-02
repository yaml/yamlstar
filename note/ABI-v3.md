# Proposal: YAMLStar Plugin ABI v3

## Status

This document is a design proposal.
It does not define a released ABI, and no ABI v3 implementation currently
exists.

ABI v2 remains the supported native plugin boundary.
An ABI v3 should be implemented only when a real plugin requires capabilities
that cannot be expressed safely by ABI v2.

## Goals

ABI v3 should support more than one named operation and should optionally
support state that lasts for one host operation.
It should retain a small C surface, explicit memory ownership, language-neutral
payloads, and side-by-side compatibility with ABI v2.

The boundary should remain serialization-based.
It should not expose host pointers, Go values, Clojure values, YAMLStar nodes,
or callbacks into the host.

## Proposed symbols

```c
uint64_t yamlstar_plugin_v3_abi(void);

int32_t yamlstar_plugin_v3_manifest(
    uint8_t **output,
    size_t *output_length);

int32_t yamlstar_plugin_v3_open(
    const uint8_t *options,
    size_t options_length,
    uint64_t *session,
    uint8_t **output,
    size_t *output_length);

int32_t yamlstar_plugin_v3_call(
    uint64_t session,
    const uint8_t *operation,
    size_t operation_length,
    const uint8_t *input,
    size_t input_length,
    uint8_t **output,
    size_t *output_length);

int32_t yamlstar_plugin_v3_close(
    uint64_t session,
    uint8_t **output,
    size_t *output_length);

void yamlstar_plugin_v3_free(uint8_t *output);
```

The ABI function returns `3`.
The manifest, open, call, close, and free symbols are required.

`open` validates options and creates an operation-local session.
A stateless implementation may return session `0`.
`call` invokes one operation declared by the manifest.
`close` releases the session and is called exactly once after a successful
open, including when a later call fails.

## Manifest

The manifest remains UTF-8 EDN and declares the operations and their payload
formats.
For example:

```clojure
{:abi 3
 :api "example-api"
 :name "example"
 :version "1.0.0"
 :kind "operations"
 :options-format "edn"
 :operations
 {"inspect" {:input-format "yamlstar-events-binary-v1"
             :output-format "edn"}
  "rewrite" {:input-format "bytes"
             :output-format "bytes"}}
 :concurrency "independent-sessions"}
```

Format names are protocol identifiers, not MIME-type guesses.
The host must reject an operation or format it does not understand before
calling the plugin.

## Status and errors

ABI v3 retains the v2 status meanings:

- `0` means success.
- `1` means a plugin input, configuration, or operation error.
- `2` means an ABI or internal failure.

An open, call, or close failure may return a UTF-8 diagnostic in its output
buffer.
Machine-readable plugin errors should be declared as an operation output
format rather than inferred from text.

## Ownership and limits

Input buffers are borrowed for one call.
Output buffers are plugin-owned until the host copies them and invokes the v3
free function.
Sessions are plugin-owned opaque integers and must never encode host pointers.

The host should apply configured limits to option, input, and output sizes.
The plugin should reject unknown options and operations.
Both sides must allow a close call after a recoverable operation error.

## Concurrency

The manifest declares one of these initial policies:

- `stateless` permits concurrent calls with session `0`.
- `independent-sessions` permits concurrent sessions but serializes calls made
  on the same session.
- `serialized` requires the host to serialize all calls into the library.

The host owns enforcement of the declared policy.
The plugin remains responsible for its internal synchronization.

## Compatibility

YAMLStar should continue loading ABI v2 symbols for existing plugins.
An ABI v3 library uses only v3 symbol names and may also export a complete v2
surface when it can support both contracts.
The host selects one complete ABI and never mixes symbols between versions.

Plugin API versions remain independent from ABI versions.
Changing a plugin operation or payload schema does not necessarily require an
ABI revision when the manifest can describe the change.

## Deliberate exclusions

ABI v3 does not provide host callbacks or direct access to parser, composer,
resolver, constructor, serializer, or emitter objects.
It does not attempt to make every in-process plugin API distributable as a
shared library.

Policy APIs such as Alias-Data should stay in-process when their purpose is to
control host-owned lifecycle and native values.
A future plugin should justify ABI v3 by demonstrating that serialized named
operations and operation-local sessions are both necessary and sufficient.

## Questions to validate with a real plugin

Before implementation, a concrete plugin should determine whether the first
version also needs cancellation, chunked streaming, progress reporting, or
host-provided resource limits.
Those features should not be added speculatively because each one changes the
host and threading contract substantially.
