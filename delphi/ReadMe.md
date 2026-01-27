# YAMLStar Delphi/Pascal Bindings

Delphi/Pascal bindings for YAMLStar - a pure YAML 1.2 loader implemented in
Clojure.

## Features

- **YAML 1.2 Spec Compliance**: 100% compliant with YAML 1.2 core schema
- **Pure Implementation**: No dependencies on external YAML parsers
- **Fast Native Performance**: Uses GraalVM native-image shared library
- **Simple API**: Load YAML documents with a single method call
- **Multi-Document Support**: Load multiple YAML documents from a single string
- **Free Pascal Compatible**: Works with Free Pascal Compiler (FPC)

## Installation

### Prerequisites

First, build and install the shared library:

```bash
cd ../libyamlstar
make native
sudo make install PREFIX=/usr/local
```

Or install to user-local directory:

```bash
cd ../libyamlstar
make native
make install PREFIX=~/.local
```

### Requirements

- **Free Pascal Compiler (FPC)**: 3.0 or higher
  - Install from: https://www.freepascal.org/
  - Or via package manager: `apt install fpc` (Debian/Ubuntu) or `brew install fpc` (macOS)
- **libyamlstar**: Shared library (installed separately)
- **System**: Linux or macOS

## Building

```bash
# Build the binding
make build

# Run tests
make test
```

## Quick Start

```pascal
program example;
uses yamlstar, fpjson;
var
  ys: TYAMLStar;
  data: TJSONData;
begin
  ys := TYAMLStar.Create;
  try
    data := ys.Load('key: value');
    try
      WriteLn(data.FormatJSON);
    finally
      data.Free;
    end;
  finally
    ys.Free;
  end;
end.
```

## Usage Examples

### Basic Types

```pascal
uses yamlstar, fpjson;
var
  ys: TYAMLStar;
  data: TJSONData;
begin
  ys := TYAMLStar.Create;
  try
    // Strings
    data := ys.Load('hello');
    WriteLn(data.AsString);  // 'hello'
    data.Free;

    // Integers
    data := ys.Load('42');
    WriteLn(data.AsInteger);  // 42
    data.Free;

    // Floats
    data := ys.Load('3.14');
    WriteLn(data.AsFloat);  // 3.14
    data.Free;

    // Booleans
    data := ys.Load('true');
    WriteLn(data.AsBoolean);  // True
    data.Free;

    // Null
    data := ys.Load('null');
    WriteLn(data.JSONType = jtNull);  // True
    data.Free;
  finally
    ys.Free;
  end;
end.
```

### Collections

```pascal
var
  ys: TYAMLStar;
  data: TJSONData;
  obj: TJSONObject;
  arr: TJSONArray;
begin
  ys := TYAMLStar.Create;
  try
    // Mappings (objects)
    data := ys.Load('name: Alice' + LineEnding + 'age: 30');
    try
      obj := data as TJSONObject;
      WriteLn(obj.Strings['name']);  // 'Alice'
      WriteLn(obj.Integers['age']);  // 30
    finally
      data.Free;
    end;

    // Sequences (arrays)
    data := ys.Load('[apple, banana, orange]');
    try
      arr := data as TJSONArray;
      WriteLn(arr.Count);  // 3
      WriteLn(arr.Strings[0]);  // 'apple'
    finally
      data.Free;
    end;
  finally
    ys.Free;
  end;
end.
```

### Nested Structures

```pascal
var
  ys: TYAMLStar;
  data: TJSONData;
  obj: TJSONObject;
  yaml: string;
begin
  ys := TYAMLStar.Create;
  try
    yaml := 'person:' + LineEnding +
            '  name: Alice' + LineEnding +
            '  age: 30' + LineEnding +
            '  hobbies:' + LineEnding +
            '    - reading' + LineEnding +
            '    - coding';

    data := ys.Load(yaml);
    try
      WriteLn(data.FormatJSON);
    finally
      data.Free;
    end;
  finally
    ys.Free;
  end;
end.
```

