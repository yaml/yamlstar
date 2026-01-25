// Copyright 2024 yaml.org
// MIT License

//! Rust binding/API for the libyamlstar shared library.
//!
//! # Loading YAML
//! The [`YAMLStar::load`] function is the main entrypoint of the library.
//! It loads YAML 1.2 content and returns the result as a deserialized type.
//!
//! ## Using `serde_json::Value`
//! ```no_run
//! let ys = yamlstar::YAMLStar::new().unwrap();
//! let data = ys.load::<serde_json::Value>("key: value").unwrap();
//!
//! let data = data.as_object().unwrap();
//! assert_eq!(data.get("key").unwrap().as_str().unwrap(), "value");
//! ```
//!
//! ## Using a user-defined type
//! ```no_run
//! use serde::Deserialize;
//!
//! #[derive(Deserialize)]
//! struct Config {
//!     host: String,
//!     port: u16,
//! }
//!
//! let ys = yamlstar::YAMLStar::new().unwrap();
//! let config = ys.load::<Config>("host: localhost\nport: 8080").unwrap();
//! assert_eq!(config.host, "localhost");
//! assert_eq!(config.port, 8080);
//! ```

#![warn(clippy::pedantic)]

use std::path::Path;

use dlopen::symbor::Library;
use libc::{c_int, c_void as void};

mod error;

pub use error::Error;
use serde::Deserialize;

use crate::error::LibYSError;

/// The name of the YAMLStar library to load.
const LIBYAMLSTAR_BASENAME: &str = "libyamlstar";

/// The version of the yamlstar library this binding works with.
pub const LIBYAMLSTAR_VERSION: &str = "0.1.0";

/// The extension of the YAMLStar library. On Linux, it's a `.so` file.
#[cfg(target_os = "linux")]
const LIBYAMLSTAR_EXTENSION: &str = "so";
/// The extension of the YAMLStar library. On MacOS, it's a `.dylib` file.
#[cfg(target_os = "macos")]
const LIBYAMLSTAR_EXTENSION: &str = "dylib";
/// The extension of the YAMLStar library. On Windows, it's a `.dll` file.
#[cfg(target_os = "windows")]
const LIBYAMLSTAR_EXTENSION: &str = "dll";
#[cfg(not(any(target_os = "linux", target_os = "macos", target_os = "windows")))]
compile_error!("Unsupported platform for yamlstar.");

/// Prototype of the `graal_create_isolate` function.
type CreateIsolateFn = unsafe extern "C" fn(*mut void, *const *mut void, *const *mut void) -> c_int;
/// Prototype of the `graal_tear_down_isolate` function.
type TearDownIsolateFn = unsafe extern "C" fn(*mut void) -> c_int;
/// Prototype of the `yamlstar_load` function.
type YamlstarLoadFn = unsafe extern "C" fn(*mut void, *const u8) -> *mut i8;
/// Prototype of the `yamlstar_load_all` function.
type YamlstarLoadAllFn = unsafe extern "C" fn(*mut void, *const u8) -> *mut i8;
/// Prototype of the `yamlstar_version` function.
type YamlstarVersionFn = unsafe extern "C" fn(*mut void) -> *mut i8;

/// A wrapper around libyamlstar.
pub struct YAMLStar {
    /// A handle to the opened dynamic library.
    _handle: Library,
    /// A GraalVM isolate.
    _isolate: *mut void,
    /// A GraalVM isolate thread.
    isolate_thread: *mut void,
    /// Pointer to the function in GraalVM to create the isolate and its thread.
    _create_isolate_fn: CreateIsolateFn,
    /// Pointer to the function in GraalVM to free an isolate thread.
    tear_down_isolate_fn: TearDownIsolateFn,
    /// Pointer to the `yamlstar_load` function.
    load_fn: YamlstarLoadFn,
    /// Pointer to the `yamlstar_load_all` function.
    load_all_fn: YamlstarLoadAllFn,
    /// Pointer to the `yamlstar_version` function.
    version_fn: YamlstarVersionFn,
}

