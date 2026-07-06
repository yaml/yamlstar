include ../common/init.mk
include $M/r.mk
ifeq ($(OS-NAME),windows)
# R CMD INSTALL needs a C toolchain to build the src/ shim; use the
# xpack gcc from the makes gcc.mk:
include $M/gcc.mk
R-TOOLCHAIN := $(GCC)
endif
include $(COMMON)/common.mk
include $(COMMON)/binding.mk

RLIB := .rlib


#------------------------------------------------------------------------------
build:: build-doc

build-doc:: ReadMe.md

# Use R (not Rscript): the relocated Rscript binary has the build
# prefix compiled in and cannot find its R home. Avoid --vanilla,
# which would skip the Rprofile.site relocation shim from r.mk:
R-RUN := R --no-save --no-restore --no-init-file --no-echo

test:: $(LIBYAMLSTAR-SO-VERSION) $(R) $(R-TOOLCHAIN)
	mkdir -p $(RLIB)
	R_LIBS="$(RLIB)" $(R-RUN) -f tests/install-deps.R
	R CMD INSTALL --library=$(RLIB) .
	R_LIBS="$(RLIB)" $(R-RUN) -f tests/test-yamlstar.R

release-deps:: $(R)

clean::
	$(RM) -r $(RLIB) src/*.o src/*.so
