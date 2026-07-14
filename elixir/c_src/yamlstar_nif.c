// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

// Erlang NIF shim for the Elixir yamlstar binding.
//
// The BEAM cannot call arbitrary C functions directly, so this small
// NIF dlopens the libyamlstar shared library and exposes its
// yamlstar_load function as a dirty-CPU NIF. The library search
// paths and exact version pinning are ported from the Python
// reference implementation.
//
// A GraalVM isolate is created and torn down per call (the same
// pattern as the Java and R bindings), which keeps the NIF safe to
// call from any BEAM scheduler thread.

#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <erl_nif.h>

// This value is automatically updated by 'make bump'.
// We currently only support binding to an exact version of libyamlstar:
#define YAMLSTAR_VERSION "0.1.15"

#ifdef __APPLE__
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
static char load_error[512] = "";

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
// Search LD_LIBRARY_PATH entries, then common install locations:
static int find_libyamlstar(char *path, size_t size) {
  const char *library_path = getenv("LD_LIBRARY_PATH");
  const char *home;

  if (library_path != NULL) {
    char *paths = strdup(library_path);
    char *dir = strtok(paths, ":");
    while (dir != NULL) {
      if (check_dir(dir, path, size)) {
        free(paths);
        return 1;
      }
      dir = strtok(NULL, ":");
    }
    free(paths);
  }

  if (check_dir("/usr/local/lib", path, size)) return 1;

  home = getenv("HOME");
  if (home != NULL) {
    char dir[4096];
    snprintf(dir, sizeof(dir), "%s/.local/lib", home);
    if (check_dir(dir, path, size)) return 1;
  }

  return 0;
}

// Open libyamlstar and resolve the 3 symbols used by this binding.
// On failure, load_error is set and libyamlstar stays NULL:
static void open_libyamlstar(void) {
  char path[4096];

  if (!find_libyamlstar(path, sizeof(path))) {
    snprintf(load_error, sizeof(load_error),
      "Shared library file '%s' not found\n"
      "Try: curl https://yamlstar.org/install |"
      " VERSION=%s LIB=1 bash\n"
      "See: https://github.com/yaml/yamlstar/wiki/"
      "Installing-YAMLStar",
      LIBYAMLSTAR_NAME, YAMLSTAR_VERSION);
    return;
  }

  libyamlstar = dlopen(path, RTLD_NOW);
  if (libyamlstar == NULL) {
    snprintf(load_error, sizeof(load_error),
      "Failed to load shared library '%s'", path);
    return;
  }

  create_isolate =
    (create_isolate_fn)dlsym(libyamlstar, "graal_create_isolate");
  tear_down_isolate =
    (tear_down_isolate_fn)dlsym(libyamlstar, "graal_tear_down_isolate");
  yamlstar_load =
    (yamlstar_load_fn)dlsym(libyamlstar, "yamlstar_load");

  if (create_isolate == NULL || tear_down_isolate == NULL ||
      yamlstar_load == NULL) {
    snprintf(load_error, sizeof(load_error),
      "Required symbols not found in libyamlstar");
    dlclose(libyamlstar);
    libyamlstar = NULL;
  }
}

// Build an {:error, binary} tuple:
static ERL_NIF_TERM error_tuple(ErlNifEnv *env, const char *message) {
  ErlNifBinary bin;
  size_t len = strlen(message);

  enif_alloc_binary(len, &bin);
  memcpy(bin.data, message, len);
  return enif_make_tuple2(env,
    enif_make_atom(env, "error"),
    enif_make_binary(env, &bin));
}

// Load a YAML string, returning the raw JSON
// response as a binary, or {:error, binary} if libyamlstar is unusable:
static ERL_NIF_TERM yamlstar_load_nif(
  ErlNifEnv *env, int argc, const ERL_NIF_TERM argv[]
) {
  ErlNifBinary input, output;
  char *input_z;
  const char *json;
  void *isolate = NULL;
  void *thread = NULL;
  size_t len;

  if (argc != 1 || !enif_inspect_binary(env, argv[0], &input)) {
    return enif_make_badarg(env);
  }

  if (libyamlstar == NULL) {
    return error_tuple(env, load_error);
  }

  // Null-terminate the input binary:
  input_z = enif_alloc(input.size + 1);
  memcpy(input_z, input.data, input.size);
  input_z[input.size] = '\0';

  if (create_isolate(NULL, &isolate, &thread) != 0) {
    enif_free(input_z);
    return error_tuple(env, "Failed to create isolate");
  }

  json = yamlstar_load(thread, input_z);
  enif_free(input_z);

  if (json == NULL) {
    tear_down_isolate(thread);
    return error_tuple(env, "Null response from 'libyamlstar'");
  }

  len = strlen(json);
  enif_alloc_binary(len, &output);
  memcpy(output.data, json, len);

  if (tear_down_isolate(thread) != 0) {
    enif_release_binary(&output);
    return error_tuple(env, "Failed to tear down isolate");
  }

  return enif_make_binary(env, &output);
}

static int load(
  ErlNifEnv *env, void **priv_data, ERL_NIF_TERM load_info
) {
  (void)env;
  (void)priv_data;
  (void)load_info;
  open_libyamlstar();
  return 0;
}

static ErlNifFunc nif_funcs[] = {
  {"nif_yamlstar_load", 1, yamlstar_load_nif,
   ERL_NIF_DIRTY_JOB_CPU_BOUND},
};

ERL_NIF_INIT(Elixir.YAMLStar, nif_funcs, load, NULL, NULL, NULL)
