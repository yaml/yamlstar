// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

// C shim for the R yamlstar package.
//
// Loads the libyamlstar shared library at first use and exposes its
// yamlstar_load function to R via the .Call interface. The search
// paths and exact version pinning are ported from the Python
// reference implementation.

#ifdef _WIN32
#include <windows.h>
#else
#include <dlfcn.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <R.h>
#include <Rinternals.h>

// This value is automatically updated by 'make bump'.
// We currently only support binding to an exact version of libyamlstar:
#define YAMLSTAR_VERSION "0.1.12"

#ifdef _WIN32
#define LIBYAMLSTAR_NAME "libyamlstar.dll"
#elif defined(__APPLE__)
#define LIBYAMLSTAR_NAME "libyamlstar.dylib." YAMLSTAR_VERSION
#else
#define LIBYAMLSTAR_NAME "libyamlstar.so." YAMLSTAR_VERSION
#endif

typedef int (*create_isolate_fn)(void *, void **, void **);
typedef int (*tear_down_isolate_fn)(void *);
typedef char *(*yamlstar_load_fn)(void *, const char *);

static void *libyamlstar = NULL;
static create_isolate_fn create_isolate;
static tear_down_isolate_fn tear_down_isolate;
static yamlstar_load_fn yamlstar_load;

// Return 1 if the libyamlstar file exists in dir and fills path:
static int check_dir(const char *dir, char *path, size_t size) {
  FILE *file;

  snprintf(path, size, "%s/%s", dir, LIBYAMLSTAR_NAME);
  file = fopen(path, "r");
  if (file == NULL) return 0;
  fclose(file);
  return 1;
}

// Find the libyamlstar shared library file path.
// Search LD_LIBRARY_PATH entries (PATH on Windows), then common
// install locations:
static int find_libyamlstar(char *path, size_t size) {
#ifdef _WIN32
  const char *library_path = getenv("PATH");
  const char *sep = ";";
#else
  const char *library_path = getenv("LD_LIBRARY_PATH");
  const char *sep = ":";
#endif
  const char *home;

  if (library_path != NULL) {
    char *paths = strdup(library_path);
    char *dir = strtok(paths, sep);
    while (dir != NULL) {
      if (check_dir(dir, path, size)) {
        free(paths);
        return 1;
      }
      dir = strtok(NULL, sep);
    }
    free(paths);
  }

#ifdef _WIN32
  home = getenv("USERPROFILE");
#else
  if (check_dir("/usr/local/lib", path, size)) return 1;

  home = getenv("HOME");
#endif
  if (home != NULL) {
    char dir[4096];
    snprintf(dir, sizeof(dir), "%s/.local/lib", home);
    if (check_dir(dir, path, size)) return 1;
  }

  return 0;
}

// Open libyamlstar and resolve the 3 symbols used by this binding:
static void open_libyamlstar(void) {
  char path[4096];

  if (libyamlstar != NULL) return;

  if (!find_libyamlstar(path, sizeof(path))) {
    Rf_error(
      "Shared library file '%s' not found\n"
      "Try: curl https://yamlstar.org/install |"
      " VERSION=%s LIB=1 bash\n"
      "See: https://github.com/yaml/yamlstar/wiki/"
      "Installing-YAMLStar",
      LIBYAMLSTAR_NAME, YAMLSTAR_VERSION);
  }

#ifdef _WIN32
  libyamlstar = (void *)LoadLibraryA(path);
#else
  libyamlstar = dlopen(path, RTLD_NOW);
#endif
  if (libyamlstar == NULL) {
    Rf_error("Failed to load shared library '%s'", path);
  }

#ifdef _WIN32
#define LIBYAMLSTAR_SYM(name) ((void *)GetProcAddress((HMODULE)libyamlstar, name))
#else
#define LIBYAMLSTAR_SYM(name) (dlsym(libyamlstar, name))
#endif

  create_isolate =
    (create_isolate_fn)LIBYAMLSTAR_SYM("graal_create_isolate");
  tear_down_isolate =
    (tear_down_isolate_fn)LIBYAMLSTAR_SYM("graal_tear_down_isolate");
  yamlstar_load =
    (yamlstar_load_fn)LIBYAMLSTAR_SYM("yamlstar_load");

  if (create_isolate == NULL || tear_down_isolate == NULL ||
      yamlstar_load == NULL) {
    Rf_error("Required symbols not found in libyamlstar");
  }
}

// Load a YAML string, returning the raw JSON
// response string. A GraalVM isolate is created and torn down per
// call (like the Java binding):
SEXP C_yamlstar_load(SEXP input) {
  void *isolate = NULL;
  void *thread = NULL;
  const char *json;
  SEXP result;

  open_libyamlstar();

  if (create_isolate(NULL, &isolate, &thread) != 0) {
    Rf_error("Failed to create isolate");
  }

  json = yamlstar_load(
    thread, CHAR(STRING_ELT(input, 0)));

  result = Rf_mkString(json == NULL ? "" : json);

  if (tear_down_isolate(thread) != 0) {
    Rf_error("Failed to tear down isolate");
  }

  return result;
}
