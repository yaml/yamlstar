# YAMLStar Plugin ABI v2

## Purpose

ABI v2 is the native shared-library boundary between YAMLStar and a binary
plugin.
It is not the public `libyamlstar` language-binding ABI, and it is not the
in-process plugin API used by Clojure or Go code.

The ABI is designed for a plugin that accepts a complete byte input, applies
one operation, and returns a complete byte output.
The JSON-comments sanitizer is the current example.

ABI v2 does not expose YAMLStar nodes, parser objects, decoder state, anchor
tables, or callbacks into the host.
It also has no per-load plugin instance or document lifecycle.

## Required symbols

Every ABI v2 library exports these four C symbols:

```c
uint64_t yamlstar_plugin_v2_abi(void);

int32_t yamlstar_plugin_v2_manifest(
    uint8_t **output,
    size_t *output_length);

int32_t yamlstar_plugin_v2_transform(
    const uint8_t *input,
    size_t input_length,
    const uint8_t *options_edn,
    size_t options_length,
    uint8_t **output,
    size_t *output_length);

void yamlstar_plugin_v2_free(uint8_t *output);
```

`yamlstar_plugin_v2_abi` returns `2`.
The host rejects a library that omits a required symbol or returns another ABI
version.

## Manifest

`yamlstar_plugin_v2_manifest` returns a UTF-8 EDN mapping.
The current host expects this shape:

```clojure
{:abi 2
 :api "json-comments"
 :name "sanitizer"
 :version "0.1.9"
 :kind "text-transform"}
```

The host validates `:abi`, `:api`, `:name`, and `:kind` against the selected
plugin.
The plugin version is independent of the YAMLStar version.

## Transform call

The host passes the complete input as an arbitrary byte buffer.
It passes plugin configuration as UTF-8 EDN in the second buffer.
The current text-transform host treats a successful result as UTF-8 text.

The call returns one of these statuses:

- `0` means success and the output contains the transformed data.
- `1` means the plugin rejected the input or configuration and the output
  contains its error message.
- `2` means an ABI, allocation, or internal failure and the output may contain
  a diagnostic message.

Lengths are authoritative.
Inputs do not need a trailing NUL byte, and outputs may contain NUL bytes even
though the current text host expects UTF-8 text.

## Memory ownership

Input buffers remain owned by the host and are valid only for the duration of
the call.
The plugin allocates every non-nil output buffer, including error messages.
The host copies the returned bytes and calls `yamlstar_plugin_v2_free` exactly
once.

A zero-length result may use a nil output pointer.
A nonzero length with a nil pointer is an ABI failure.

## Loading and discovery

The native host loads `libyamlstar-plugin-NAME.so` on ELF systems and
`libyamlstar-plugin-NAME.dylib` on macOS.
It searches `YAMLSTAR_LIBRARY_PATH` when set, then host-relative and standard
installation directories.

When installation is allowed and the library is missing, the host invokes the
`yamlstar-plugin` installer or `YAMLSTAR_PLUGIN_INSTALLER`.
The installer maps an official plugin name to its release artifacts.

Loaded libraries are cached by path for the life of the process.
Loading is protected by a host mutex, but transform calls are not globally
serialized.
A plugin must therefore make its transform implementation safe for concurrent
calls.

## Appropriate uses

ABI v2 is a good fit when all required state can be represented by the input,
options, and output buffers.
Examples include source sanitizers, format conversion, and complete parser or
emitter operations with a declared wire format.

It is a poor fit for policy hooks that operate on host-owned objects throughout
a load.
Alias-Data belongs in the in-process plugin API because it manages anchor state
at stream and document boundaries and resolves aliases to native host values.
