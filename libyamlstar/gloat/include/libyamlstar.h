#ifndef YAMLSTAR_H
#define YAMLSTAR_H

#include "graal_isolate.h"

#ifdef __cplusplus
extern "C" {
#endif

char *yamlstar_load(
  graal_isolatethread_t *thread, const char *yaml, const char *opts_json);
char *yamlstar_load_all(
  graal_isolatethread_t *thread, const char *yaml, const char *opts_json);
char *yamlstar_dump(
  graal_isolatethread_t *thread, const char *data_json, const char *opts_json);
char *yamlstar_dump_all(
  graal_isolatethread_t *thread, const char *data_json, const char *opts_json);
char *yamlstar_version(graal_isolatethread_t *thread);

#ifdef __cplusplus
}
#endif

#endif
