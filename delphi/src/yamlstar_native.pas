{ Copyright 2024 yaml.org
  MIT License }

unit yamlstar_native;

{$mode objfpc}{$H+}

interface

uses
  ctypes;

const
  {$IFDEF LINUX}
  LIBYAMLSTAR = 'libyamlstar.so.0';
  {$ENDIF}
  {$IFDEF DARWIN}
  LIBYAMLSTAR = 'libyamlstar.0.dylib';
  {$ENDIF}
  {$IFDEF WINDOWS}
  LIBYAMLSTAR = 'libyamlstar.dll';
  {$ENDIF}

{ Create a new GraalVM isolate }
function graal_create_isolate(params: Pointer; isolate: PPointer;
  isolate_thread: PPointer): cint; cdecl; external LIBYAMLSTAR;

{ Tear down a GraalVM isolate }
function graal_tear_down_isolate(isolate_thread: Pointer): cint;
  cdecl; external LIBYAMLSTAR;

{ Load a single YAML document }
function yamlstar_load(isolate_thread: Pointer; yaml: PAnsiChar): PAnsiChar;
  cdecl; external LIBYAMLSTAR;

{ Load all YAML documents from a multi-document string }
function yamlstar_load_all(isolate_thread: Pointer; yaml: PAnsiChar): PAnsiChar;
  cdecl; external LIBYAMLSTAR;

{ Get the YAMLStar version string }
function yamlstar_version(isolate_thread: Pointer): PAnsiChar;
  cdecl; external LIBYAMLSTAR;

implementation

end.
