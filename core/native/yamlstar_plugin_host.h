#ifndef YAMLSTAR_PLUGIN_HOST_H
#define YAMLSTAR_PLUGIN_HOST_H

#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <dlfcn.h>
#include <errno.h>
#include <limits.h>
#include <pthread.h>
#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>

#if defined(__APPLE__)
#include <mach-o/dyld.h>
#define YAMLSTAR_PLUGIN_EXTENSION ".dylib"
#else
#define YAMLSTAR_PLUGIN_EXTENSION ".so"
#endif

#ifndef PATH_MAX
#define PATH_MAX 4096
#endif

#define YAMLSTAR_PLUGIN_LIMIT 32
#define YAMLSTAR_SEARCH_LIMIT 8192

typedef uint64_t (*yamlstar_abi_fn)(void);
typedef int32_t (*yamlstar_manifest_fn)(uint8_t **, size_t *);
typedef int32_t (*yamlstar_parse_fn)(
    const uint8_t *, size_t, const uint8_t *, size_t,
    uint8_t **, size_t *);
typedef void (*yamlstar_free_fn)(uint8_t *);

struct yamlstar_host_library {
    char path[PATH_MAX];
    void *handle;
    yamlstar_manifest_fn manifest;
    yamlstar_parse_fn parse;
    yamlstar_free_fn free_output;
};

static struct yamlstar_host_library
    yamlstar_host_libraries[YAMLSTAR_PLUGIN_LIMIT];
static size_t yamlstar_host_library_count;
static pthread_mutex_t yamlstar_host_mutex = PTHREAD_MUTEX_INITIALIZER;

static inline char *yamlstar_host_error(const char *format, ...) {
    va_list arguments;
    va_start(arguments, format);
    va_list copy;
    va_copy(copy, arguments);
    int length = vsnprintf(NULL, 0, format, copy);
    va_end(copy);
    if (length < 0) {
        va_end(arguments);
        return strdup("Shared plugin host formatting failed");
    }
    char *message = malloc((size_t) length + 1);
    if (message != NULL) {
        vsnprintf(message, (size_t) length + 1, format, arguments);
    }
    va_end(arguments);
    return message;
}

static inline int yamlstar_host_valid_name(const char *name) {
    if (name == NULL || *name == '\0') {
        return 0;
    }
    for (const unsigned char *cursor = (const unsigned char *) name;
         *cursor != '\0'; cursor++) {
        if (!((*cursor >= 'a' && *cursor <= 'z')
              || (*cursor >= 'A' && *cursor <= 'Z')
              || (*cursor >= '0' && *cursor <= '9')
              || *cursor == '-' || *cursor == '_')) {
            return 0;
        }
    }
    return 1;
}

static inline void yamlstar_host_add_search(
    char *searched,
    size_t searched_size,
    const char *directory
) {
    if (directory == NULL || *directory == '\0') {
        return;
    }
    size_t used = strlen(searched);
    snprintf(searched + used, searched_size - used, "%s%s",
             used == 0 ? "" : ", ", directory);
}

static inline int yamlstar_host_candidate(
    char *result,
    size_t result_size,
    char *searched,
    size_t searched_size,
    const char *directory,
    const char *filename
) {
    if (directory == NULL || *directory == '\0') {
        return 0;
    }
    yamlstar_host_add_search(searched, searched_size, directory);
    int length = snprintf(result, result_size, "%s/%s",
                          directory, filename);
    return length > 0 && (size_t) length < result_size
        && access(result, R_OK) == 0;
}

static inline void yamlstar_host_dirname(char *path) {
    char *slash = strrchr(path, '/');
    if (slash == NULL) {
        strcpy(path, ".");
    } else if (slash == path) {
        slash[1] = '\0';
    } else {
        *slash = '\0';
    }
}

static inline int yamlstar_host_append(
    char *result,
    size_t result_size,
    const char *base,
    const char *suffix
) {
    size_t base_size = strlen(base);
    size_t suffix_size = strlen(suffix);
    if (base_size + suffix_size >= result_size) {
        return 0;
    }
    memcpy(result, base, base_size);
    memcpy(result + base_size, suffix, suffix_size + 1);
    return 1;
}

