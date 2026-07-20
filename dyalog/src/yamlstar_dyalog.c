#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define YAMLSTAR_VERSION "0.1.16"

typedef int (*graal_create_isolate_fn)(void *, void *, void *);
typedef int (*graal_tear_down_isolate_fn)(void *);
typedef char *(*yamlstar_load_fn)(void *, char *);

static void *libyamlstar = NULL;
static void *isolate_thread = NULL;
static graal_tear_down_isolate_fn tear_down_isolate = NULL;
static yamlstar_load_fn load_yaml = NULL;

static int copy_output(char *output, int max, const char *text) {
  int len = (int)strlen(text);
  if (max <= 0) {
    return -len;
  }
  if (len >= max) {
    memcpy(output, text, (size_t)max - 1);
    output[max - 1] = '\0';
    return -len;
  }
  memcpy(output, text, (size_t)len + 1);
  return len;
}

static int copy_error(char *output, int max, const char *message) {
  char buffer[4096];
  snprintf(
    buffer,
    sizeof(buffer),
    "{\"error\":{\"cause\":\"%s\"}}",
    message
  );
  return copy_output(output, max, buffer);
}

static int open_libyamlstar(char *output, int max) {
  if (load_yaml != NULL) {
    return 0;
  }

  const char *names[] = {
    "libyamlstar.so." YAMLSTAR_VERSION,
    "libyamlstar.so",
    NULL,
  };

  const char *env = getenv("YAMLSTAR_DYALOG_LIBYAMLSTAR");
  if (env != NULL) {
    libyamlstar = dlopen(env, RTLD_LAZY);
  }

  for (int i = 0; names[i] != NULL; i++) {
    if (libyamlstar != NULL) {
      break;
    }
    libyamlstar = dlopen(names[i], RTLD_LAZY);
    if (libyamlstar != NULL) {
      break;
    }
  }

  if (libyamlstar == NULL) {
    return copy_error(output, max, dlerror());
  }

  graal_create_isolate_fn create_isolate =
    (graal_create_isolate_fn)dlsym(libyamlstar, "graal_create_isolate");
  tear_down_isolate =
    (graal_tear_down_isolate_fn)dlsym(libyamlstar, "graal_tear_down_isolate");
  load_yaml = (yamlstar_load_fn)dlsym(libyamlstar, "yamlstar_load");

  if (
    create_isolate == NULL ||
    tear_down_isolate == NULL ||
    load_yaml == NULL
  ) {
    return copy_error(output, max, "required libyamlstar symbol not found");
  }

  if (create_isolate(NULL, NULL, &isolate_thread) != 0) {
    return copy_error(output, max, "failed to create GraalVM isolate");
  }

  return 0;
}

int yamlstar_load_json(const char *input, char *output, int max) {
  int rc = open_libyamlstar(output, max);
  if (rc != 0) {
    return rc;
  }

  char *json = load_yaml(isolate_thread, (char *)input);
  if (json == NULL) {
    return copy_error(output, max, "null response from libyamlstar");
  }

  return copy_output(output, max, json);
}

int yamlstar_close(void) {
  if (tear_down_isolate == NULL || isolate_thread == NULL) {
    return 0;
  }
  int rc = tear_down_isolate(isolate_thread);
  isolate_thread = NULL;
  return rc;
}
