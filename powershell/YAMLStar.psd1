@{
  RootModule = 'YAMLStar.psm1'
  ModuleVersion = '0.1.17'
  GUID = '3169069f-8994-44b9-b086-8384419f45c4'
  Author = 'YAMLStar Contributors'
  CompanyName = 'YAMLStar'
  Copyright = '(c) 2026 YAMLStar Contributors'
  Description = 'YAMLStar language binding for PowerShell'
  PowerShellVersion = '7.0'
  FunctionsToExport = @('Invoke-YAMLStar', 'Invoke-YAMLStarJson')
  CmdletsToExport = @()
  VariablesToExport = '*'
  AliasesToExport = @()
  PrivateData = @{
    PSData = @{
      Tags = @('yaml', 'yamlstar')
      LicenseUri = 'https://github.com/yaml/yamlstar/blob/main/License'
      ProjectUri = 'https://github.com/yaml/yamlstar'
    }
  }
}
