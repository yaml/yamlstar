<?php

namespace YAMLStar;

use FFI;
use RuntimeException;

class YAMLStar
{
    private const VERSION = '0.1.15';

    private static ?FFI $ffi = null;
    private static ?string $libPath = null;

    private $isolateThread = null;
    public ?array $error = null;

    public function __construct(?string $libPath = null)
    {
        if (!class_exists('FFI')) {
            throw new RuntimeException('PHP FFI extension is required');
        }

        if ($libPath !== null) {
            self::$libPath = $libPath;
        } elseif (self::$libPath === null) {
            self::$libPath = self::findLibrary();
        }

        if (self::$ffi === null) {
            self::$ffi = FFI::cdef('
                int graal_create_isolate(void* params, void** isolate, void** thread);
                int graal_tear_down_isolate(void* thread);
                char* yamlstar_load(void* thread, const char* input);
                char* yamlstar_load_all(void* thread, const char* input);
                char* yamlstar_dump(void* thread, const char* input);
                char* yamlstar_dump_all(void* thread, const char* input);
                char* yamlstar_version(void* thread);
            ', self::$libPath);
        }

        $isolatePtr = FFI::addr(FFI::new('void*'));
        $threadPtr = FFI::addr(FFI::new('void*'));
        $result = self::$ffi->graal_create_isolate(null, $isolatePtr, $threadPtr);
        if ($result !== 0) {
            throw new RuntimeException('Failed to create GraalVM isolate');
        }

        $this->isolateThread = $threadPtr[0];
    }

    public function __destruct()
    {
        $this->close();
    }

    public function close(): void
    {
        if ($this->isolateThread !== null) {
            $result = self::$ffi->graal_tear_down_isolate($this->isolateThread);
            $this->isolateThread = null;
            if ($result !== 0) {
                throw new RuntimeException('Failed to tear down GraalVM isolate');
            }
        }
    }

    public function load(string $input)
    {
        return $this->callYaml('yamlstar_load', $input);
    }

    public function loadAll(string $input): array
    {
        return $this->callYaml('yamlstar_load_all', $input);
    }

    public function dump($value): string
    {
        return $this->callJson('yamlstar_dump', $value);
    }

    public function dumpAll(array $values): string
    {
        return $this->callJson('yamlstar_dump_all', $values);
    }

    public function version(): string
    {
        return FFI::string(self::$ffi->yamlstar_version($this->isolateThread));
    }

    private function callYaml(string $function, string $input)
    {
        $result = self::$ffi->$function($this->isolateThread, $input);
        return $this->handleResponse($result);
    }

    private function callJson(string $function, $value)
    {
        $json = json_encode($value);
        if ($json === false) {
            throw new RuntimeException('Failed to encode value as JSON');
        }

        $result = self::$ffi->$function($this->isolateThread, $json);
        return $this->handleResponse($result);
    }

    private function handleResponse($result)
    {
        if ($result === null) {
            throw new RuntimeException('libyamlstar returned null');
        }

        $response = json_decode(FFI::string($result), true);
        if (!is_array($response)) {
            throw new RuntimeException('Invalid response from libyamlstar');
        }

        $this->error = $response['error'] ?? null;
        if ($this->error !== null) {
            throw new RuntimeException('libyamlstar: ' . $this->error['cause']);
        }

        if (!array_key_exists('data', $response)) {
            throw new RuntimeException("Unexpected response from 'libyamlstar'");
        }

        return $response['data'];
    }

    private static function findLibrary(): string
    {
        $extension = PHP_OS_FAMILY === 'Darwin'
            ? 'dylib'
            : (PHP_OS_FAMILY === 'Windows' ? 'dll' : 'so');
        $names = [
            "libyamlstar.$extension",
            "libyamlstar.$extension." . self::VERSION,
        ];

        $paths = [
            realpath(__DIR__ . '/../../libyamlstar/lib') ?: '',
        ];
        $envName = PHP_OS_FAMILY === 'Windows' ? 'PATH' : 'LD_LIBRARY_PATH';
        $envPath = getenv($envName);
        if ($envPath !== false) {
            $paths = array_merge($paths, explode(PATH_SEPARATOR, $envPath));
        }
        if (PHP_OS_FAMILY !== 'Windows') {
            $paths[] = '/usr/local/lib';
        }
        $home = getenv('HOME');
        if ($home !== false) {
            $paths[] = "$home/.local/lib";
        }

        foreach ($paths as $path) {
            if ($path === '') {
                continue;
            }
            foreach ($names as $name) {
                $candidate = "$path/$name";
                if (file_exists($candidate)) {
                    return $candidate;
                }
            }
        }

        throw new RuntimeException(
            "Shared library file libyamlstar.$extension not found. " .
            'Install it with: curl -sSL https://yamlstar.org/install | LIB=1 bash'
        );
    }
}
