//go:build darwin || freebsd || linux

// Package pluginloader loads YAMLStar event-source shared libraries.
package pluginloader

import (
	"bufio"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"sort"
	"strings"
	"sync"
	"unsafe"

	"github.com/ebitengine/purego"
)

const pluginPrefix = "libyamlstar-plugin-"

type library struct {
	path     string
	abi      func() uint64
	manifest func(**byte, *uintptr) int32
	parse    func(*byte, uintptr, *byte, uintptr, **byte, *uintptr) int32
	free     func(*byte)
}

var libraries = struct {
	sync.Mutex
	byPath map[string]*library
}{byPath: map[string]*library{}}

// Manifest returns the EDN manifest for the requested plugin.
func Manifest(api, name string, install bool) (string, error) {
	library, err := load(api, name, install)
	if err != nil {
		return "", err
	}
	return library.callManifest()
}

// Parse invokes a plugin and returns its EDN response and status code.
func Parse(api, name, input, options string) (string, int64, error) {
	library, err := load(api, name, false)
	if err != nil {
		return "", 2, err
	}
	output, status, err := library.callParse([]byte(input), []byte(options))
	return output, int64(status), err
}

func load(api, name string, install bool) (*library, error) {
	if err := validateName(api); err != nil {
		return nil, fmt.Errorf("invalid plugin API %q: %w", api, err)
	}
	if err := validateName(name); err != nil {
		return nil, fmt.Errorf("invalid plugin name %q: %w", name, err)
	}
	path, searched, err := findLibrary(name)
	if err != nil && install {
		if installErr := installPlugin(api, name, searched); installErr != nil {
			return nil, installErr
		}
		path, searched, err = findLibrary(name)
	}
	if err != nil {
		return nil, fmt.Errorf(
			"YAMLStar plugin %s=%s (%s) was not found; searched: %s",
			api, name, libraryFilename(name), strings.Join(searched, ", "))
	}

	libraries.Lock()
	defer libraries.Unlock()
	if loaded := libraries.byPath[path]; loaded != nil {
		return loaded, nil
	}
	loaded, err := open(path)
	if err != nil {
		return nil, err
	}
	libraries.byPath[path] = loaded
	return loaded, nil
}

func installPlugin(api, name string, searched []string) error {
	installer, err := findInstaller()
	if err != nil {
		return err
	}
	environment := os.Environ()
	if _, present := os.LookupEnv("YAMLSTAR_LIBRARY_PATH"); !present {
		environment = append(environment, "YAMLSTAR_LIBRARY_PATH="+
			strings.Join(searched, string(os.PathListSeparator)))
	}
	process, err := os.StartProcess(installer,
		[]string{installer, "install", api, name},
		&os.ProcAttr{
			Env:   environment,
			Files: []*os.File{os.Stdin, os.Stdout, os.Stderr},
		})
	if err != nil {
		return fmt.Errorf("start YAMLStar plugin installer: %w", err)
	}
	state, err := process.Wait()
	if err != nil {
		return fmt.Errorf("wait for YAMLStar plugin installer: %w", err)
	}
	if !state.Success() {
		return fmt.Errorf("YAMLStar plugin installer failed: %s", state)
	}
	return nil
}

func findInstaller() (string, error) {
	if configured := os.Getenv("YAMLSTAR_PLUGIN_INSTALLER"); configured != "" {
		if executableFile(configured) {
			return configured, nil
		}
		return "", fmt.Errorf(
			"YAMLSTAR_PLUGIN_INSTALLER is not executable: %s", configured)
	}

	var candidates []string
	for _, host := range hostLibraryDirectories() {
		candidates = append(candidates,
			filepath.Join(host, "yamlstar-plugin"),
			filepath.Join(host, "..", "libexec", "yamlstar",
				"yamlstar-plugin"))
	}
	if executable, err := os.Executable(); err == nil {
		directory := filepath.Dir(executable)
		candidates = append(candidates,
			filepath.Join(directory, "yamlstar-plugin"),
			filepath.Join(directory, "..", "libexec", "yamlstar",
				"yamlstar-plugin"))
	}
	for _, directory := range filepath.SplitList(os.Getenv("PATH")) {
		if directory != "" {
			candidates = append(candidates,
				filepath.Join(directory, "yamlstar-plugin"))
		}
	}
	for _, candidate := range candidates {
		if executableFile(candidate) {
			absolute, err := filepath.Abs(candidate)
			if err != nil {
				return "", err
			}
			return absolute, nil
		}
	}
	return "", errors.New(
		"YAMLStar plugin installer was not found; searched beside " +
			"the host and in PATH")
}

func executableFile(path string) bool {
	info, err := os.Stat(path)
	return err == nil && info.Mode().IsRegular() &&
		info.Mode().Perm()&0111 != 0
}