### Multi-Document YAML

```pascal
var
  ys: TYAMLStar;
  docs: TJSONArray;
  yaml: string;
  i: Integer;
begin
  ys := TYAMLStar.Create;
  try
    yaml := '---' + LineEnding +
            'name: Document 1' + LineEnding +
            '---' + LineEnding +
            'name: Document 2';

    docs := ys.LoadAll(yaml);
    try
      for i := 0 to docs.Count - 1 do
        WriteLn(docs.Items[i].FormatJSON);
    finally
      docs.Free;
    end;
  finally
    ys.Free;
  end;
end.
```

### Error Handling

```pascal
uses yamlstar, SysUtils;
var
  ys: TYAMLStar;
  data: TJSONData;
begin
  ys := TYAMLStar.Create;
  try
    try
      data := ys.Load('invalid: yaml: syntax');
      data.Free;
    except
      on E: EYAMLStarException do
        WriteLn('Error loading YAML: ', E.Message);
    end;
  finally
    ys.Free;
  end;
end.
```

### Version Information

```pascal
var
  ys: TYAMLStar;
begin
  ys := TYAMLStar.Create;
  try
    WriteLn('YAMLStar version: ', ys.Version);
  finally
    ys.Free;
  end;
end.
```

## API Reference

### `TYAMLStar` Class

#### `constructor Create`
Create a new YAMLStar instance.
Each instance maintains its own GraalVM isolate.

```pascal
ys := TYAMLStar.Create;
```

#### `destructor Destroy`
Destroys the YAMLStar instance and tears down the GraalVM isolate.
Always call `Free` or use a try-finally block to ensure cleanup.

```pascal
ys.Free;
```

#### `function Load(const YAMLInput: string): TJSONData`
Load a single YAML document.

**Parameters:**
- `YAMLInput`: String containing YAML content

**Returns:**
- TJSONData object representing the YAML document (TJSONObject, TJSONArray,
  TJSONString, TJSONIntegerNumber, TJSONFloatNumber, TJSONBoolean, or
  TJSONNull)

**Raises:**
- `EYAMLStarException` if the YAML is malformed

**Note:** Caller is responsible for freeing the returned TJSONData.

**Example:**
```pascal
data := ys.Load('key: value');
try
  // Use data
finally
  data.Free;
end;
```

#### `function LoadAll(const YAMLInput: string): TJSONArray`
Load all YAML documents from a multi-document string.

**Parameters:**
- `YAMLInput`: String containing one or more YAML documents

**Returns:**
- TJSONArray containing all documents

**Raises:**
- `EYAMLStarException` if the YAML is malformed

**Note:** Caller is responsible for freeing the returned TJSONArray.

**Example:**
```pascal
docs := ys.LoadAll('---' + LineEnding + 'doc1' + LineEnding + '---' + LineEnding + 'doc2');
try
  // Use docs
finally
  docs.Free;
end;
```

#### `function Version: string`
Get the YAMLStar version string.

**Returns:**
- Version string

**Example:**
```pascal
version := ys.Version;
```

## Library Search Path

The binding searches for `libyamlstar.so.0` (or `.0.dylib` on macOS) using
Free Pascal's standard library loading mechanism.
You can set `LD_LIBRARY_PATH` to specify custom search paths:

```bash
export LD_LIBRARY_PATH=/path/to/lib:$LD_LIBRARY_PATH
```

## Compilation

To compile programs using the YAMLStar binding:

```bash
fpc -Fusrc -Fl/path/to/libyamlstar/lib your_program.pas
```

Where:
- `-Fu` specifies the unit search path
- `-Fl` specifies the library search path

## License

MIT License - See [License](License) file

## Credits

Created by Ingy döt Net, inventor of YAML.

YAMLStar is built on the YAML Reference Parser (pure Clojure implementation).

## Links

- **GitHub**: https://github.com/yaml/yamlstar
- **YAML Specification**: https://yaml.org/spec/1.2/spec.html