static inline int yamlstar_host_installer_candidate(
    char *result,
    size_t result_size,
    const char *directory,
    const char *suffix
) {
    if (!yamlstar_host_append(result, result_size, directory, suffix)) {
        return 0;
    }
    return access(result, X_OK) == 0;
}

static inline int yamlstar_host_find_installer(
    char *result,
    size_t result_size
) {
    const char *configured = getenv("YAMLSTAR_PLUGIN_INSTALLER");
    if (configured != NULL && *configured != '\0') {
        snprintf(result, result_size, "%s", configured);
        return access(result, X_OK) == 0 ? 1 : -1;
    }

    Dl_info info;
    if (dladdr((void *) &yamlstar_host_find_installer, &info) != 0
        && info.dli_fname != NULL) {
        char host[PATH_MAX];
        snprintf(host, sizeof(host), "%s", info.dli_fname);
        yamlstar_host_dirname(host);
        if (yamlstar_host_installer_candidate(
                result, result_size, host, "/yamlstar-plugin")
            || yamlstar_host_installer_candidate(
                result, result_size, host,
                "/../libexec/yamlstar/yamlstar-plugin")) {
            return 1;
        }
    }

    char executable[PATH_MAX] = "";
#if defined(__APPLE__)
    uint32_t executable_size = sizeof(executable);
    if (_NSGetExecutablePath(executable, &executable_size) != 0) {
        executable[0] = '\0';
    }
#elif defined(__linux__) || defined(__FreeBSD__)
    ssize_t executable_size = readlink(
        "/proc/self/exe", executable, sizeof(executable) - 1);
    if (executable_size > 0) {
        executable[executable_size] = '\0';
    }
#endif
    if (*executable != '\0') {
        yamlstar_host_dirname(executable);
        if (yamlstar_host_installer_candidate(
                result, result_size, executable, "/yamlstar-plugin")
            || yamlstar_host_installer_candidate(
                result, result_size, executable,
                "/../libexec/yamlstar/yamlstar-plugin")) {
            return 1;
        }
    }

    snprintf(result, result_size, "%s", "yamlstar-plugin");
    return 0;
}

static inline int yamlstar_host_install(
    const char *api,
    const char *name,
    char **error
) {
    char installer[PATH_MAX];
    int found = yamlstar_host_find_installer(
        installer, sizeof(installer));
    if (found < 0) {
        *error = yamlstar_host_error(
            "YAMLSTAR_PLUGIN_INSTALLER is not executable: %s", installer);
        return 0;
    }

    pid_t child = fork();
    if (child < 0) {
        *error = yamlstar_host_error(
            "Failed to start YAMLStar plugin installer: %s",
            strerror(errno));
        return 0;
    }
    if (child == 0) {
        if (found == 1) {
            execl(installer, installer, "install", api, name, NULL);
        } else {
            execlp(installer, installer, "install", api, name, NULL);
        }
        _exit(127);
    }

    int status = 0;
    while (waitpid(child, &status, 0) < 0) {
        if (errno == EINTR) {
            continue;
        }
        *error = yamlstar_host_error(
            "Failed to wait for YAMLStar plugin installer: %s",
            strerror(errno));
        return 0;
    }
    if (WIFEXITED(status) && WEXITSTATUS(status) == 0) {
        return 1;
    }
    if (WIFEXITED(status) && WEXITSTATUS(status) == 127) {
        *error = yamlstar_host_error(
            "YAMLStar plugin installer was not found; searched beside "
            "the host and in PATH");
        return 0;
    }
    if (WIFEXITED(status)) {
        *error = yamlstar_host_error(
            "YAMLStar plugin installer failed with status %d",
            WEXITSTATUS(status));
    } else if (WIFSIGNALED(status)) {
        *error = yamlstar_host_error(
            "YAMLStar plugin installer was terminated by signal %d",
            WTERMSIG(status));
    } else {
        *error = strdup("YAMLStar plugin installer failed");
    }
    return 0;
}