impl YAMLStar {
    /// Create a new instance of a YAMLStar loader.
    ///
    /// # Errors
    /// Returns an error if we fail to open the library.
    /// Returns [`Error::NotFound`] if the library cannot be found.
    #[allow(clippy::crosspointer_transmute)]
    pub fn new() -> Result<Self, Error> {
        // Open library and create pointers the library needs.
        let handle = Self::open_library()?;
        let isolate = std::ptr::null_mut();
        let isolate_thread = std::ptr::null_mut();

        // Fetch symbols.
        let create_isolate_fn =
            unsafe { handle.ptr_or_null::<CreateIsolateFn>("graal_create_isolate")? };
        let tear_down_isolate_fn =
            unsafe { handle.ptr_or_null::<TearDownIsolateFn>("graal_tear_down_isolate")? };
        let load_fn = unsafe { handle.ptr_or_null::<YamlstarLoadFn>("yamlstar_load")? };
        let load_all_fn =
            unsafe { handle.ptr_or_null::<YamlstarLoadAllFn>("yamlstar_load_all")? };
        let version_fn = unsafe { handle.ptr_or_null::<YamlstarVersionFn>("yamlstar_version")? };

        // Check for null-ness.
        if create_isolate_fn.is_null()
            || tear_down_isolate_fn.is_null()
            || load_fn.is_null()
            || load_all_fn.is_null()
            || version_fn.is_null()
        {
            return Err(Error::Load(dlopen::Error::NullSymbol));
        }

        // Transmute to remove borrow and convert to correct Rust type.
        let create_isolate_fn: CreateIsolateFn = unsafe { std::mem::transmute(*create_isolate_fn) };
        let tear_down_isolate_fn: TearDownIsolateFn =
            unsafe { std::mem::transmute(*tear_down_isolate_fn) };
        let load_fn: YamlstarLoadFn = unsafe { std::mem::transmute(*load_fn) };
        let load_all_fn: YamlstarLoadAllFn = unsafe { std::mem::transmute(*load_all_fn) };
        let version_fn: YamlstarVersionFn = unsafe { std::mem::transmute(*version_fn) };

        // Create GraalVM isolate.
        let x = unsafe {
            (create_isolate_fn)(
                std::ptr::null_mut(),
                &raw const isolate,
                &raw const isolate_thread,
            )
        };
        if x != 0 {
            return Err(Error::GraalVM(x));
        }

        Ok(Self {
            _handle: handle,
            _isolate: isolate,
            isolate_thread,
            _create_isolate_fn: create_isolate_fn,
            tear_down_isolate_fn,
            load_fn,
            load_all_fn,
            version_fn,
        })
    }

    /// Load a YAML string and return the first document, deserialized.
    ///
    /// # Errors
    /// Returns an error if the input string is invalid or YAML parsing fails.
    pub fn load<T>(&self, yaml: &str) -> Result<T, Error>
    where
        T: serde::de::DeserializeOwned,
    {
        let raw = unsafe { std::ffi::CStr::from_ptr(self.load_raw(yaml)?) }.to_str()?;
        let response = serde_json::from_str::<YsResponse<T>>(raw)?;

        match response {
            YsResponse::Data(value) => Ok(value),
            YsResponse::Error(err) => Err(Error::YAMLStar(err)),
        }
    }

    /// Load a YAML string and return all documents, deserialized.
    ///
    /// # Errors
    /// Returns an error if the input string is invalid or YAML parsing fails.
    pub fn load_all<T>(&self, yaml: &str) -> Result<Vec<T>, Error>
    where
        T: serde::de::DeserializeOwned,
    {
        let raw = unsafe { std::ffi::CStr::from_ptr(self.load_all_raw(yaml)?) }.to_str()?;
        let response = serde_json::from_str::<YsResponse<Vec<T>>>(raw)?;

        match response {
            YsResponse::Data(value) => Ok(value),
            YsResponse::Error(err) => Err(Error::YAMLStar(err)),
        }
    }

