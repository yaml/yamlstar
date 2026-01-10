include ../common/init.mk
include $M/dotnet.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

PROJECT-FILE := YAMLStar.csproj
TEST-PROJECT-FILE := test/YAMLStar.Tests.csproj

MAKES-CLEAN := \
  bin \
  obj \
  test/bin \
  test/obj \


test: $(DOTNET) $(LIBYAMLSTAR-SO)
	dotnet test $(TEST-PROJECT-FILE)

build: $(DOTNET) $(LIBYAMLSTAR-SO)
	dotnet build $(PROJECT-FILE)
