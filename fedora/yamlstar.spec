%global goipath github.com/yaml/yamlstar
%global yamlstar_version 0.1.21
Version: %{yamlstar_version}

%gometa -L -f

Name:           yamlstar
Release:        %autorelease
Summary:        YAML processing command-line interface

License:        Apache-2.0 AND BSD-3-Clause AND EPL-1.0 AND MIT
URL:            %{gourl}
Source0:        %{gosource}
Source1:        %{archivename}-vendor.tar.bz2
Source2:        go-vendor-tools.toml

BuildRequires:  go-vendor-tools

%description
YAMLStar provides a command-line interface for loading, transforming, and
emitting YAML data.
The yamlstar package installs the command as yaml.

%prep
%goprep -p1
tar -xf %{S:1}

%generate_buildrequires
%go_vendor_license_buildrequires -c %{S:2}

%build
%global gomodulesmode GO111MODULE=on
%gobuild -tags=glj_aot_runtime,glj_no_goimports -o %{gobuilddir}/bin/yaml %{goipath}/cmd/yaml

%install
%go_vendor_license_install -c %{S:2}
install -Dpm 0755 \
  %{gobuilddir}/bin/yaml \
  %{buildroot}%{_bindir}/yaml

%check
%go_vendor_license_check -c %{S:2}
%gotest ./...
test "$(%{gobuilddir}/bin/yaml --version)" = "yaml v%{version}"
test "$(printf 'answer: 42\n' | %{gobuilddir}/bin/yaml -j)" = \
  '{"answer":42}'

%files -f %{go_vendor_license_filelist}
%doc Changes ReadMe.md
%{_bindir}/yaml

%changelog
%autochangelog
