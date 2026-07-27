#ifndef YAMLSTAR_H
#define YAMLSTAR_H

#include "graal_isolate.h"

#ifdef __cplusplus
extern "C" {
#endif

char *yamlstar_load(graal_isolatethread_t *thread, const char *yaml);
char *yamlstar_load_all(graal_isolatethread_t *thread, const char *yaml);
char *yamlstar_dump(graal_isolatethread_t *thread, const char *data_json);
char *yamlstar_dump_all(graal_isolatethread_t *thread, const char *data_json);
char *yamlstar_version(graal_isolatethread_t *thread);

#ifdef __cplusplus
}
#endif

#endif
