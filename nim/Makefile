include ../common/init.mk
include $M/nim.mk
include $(COMMON)/common.mk
include $(COMMON)/binding.mk


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

test:: $(LIBYAMLSTAR-SO-VERSION) $(NIM)
	nim c -r --hints:off --nimcache:.nimcache -p:src \
	    -o:tests/test_yamlstar.bin tests/test_yamlstar.nim

release-deps::
	@true

clean::
	$(RM) -r .nimcache tests/test_yamlstar.bin
