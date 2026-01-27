{ Copyright 2024 yaml.org
  MIT License }

{
  Delphi/Pascal binding for the libyamlstar shared library.

  This unit provides a Pascal interface to YAMLStar, a pure YAML 1.2 loader.
  The TYAMLStar class has methods for loading YAML documents and converting
  them to Pascal objects (via JSON).
}

unit yamlstar;

{$mode objfpc}{$H+}

interface

uses
  SysUtils, fpjson, jsonscanner, jsonparser, yamlstar_native;

const
  { Version matching the yamlstar shared library }
  YAMLSTAR_VERSION = '0.1.2';

type
  { Exception raised when YAMLStar encounters an error }
  EYAMLStarException = class(Exception);

  { Interface to the libyamlstar shared library }
  TYAMLStar = class
  private
    FIsolateThread: Pointer;
    function ParseResponse(const JSONStr: string): TJSONData;
  public
    { Constructor - creates a new GraalVM isolate }
    constructor Create;

    { Destructor - tears down the GraalVM isolate }
    destructor Destroy; override;

    { Load a single YAML document and return the result as JSON data.

      Args:
        YAMLInput: String containing YAML content

      Returns:
        TJSONData object representing the YAML document

      Raises:
        EYAMLStarException if the YAML is malformed

      Note: Caller is responsible for freeing the returned TJSONData }
    function Load(const YAMLInput: string): TJSONData;

    { Load all YAML documents from a multi-document string.

      Args:
        YAMLInput: String containing one or more YAML documents

      Returns:
        TJSONArray containing all documents

      Raises:
        EYAMLStarException if the YAML is malformed

      Note: Caller is responsible for freeing the returned TJSONArray }
    function LoadAll(const YAMLInput: string): TJSONArray;

    { Get the YAMLStar version string.

      Returns:
        Version string }
    function Version: string;
  end;

implementation

{ TYAMLStar }

constructor TYAMLStar.Create;
var
  rc: Integer;
begin
  inherited Create;
  FIsolateThread := nil;

  { Create a new GraalVM isolate }
  rc := yamlstar_native.graal_create_isolate(nil, nil, @FIsolateThread);

  if rc <> 0 then
    raise EYAMLStarException.Create('Failed to create GraalVM isolate');
end;

destructor TYAMLStar.Destroy;
var
  rc: Integer;
begin
  { Tear down the isolate thread to free resources }
  if FIsolateThread <> nil then
  begin
    rc := yamlstar_native.graal_tear_down_isolate(FIsolateThread);
    if rc <> 0 then
      raise EYAMLStarException.Create('Failed to tear down GraalVM isolate');
  end;

  inherited Destroy;
end;

function TYAMLStar.ParseResponse(const JSONStr: string): TJSONData;
var
  Parser: TJSONParser;
  Response: TJSONObject;
  ErrorObj: TJSONObject;
  ErrorMsg: string;
begin
  Result := nil;
  Parser := TJSONParser.Create(JSONStr, [joUTF8]);
  try
    Response := Parser.Parse as TJSONObject;
    try
      { Check for error in response }
      if Response.Find('error') <> nil then
      begin
        ErrorObj := Response.Objects['error'];
        if ErrorObj.Find('cause') <> nil then
          ErrorMsg := ErrorObj.Strings['cause']
        else
          ErrorMsg := 'Unknown error from libyamlstar';
        raise EYAMLStarException.Create(ErrorMsg);
      end;

      { Get the data field }
      if Response.Find('data') = nil then
        raise EYAMLStarException.Create('Unexpected response from libyamlstar');

      Result := Response.Extract('data');
    finally
      Response.Free;
    end;
  finally
    Parser.Free;
  end;
end;

function TYAMLStar.Load(const YAMLInput: string): TJSONData;
var
  JSONResponse: PAnsiChar;
  JSONStr: string;
begin
  { Call yamlstar_load function in libyamlstar shared library }
  JSONResponse := yamlstar_native.yamlstar_load(FIsolateThread, PAnsiChar(AnsiString(YAMLInput)));
  JSONStr := string(JSONResponse);

  { Parse and return the response }
  Result := ParseResponse(JSONStr);
end;

function TYAMLStar.LoadAll(const YAMLInput: string): TJSONArray;
var
  JSONResponse: PAnsiChar;
  JSONStr: string;
  Data: TJSONData;
begin
  { Call yamlstar_load_all function in libyamlstar shared library }
  JSONResponse := yamlstar_native.yamlstar_load_all(FIsolateThread, PAnsiChar(AnsiString(YAMLInput)));
  JSONStr := string(JSONResponse);

  { Parse the response }
  Data := ParseResponse(JSONStr);

  { Ensure it's an array }
  if not (Data is TJSONArray) then
  begin
    Data.Free;
    raise EYAMLStarException.Create('Expected array from load_all');
  end;

  Result := Data as TJSONArray;
end;

function TYAMLStar.Version: string;
var
  PtrResult: PAnsiChar;
begin
  PtrResult := yamlstar_native.yamlstar_version(FIsolateThread);
  Result := string(PtrResult);
end;

end.
