{ Copyright 2024 yaml.org
  MIT License }

program test_yamlstar;

{$mode objfpc}{$H+}

uses
  SysUtils, fpjson, yamlstar;

var
  ys: TYAMLStar;
  data: TJSONData;
  docs: TJSONArray;
  testCount: Integer = 0;
  passCount: Integer = 0;

procedure Test(const TestName: string; Condition: Boolean);
begin
  Inc(testCount);
  if Condition then
  begin
    Inc(passCount);
    WriteLn('PASS: ', TestName);
  end
  else
    WriteLn('FAIL: ', TestName);
end;

procedure TestVersion;
var
  ver: string;
begin
  ver := ys.Version;
  Test('Version returns non-empty string', Length(ver) > 0);
  WriteLn('  Version: ', ver);
end;

procedure TestLoadString;
begin
  data := ys.Load('hello');
  try
    Test('Load simple string', (data.JSONType = jtString) and (data.AsString = 'hello'));
  finally
    data.Free;
  end;
end;

procedure TestLoadInteger;
begin
  data := ys.Load('42');
  try
    Test('Load integer', (data.JSONType = jtNumber) and (data.AsInteger = 42));
  finally
    data.Free;
  end;
end;

procedure TestLoadFloat;
begin
  data := ys.Load('3.14');
  try
    Test('Load float', (data.JSONType = jtNumber) and (Abs(data.AsFloat - 3.14) < 0.001));
  finally
    data.Free;
  end;
end;

procedure TestLoadBoolean;
begin
  data := ys.Load('true');
  try
    Test('Load boolean true', (data.JSONType = jtBoolean) and data.AsBoolean);
  finally
    data.Free;
  end;

  data := ys.Load('false');
  try
    Test('Load boolean false', (data.JSONType = jtBoolean) and (not data.AsBoolean));
  finally
    data.Free;
  end;
end;

procedure TestLoadNull;
begin
  data := ys.Load('null');
  try
    Test('Load null', data.JSONType = jtNull);
  finally
    data.Free;
  end;
end;

procedure TestLoadMapping;
var
  obj: TJSONObject;
begin
  data := ys.Load('key: value');
  try
    Test('Load mapping returns object', data.JSONType = jtObject);
    if data.JSONType = jtObject then
    begin
      obj := data as TJSONObject;
      Test('Mapping has correct key', obj.Find('key') <> nil);
      Test('Mapping has correct value', obj.Strings['key'] = 'value');
    end;
  finally
    data.Free;
  end;
end;

procedure TestLoadSequence;
var
  arr: TJSONArray;
begin
  data := ys.Load('[a, b, c]');
  try
    Test('Load sequence returns array', data.JSONType = jtArray);
    if data.JSONType = jtArray then
    begin
      arr := data as TJSONArray;
      Test('Sequence has 3 elements', arr.Count = 3);
      if arr.Count = 3 then
      begin
        Test('First element is "a"', arr.Strings[0] = 'a');
        Test('Second element is "b"', arr.Strings[1] = 'b');
        Test('Third element is "c"', arr.Strings[2] = 'c');
      end;
    end;
  finally
    data.Free;
  end;
end;

procedure TestLoadAll;
begin
  docs := ys.LoadAll('---' + LineEnding + 'doc1' + LineEnding + '---' + LineEnding + 'doc2');
  try
    Test('LoadAll returns array', docs.JSONType = jtArray);
    Test('LoadAll has 2 documents', docs.Count = 2);
    if docs.Count = 2 then
    begin
      Test('First document is "doc1"', docs.Strings[0] = 'doc1');
      Test('Second document is "doc2"', docs.Strings[1] = 'doc2');
    end;
  finally
    docs.Free;
  end;
end;

procedure TestNestedStructure;
var
  obj: TJSONObject;
  arr: TJSONArray;
  yamlStr: string;
begin
  yamlStr := 'users:' + LineEnding + '- name: Alice' + LineEnding + '  age: 30' + LineEnding + '- name: Bob' + LineEnding + '  age: 25';
  data := ys.Load(yamlStr);
  try
    Test('Nested structure returns object', data.JSONType = jtObject);
    if data.JSONType = jtObject then
    begin
      obj := data as TJSONObject;
      Test('Has users key', obj.Find('users') <> nil);
      if obj.Find('users') <> nil then
      begin
        Test('Users is array', obj.Arrays['users'].JSONType = jtArray);
        arr := obj.Arrays['users'];
        Test('Users has 2 elements', arr.Count = 2);
      end;
    end;
  finally
    data.Free;
  end;
end;

begin
  WriteLn('YAMLStar Delphi Binding Test Suite');
  WriteLn('===================================');
  WriteLn;

  { Create YAMLStar instance }
  ys := TYAMLStar.Create;
  try
    { Run tests }
    TestVersion;
    WriteLn;
    TestLoadString;
    TestLoadInteger;
    TestLoadFloat;
    TestLoadBoolean;
    TestLoadNull;
    WriteLn;
    TestLoadMapping;
    TestLoadSequence;
    WriteLn;
    TestLoadAll;
    WriteLn;
    TestNestedStructure;
  finally
    ys.Free;
  end;

  { Print summary }
  WriteLn;
  WriteLn('===================================');
  WriteLn(Format('Tests passed: %d / %d', [passCount, testCount]));

  if passCount = testCount then
  begin
    WriteLn('All tests passed!');
    Halt(0);
  end
  else
  begin
    WriteLn('Some tests failed!');
    Halt(1);
  end;
end.