func open(path string) (loaded *library, err error) {
	handle, err := purego.Dlopen(path, purego.RTLD_NOW|purego.RTLD_LOCAL)
	if err != nil {
		return nil, fmt.Errorf("load YAMLStar plugin %s: %w", path, err)
	}
	loaded = &library{path: path}
	defer func() {
		if value := recover(); value != nil {
			loaded = nil
			err = fmt.Errorf("load YAMLStar plugin %s: %v", path, value)
		}
	}()
	purego.RegisterLibFunc(&loaded.abi, handle,
		"yamlstar_plugin_v1_abi")
	purego.RegisterLibFunc(&loaded.manifest, handle,
		"yamlstar_plugin_v1_manifest")
	purego.RegisterLibFunc(&loaded.parse, handle,
		"yamlstar_plugin_v1_parse")
	purego.RegisterLibFunc(&loaded.free, handle,
		"yamlstar_plugin_v1_free")
	if abi := loaded.abi(); abi != 1 {
		return nil, fmt.Errorf(
			"YAMLStar plugin %s has ABI %d, expected 1", path, abi)
	}
	return loaded, nil
}

func (loaded *library) callManifest() (string, error) {
	var output *byte
	var length uintptr
	status := loaded.manifest(&output, &length)
	return loaded.takeOutput(status, output, length)
}

func (loaded *library) callParse(
	input, options []byte,
) (string, int32, error) {
	var output *byte
	var length uintptr
	status := loaded.parse(firstByte(input), uintptr(len(input)),
		firstByte(options), uintptr(len(options)), &output, &length)
	runtime.KeepAlive(input)
	runtime.KeepAlive(options)
	text, err := loaded.takeOutput(status, output, length)
	return text, status, err
}

func (loaded *library) takeOutput(
	status int32, output *byte, length uintptr,
) (string, error) {
	if output == nil && length != 0 {
		return "", fmt.Errorf(
			"YAMLStar plugin %s returned a nil output", loaded.path)
	}
	if output != nil {
		defer loaded.free(output)
	}
	text := string(unsafe.Slice(output, length))
	if status == 2 {
		return text, fmt.Errorf(
			"YAMLStar plugin %s reported an ABI failure: %s",
			loaded.path, text)
	}
	return text, nil
}

func firstByte(data []byte) *byte {
	if len(data) == 0 {
		return nil
	}
	return &data[0]
}

func validateName(name string) error {
	if name == "" {
		return errors.New("name is empty")
	}
	for _, char := range name {
		if !((char >= 'a' && char <= 'z') ||
			(char >= 'A' && char <= 'Z') ||
			(char >= '0' && char <= '9') ||
			char == '-' || char == '_') {
			return fmt.Errorf("unsupported character %q", char)
		}
	}
	return nil
}

func libraryFilename(name string) string {
	extension := ".so"
	if runtime.GOOS == "darwin" {
		extension = ".dylib"
	}
	return pluginPrefix + name + extension
}

func findLibrary(name string) (string, []string, error) {
	searched := searchDirectories()
	filename := libraryFilename(name)
	for _, directory := range searched {
		path := filepath.Join(directory, filename)
		info, err := os.Stat(path)
		if err == nil && !info.IsDir() {
			absolute, absErr := filepath.Abs(path)
			if absErr != nil {
				return "", searched, absErr
			}
			return absolute, searched, nil
		}
	}
	return "", searched, os.ErrNotExist
}

func searchDirectories() []string {
	if configured, present := os.LookupEnv("YAMLSTAR_LIBRARY_PATH"); present {
		return uniqueDirectories(filepath.SplitList(configured))
	}
	return defaultSearchDirectories()
}

// DefaultPath returns the platform's standard plugin search path.
func DefaultPath() string {
	return strings.Join(defaultSearchDirectories(),
		string(os.PathListSeparator))
}

func defaultSearchDirectories() []string {
	var directories []string
	for _, host := range hostLibraryDirectories() {
		directories = append(directories, host)
	}
	if executable, err := os.Executable(); err == nil {
		directories = append(directories,
			filepath.Join(filepath.Dir(executable), "..", "lib"))
	}
	if home, err := os.UserHomeDir(); err == nil {
		directories = append(directories,
			filepath.Join(home, ".local", "lib"))
	}
	directories = append(directories,
		"/usr/local/lib",
		"/usr/lib")
	return uniqueDirectories(directories)
}

func uniqueDirectories(directories []string) []string {
	seen := map[string]bool{}
	unique := make([]string, 0, len(directories))
	for _, directory := range directories {
		if directory == "" {
			continue
		}
		directory = filepath.Clean(directory)
		if !seen[directory] {
			seen[directory] = true
			unique = append(unique, directory)
		}
	}
	return unique
}

func hostLibraryDirectories() []string {
	if runtime.GOOS != "linux" {
		return nil
	}
	file, err := os.Open("/proc/self/maps")
	if err != nil {
		return nil
	}
	defer file.Close()
	seen := map[string]bool{}
	var directories []string
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		fields := strings.Fields(scanner.Text())
		if len(fields) < 6 {
			continue
		}
		path := fields[len(fields)-1]
		base := filepath.Base(path)
		if strings.HasPrefix(base, "libyamlstar") &&
			strings.Contains(base, ".so") {
			directory := filepath.Dir(path)
			if !seen[directory] {
				seen[directory] = true
				directories = append(directories, directory)
			}
		}
	}
	sort.Strings(directories)
	return directories
}
