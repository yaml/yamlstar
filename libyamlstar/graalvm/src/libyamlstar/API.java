// Copyright 2024 yaml.org
// MIT License

package libyamlstar;

import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.c.type.CConst;

public final class API {

    /**
     * Load a single YAML document and return JSON.
     *
     * @param isolateId The GraalVM isolate thread context
     * @param yamlStr The YAML string to parse
     * @return JSON string: {"data": ...} on success, {"error": {...}} on failure
     */
    @CEntryPoint(name = "yamlstar_load")
    public static @CConst CCharPointer load(
        @CEntryPoint.IsolateThreadContext long isolateId,
        @CConst CCharPointer yamlStr
    ) {
        debug("API - called yamlstar_load");

        String yaml = CTypeConversion.toJavaString(yamlStr);
        debug("API - java input string: " + yaml);

        String json = libyamlstar.core.loadYaml(yaml);
        debug("API - java response string: " + json);

        try (CTypeConversion.CCharPointerHolder holder =
                CTypeConversion.toCString(json)) {
            return holder.get();
        }
    }

    /**
     * Load all YAML documents and return JSON array.
     *
     * @param isolateId The GraalVM isolate thread context
     * @param yamlStr The YAML string containing one or more documents
     * @return JSON string: {"data": [...]} on success, {"error": {...}} on failure
     */
    @CEntryPoint(name = "yamlstar_load_all")
    public static @CConst CCharPointer loadAll(
        @CEntryPoint.IsolateThreadContext long isolateId,
        @CConst CCharPointer yamlStr
    ) {
        debug("API - called yamlstar_load_all");

        String yaml = CTypeConversion.toJavaString(yamlStr);
        debug("API - java input string: " + yaml);

        String json = libyamlstar.core.loadYamlAll(yaml);
        debug("API - java response string: " + json);

        try (CTypeConversion.CCharPointerHolder holder =
                CTypeConversion.toCString(json)) {
            return holder.get();
        }
    }

    /**
     * Get the YAMLStar version string.
     *
     * @param isolateId The GraalVM isolate thread context
     * @return Version string
     */
    @CEntryPoint(name = "yamlstar_version")
    public static @CConst CCharPointer version(
        @CEntryPoint.IsolateThreadContext long isolateId
    ) {
        String ver = libyamlstar.core.version();

        try (CTypeConversion.CCharPointerHolder holder =
                CTypeConversion.toCString(ver)) {
            return holder.get();
        }
    }

    private static void debug(String s) {
        if (System.getenv("YAMLSTAR_DEBUG") != null) {
            System.err.println(s);
        }
    }
}