static inline int yamlstar_host_find(
    const char *name,
    char *result,
    size_t result_size,
    char *searched,
    size_t searched_size
) {
    char filename[PATH_MAX];
    snprintf(filename, sizeof(filename),
             "libyamlstar-plugin-%s%s",
             name, YAMLSTAR_PLUGIN_EXTENSION);

    const char *configured = getenv("YAMLSTAR_LIBRARY_PATH");
    if (configured != NULL) {
        if (*configured != '\0') {
            char *paths = strdup(configured);
            if (paths != NULL) {
                char *state = NULL;
                for (char *directory = strtok_r(paths, ":", &state);
                     directory != NULL;
                     directory = strtok_r(NULL, ":", &state)) {
                    if (yamlstar_host_candidate(
                            result, result_size, searched, searched_size,
                            directory, filename)) {
                        free(paths);
                        return 1;
                    }
                }
                free(paths);
            }
        }
        return 0;
    }

    Dl_info info;
    if (dladdr((void *) &yamlstar_host_find, &info) != 0
        && info.dli_fname != NULL) {
        char host[PATH_MAX];
        snprintf(host, sizeof(host), "%s", info.dli_fname);
        yamlstar_host_dirname(host);
        if (yamlstar_host_candidate(
                result, result_size, searched, searched_size,
                host, filename)) {
            return 1;
        }
    }

    char executable[PATH_MAX] = "";
#if defined(__APPLE__)
    uint32_t executable_size = sizeof(executable);
    if (_NSGetExecutablePath(executable, &executable_size) != 0) {
        executable[0] = '\0';
    }
#elif defined(__linux__) || defined(__FreeBSD__)
    ssize_t executable_size = readlink(
        "/proc/self/exe", executable, sizeof(executable) - 1);
    if (executable_size > 0) {
        executable[executable_size] = '\0';
    }
#endif
    if (*executable != '\0') {
        yamlstar_host_dirname(executable);
        char directory[PATH_MAX];
        if (yamlstar_host_append(
                directory, sizeof(directory), executable,
                "/../lib")
            && yamlstar_host_candidate(
                result, result_size, searched, searched_size,
                directory, filename)) {
            return 1;
        }
    }

    const char *home = getenv("HOME");
    if (home != NULL && *home != '\0') {
        char directory[PATH_MAX];
        snprintf(directory, sizeof(directory),
                 "%s/.local/lib", home);
        if (yamlstar_host_candidate(
                result, result_size, searched, searched_size,
                directory, filename)) {
            return 1;
        }
    }
    if (yamlstar_host_candidate(
            result, result_size, searched, searched_size,
            "/usr/local/lib", filename)) {
        return 1;
    }
    return yamlstar_host_candidate(
        result, result_size, searched, searched_size,
        "/usr/lib", filename);
}

static inline int yamlstar_host_symbol(
    void *handle,
    const char *name,
    void *target
) {
    dlerror();
    void *symbol = dlsym(handle, name);
    const char *error = dlerror();
    if (error != NULL || symbol == NULL) {
        return 0;
    }
    memcpy(target, &symbol, sizeof(symbol));
    return 1;
}

