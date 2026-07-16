// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <erl_nif.h>

#ifndef LIBYAMLSTAR_VERSION
#define LIBYAMLSTAR_VERSION "0.0.0"
#endif

#ifdef __APPLE__
#define LIBYAMLSTAR_NAME "libyamlstar.dylib." LIBYAMLSTAR_VERSION
#else
#define LIBYAMLSTAR_NAME "libyamlstar.so." LIBYAMLSTAR_VERSION
#endif

typedef int (*create_isolate_fn)(void *, void **, void **);
typedef int (*tear_down_isolate_fn)(void *);
typedef char *(*yamlstar_load_fn)(void *, const char *);

static void *libyamlstar = NULL;
static create_isolate_fn create_isolate;
static tear_down_isolate_fn tear_down_isolate;
static yamlstar_load_fn yamlstar_load;
static char load_error[512] = "";

static int check_dir(const char *dir, char *path, size_t size) {
  FILE *file;

  snprintf(path, size, "%s/%s", dir, LIBYAMLSTAR_NAME);
  file = fopen(path, "r");
  if (file == NULL) return 0;
  fclose(file);
  return 1;
}

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

static void open_libyamlstar(void) {
  char path[4096];

  if (!find_libyamlstar(path, sizeof(path))) {
    snprintf(load_error, sizeof(load_error),
      "Shared library file '%s' not found", LIBYAMLSTAR_NAME);
    return;
  }

  libyamlstar = dlopen(path, RTLD_NOW);
  if (libyamlstar == NULL) {
    snprintf(load_error, sizeof(load_error),
      "Failed to load shared library");
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

static ERL_NIF_TERM error_tuple(ErlNifEnv *env, const char *message) {
  ErlNifBinary bin;
  size_t len = strlen(message);

  enif_alloc_binary(len, &bin);
  memcpy(bin.data, message, len);
  return enif_make_tuple2(env,
    enif_make_atom(env, "error"),
    enif_make_binary(env, &bin));
}

static ERL_NIF_TERM load_json_nif(
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

  if (libyamlstar == NULL) return error_tuple(env, load_error);

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
    return error_tuple(env, "Null response from libyamlstar");
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

static int load(ErlNifEnv *env, void **priv_data, ERL_NIF_TERM info) {
  (void)env;
  (void)priv_data;
  (void)info;
  open_libyamlstar();
  return 0;
}

static ErlNifFunc funcs[] = {
  {"nif_load_json", 1, load_json_nif, ERL_NIF_DIRTY_JOB_CPU_BOUND},
};

ERL_NIF_INIT(yamlstar, funcs, load, NULL, NULL, NULL)
