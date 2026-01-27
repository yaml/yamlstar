{ Copyright 2024 yaml.org
  MIT License }

program yaml_to_json;

{$mode objfpc}{$H+}

uses
  SysUtils, Classes, fpjson, yamlstar;

function ReadFileToString(const FileName: string): string;
var
  FileStream: TFileStream;
  StringStream: TStringStream;
begin
  FileStream := TFileStream.Create(FileName, fmOpenRead);
  try
    StringStream := TStringStream.Create('');
    try
      StringStream.CopyFrom(FileStream, FileStream.Size);
      Result := StringStream.DataString;
    finally
      StringStream.Free;
    end;
  finally
    FileStream.Free;
  end;
end;

var
  ys: TYAMLStar;
  yamlFile: string;
  yamlContent: string;
  data: TJSONData;
begin
  { Get input file from command line or use default }
  if ParamCount >= 1 then
    yamlFile := ParamStr(1)
  else
    yamlFile := '../sample.yaml';

  WriteLn('YAMLStar Example - Loading ', yamlFile, ' and outputting JSON');
  WriteLn;

  { Read YAML file }
  yamlContent := ReadFileToString(yamlFile);

  WriteLn('Input YAML:');
  WriteLn(yamlContent);
  WriteLn;
  WriteLn('---');
  WriteLn;

  { Create YAMLStar instance and load YAML }
  ys := TYAMLStar.Create;
  try
    data := ys.Load(yamlContent);
    try
      WriteLn('Output JSON:');
      WriteLn(data.FormatJSON([], 2));
    finally
      data.Free;
    end;
  finally
    ys.Free;
  end;
end.