static inline struct yamlstar_host_library *yamlstar_host_load(
    const char *api,
    const char *name,
    int install,
    char **error
) {
    if (!yamlstar_host_valid_name(api) || !yamlstar_host_valid_name(name)) {
        *error = yamlstar_host_error(
            "Invalid YAMLStar plugin selector %s=%s", api, name);
        return NULL;
    }
    char path[PATH_MAX];
    char searched[YAMLSTAR_SEARCH_LIMIT] = "";
    int plugin_found = yamlstar_host_find(
        name, path, sizeof(path), searched, sizeof(searched));
    if (!plugin_found && install) {
        if (!yamlstar_host_install(api, name, error)) {
            return NULL;
        }
        searched[0] = '\0';
        plugin_found = yamlstar_host_find(
            name, path, sizeof(path), searched, sizeof(searched));
    }
    if (!plugin_found) {
        *error = yamlstar_host_error(
            "YAMLStar plugin %s=%s "
            "(libyamlstar-plugin-%s%s) was not found; searched: %s",
            api, name, name, YAMLSTAR_PLUGIN_EXTENSION, searched);
        return NULL;
    }

    pthread_mutex_lock(&yamlstar_host_mutex);
    for (size_t index = 0; index < yamlstar_host_library_count; index++) {
        if (strcmp(yamlstar_host_libraries[index].path, path) == 0) {
            pthread_mutex_unlock(&yamlstar_host_mutex);
            return &yamlstar_host_libraries[index];
        }
    }
    if (yamlstar_host_library_count >= YAMLSTAR_PLUGIN_LIMIT) {
        pthread_mutex_unlock(&yamlstar_host_mutex);
        *error = strdup("Too many YAMLStar shared plugins are loaded");
        return NULL;
    }

    struct yamlstar_host_library *library =
        &yamlstar_host_libraries[yamlstar_host_library_count];
    library->handle = dlopen(path, RTLD_NOW | RTLD_LOCAL);
    if (library->handle == NULL) {
        *error = yamlstar_host_error(
            "Failed to load YAMLStar plugin %s: %s", path, dlerror());
        pthread_mutex_unlock(&yamlstar_host_mutex);
        return NULL;
    }
    yamlstar_abi_fn abi = NULL;
    int valid = yamlstar_host_symbol(
                    library->handle, "yamlstar_plugin_v1_abi", &abi)
        && yamlstar_host_symbol(
            library->handle, "yamlstar_plugin_v1_manifest",
            &library->manifest)
        && yamlstar_host_symbol(
            library->handle, "yamlstar_plugin_v1_parse", &library->parse)
        && yamlstar_host_symbol(
            library->handle, "yamlstar_plugin_v1_free",
            &library->free_output);
    if (!valid) {
        *error = yamlstar_host_error(
            "YAMLStar plugin %s is missing a version 1 ABI symbol", path);
        dlclose(library->handle);
        library->handle = NULL;
        pthread_mutex_unlock(&yamlstar_host_mutex);
        return NULL;
    }
    uint64_t version = abi();
    if (version != 1) {
        *error = yamlstar_host_error(
            "YAMLStar plugin %s has ABI %llu, expected 1",
            path, (unsigned long long) version);
        dlclose(library->handle);
        library->handle = NULL;
        pthread_mutex_unlock(&yamlstar_host_mutex);
        return NULL;
    }
    snprintf(library->path, sizeof(library->path), "%s", path);
    yamlstar_host_library_count++;
    pthread_mutex_unlock(&yamlstar_host_mutex);
    return library;
}

static inline char *yamlstar_host_copy_output(
    struct yamlstar_host_library *library,
    uint8_t *output,
    size_t length
) {
    if (output == NULL && length != 0) {
        return strdup("Shared plugin returned a nil output");
    }
    char *copy = malloc(length + 1);
    if (copy == NULL) {
        if (output != NULL) {
            library->free_output(output);
        }
        return NULL;
    }
    if (length != 0) {
        memcpy(copy, output, length);
    }
    copy[length] = '\0';
    if (output != NULL) {
        library->free_output(output);
    }
    return copy;
}

#ifdef YAMLSTAR_PLUGIN_HOST_IMPLEMENTATION
#define YAMLSTAR_PLUGIN_HOST_LINKAGE
#else
#define YAMLSTAR_PLUGIN_HOST_LINKAGE static inline
#endif

YAMLSTAR_PLUGIN_HOST_LINKAGE char *yamlstar_host_plugin_manifest(
    const char *api,
    const char *name,
    int32_t install,
    int32_t *status
) {
    char *error = NULL;
    struct yamlstar_host_library *library =
        yamlstar_host_load(api, name, install != 0, &error);
    if (library == NULL) {
        *status = 2;
        return error;
    }
    uint8_t *output = NULL;
    size_t length = 0;
    *status = library->manifest(&output, &length);
    return yamlstar_host_copy_output(library, output, length);
}

YAMLSTAR_PLUGIN_HOST_LINKAGE char *yamlstar_host_plugin_parse(
    const char *api,
    const char *name,
    const char *input,
    const char *options,
    int32_t *status
) {
    char *error = NULL;
    struct yamlstar_host_library *library =
        yamlstar_host_load(api, name, 0, &error);
    if (library == NULL) {
        *status = 2;
        return error;
    }
    uint8_t *output = NULL;
    size_t length = 0;
    *status = library->parse(
        (const uint8_t *) input, strlen(input),
        (const uint8_t *) options, strlen(options),
        &output, &length);
    return yamlstar_host_copy_output(library, output, length);
}

YAMLSTAR_PLUGIN_HOST_LINKAGE void yamlstar_host_plugin_free(char *output) {
    free(output);
}

#undef YAMLSTAR_PLUGIN_HOST_LINKAGE

#endif