    /// Get the YAMLStar library version.
    ///
    /// # Errors
    /// Returns an error if the version string cannot be retrieved.
    pub fn version(&self) -> Result<String, Error> {
        let raw = unsafe { (self.version_fn)(self.isolate_thread) };
        if raw.is_null() {
            return Err(Error::Ffi("yamlstar_version: returned null".to_string()));
        }
        let version = unsafe { std::ffi::CStr::from_ptr(raw) }.to_str()?;
        Ok(version.to_string())
    }

    /// Load a YAML string, returning the raw buffer from the library.
    fn load_raw(&self, yaml: &str) -> Result<*mut i8, Error> {
        let input = std::ffi::CString::new(yaml)
            .map_err(|_| Error::Ffi("load: input contains a nil-byte".to_string()))?;
        let json = unsafe { (self.load_fn)(self.isolate_thread, input.as_bytes().as_ptr()) };
        if json.is_null() {
            Err(Error::Ffi("yamlstar_load: returned null".to_string()))
        } else {
            Ok(json)
        }
    }

    /// Load all YAML documents, returning the raw buffer from the library.
    fn load_all_raw(&self, yaml: &str) -> Result<*mut i8, Error> {
        let input = std::ffi::CString::new(yaml)
            .map_err(|_| Error::Ffi("load_all: input contains a nil-byte".to_string()))?;
        let json = unsafe { (self.load_all_fn)(self.isolate_thread, input.as_bytes().as_ptr()) };
        if json.is_null() {
            Err(Error::Ffi(
                "yamlstar_load_all: returned null".to_string(),
            ))
        } else {
            Ok(json)
        }
    }

    /// Open the library found at the first matching path in LD_LIBRARY_PATH.
    fn open_library() -> Result<Library, Error> {
        let mut first_error = None;

        // Build list of search paths
        let mut search_paths: Vec<String> = Vec::new();

        // Check relative to crate for development
        if let Ok(manifest_dir) = std::env::var("CARGO_MANIFEST_DIR") {
            search_paths.push(format!("{manifest_dir}/../libyamlstar/lib"));
        }

        // Check LD_LIBRARY_PATH (Unix) or PATH (Windows)
        let path_var = if cfg!(windows) { "PATH" } else { "LD_LIBRARY_PATH" };
        let path_sep = if cfg!(windows) { ';' } else { ':' };
        if let Ok(library_path) = std::env::var(path_var) {
            for path in library_path.split(path_sep) {
                search_paths.push(path.to_string());
            }
        }

        // Standard locations
        search_paths.push("/usr/local/lib".to_string());
        if let Ok(home) = std::env::var("HOME") {
            search_paths.push(format!("{home}/.local/lib"));
        }

        let library_filename = format!(
            "{}.{}.{}",
            LIBYAMLSTAR_BASENAME, LIBYAMLSTAR_EXTENSION, LIBYAMLSTAR_VERSION
        );

        for path in &search_paths {
            let full_path = Path::new(path).join(&library_filename);
            if !full_path.is_file() {
                continue;
            }
            let library = Library::open(&full_path);

            match library {
                Ok(x) => return Ok(x),
                Err(x) => {
                    if first_error.is_none() {
                        first_error = Some(x);
                    }
                }
            }
        }

        match first_error {
            Some(x) => Err(x.into()),
            None => Err(Error::NotFound),
        }
    }
}

impl Drop for YAMLStar {
    fn drop(&mut self) {
        let res = unsafe { (self.tear_down_isolate_fn)(self.isolate_thread) };
        if res != 0 {
            eprintln!("Warning: Failed to tear down yamlstar's GraalVM isolate");
        }
    }
}

/// A response from the yamlstar library.
#[derive(Deserialize)]
enum YsResponse<T> {
    /// A JSON object containing the result of loading the YAML.
    #[serde(rename = "data")]
    Data(T),
    /// An error object.
    #[serde(rename = "error")]
    Error(LibYSError),
}
