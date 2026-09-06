package yamlstar.plugin;

import java.util.List;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CIntPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

@CContext(SharedPluginHost.Directives.class)
public final class SharedPluginHost {
    private SharedPluginHost() {
    }

    public static String manifest(String api, String name, boolean install) {
        try (CTypeConversion.CCharPointerHolder apiPointer =
                 CTypeConversion.toCString(api);
             CTypeConversion.CCharPointerHolder namePointer =
                 CTypeConversion.toCString(name)) {
            CIntPointer status = StackValue.get(CIntPointer.class);
            CCharPointer output = manifestNative(
                apiPointer.get(), namePointer.get(), install ? 1 : 0,
                status);
            String text = takeOutput(output);
            if (status.read() != 0) {
                throw new IllegalArgumentException(text);
            }
            return text;
        }
    }

    public static String[] parse(
        String api,
        String name,
        String input,
        String options
    ) {
        try (CTypeConversion.CCharPointerHolder apiPointer =
                 CTypeConversion.toCString(api);
             CTypeConversion.CCharPointerHolder namePointer =
                 CTypeConversion.toCString(name);
             CTypeConversion.CCharPointerHolder inputPointer =
                 CTypeConversion.toCString(input);
             CTypeConversion.CCharPointerHolder optionsPointer =
                 CTypeConversion.toCString(options)) {
            CIntPointer status = StackValue.get(CIntPointer.class);
            CCharPointer output = parseNative(
                apiPointer.get(), namePointer.get(), inputPointer.get(),
                optionsPointer.get(), status);
            String text = takeOutput(output);
            if (status.read() == 2) {
                throw new IllegalStateException(text);
            }
            return new String[]{Integer.toString(status.read()), text};
        }
    }

    private static String takeOutput(CCharPointer output) {
        if (output.isNull()) {
            throw new IllegalStateException(
                "Shared plugin host returned a nil output");
        }
        try {
            return CTypeConversion.toJavaString(output);
        } finally {
            freeNative(output);
        }
    }

    @CFunction("yamlstar_host_plugin_manifest")
    private static native CCharPointer manifestNative(
        CCharPointer api,
        CCharPointer name,
        int install,
        CIntPointer status
    );

    @CFunction("yamlstar_host_plugin_parse")
    private static native CCharPointer parseNative(
        CCharPointer api,
        CCharPointer name,
        CCharPointer input,
        CCharPointer options,
        CIntPointer status
    );

    @CFunction("yamlstar_host_plugin_free")
    private static native void freeNative(CCharPointer output);

    public static final class Directives implements CContext.Directives {
        @Override
        public List<String> getHeaderFiles() {
            return List.of("\"yamlstar_plugin_host.h\"");
        }

        @Override
        public List<String> getOptions() {
            String include = System.getProperty("yamlstar.plugin.include");
            if (include == null || include.isEmpty()) {
                throw new IllegalStateException(
                    "yamlstar.plugin.include is not configured");
            }
            return List.of("-D_GNU_SOURCE", "-I" + include);
        }

        @Override
        public List<String> getLibraries() {
            return List.of("yamlstar_plugin_host");
        }

        @Override
        public List<String> getLibraryPaths() {
            String library = System.getProperty("yamlstar.plugin.library");
            if (library == null || library.isEmpty()) {
                throw new IllegalStateException(
                    "yamlstar.plugin.library is not configured");
            }
            return List.of(library);
        }
    }
}
