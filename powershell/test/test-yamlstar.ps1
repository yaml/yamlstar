Import-Module "$PSScriptRoot/../YAMLStar.psd1" -Force

$data = Invoke-YAMLStar "test: 42"

if ($data.test -ne 42) {
  throw 'load yaml failed'
}

Write-Output 'ok - load yaml'
