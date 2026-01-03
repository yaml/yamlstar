// Copyright 2024 yaml.org
// MIT License

use std::{
    error::Error as StdError,
    fmt::{Debug, Display},
    str::Utf8Error,
};

use serde::Deserialize;

/// An error with the binding.
pub enum Error {
    /// The library was not found.
    NotFound,
    /// An error while loading the library.
    Load(dlopen::Error),
    /// An error with GraalVM.
    GraalVM(i32),
    /// An error in the FFI while calling a libyamlstar function.
    Ffi(String),
    /// An error from the libyamlstar library.
    YAMLStar(LibYSError),
    /// An error with serde_json while deserializing.
    Serde(serde_json::Error),
    /// An error while decoding strings returned from libyamlstar.
    Utf8(Utf8Error),
}

impl Debug for Error {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::NotFound => write!(
                f,
                "Shared library file 'libyamlstar.so.{0}' not found\n\
                Try: cd libyamlstar && make native\n\
                Or set LD_LIBRARY_PATH to include the library location",
                &super::LIBYAMLSTAR_VERSION
            ),
            Error::Load(e) => write!(f, "Error::Load({e:?})"),
            Error::GraalVM(e) => write!(f, "Error::GraalVM({e:?})"),
            Error::Ffi(e) => write!(f, "Error::Ffi({e:?})"),
            Error::YAMLStar(e) => write!(f, "Error::YAMLStar({e:?})"),
            Error::Serde(e) => write!(f, "Error::Serde({e:?})"),
            Error::Utf8(e) => write!(f, "Error::Utf8({e:?})"),
        }
    }
}

impl Display for Error {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::NotFound => write!(
                f,
                "Shared library file 'libyamlstar.so.{0}' not found",
                &super::LIBYAMLSTAR_VERSION
            ),
            Error::Load(e) => write!(f, "Failed to load library: {e}"),
            Error::GraalVM(code) => write!(f, "GraalVM error (code {code})"),
            Error::Ffi(msg) => write!(f, "FFI error: {msg}"),
            Error::YAMLStar(e) => write!(f, "YAML parsing error: {}", e.cause),
            Error::Serde(e) => write!(f, "JSON deserialization error: {e}"),
            Error::Utf8(e) => write!(f, "UTF-8 decoding error: {e}"),
        }
    }
}

impl StdError for Error {
    fn source(&self) -> Option<&(dyn StdError + 'static)> {
        match self {
            Error::Load(e) => Some(e),
            Error::Serde(e) => Some(e),
            Error::Utf8(e) => Some(e),
            _ => None,
        }
    }
}

/// An error from libyamlstar.
#[allow(clippy::module_name_repetitions)]
#[derive(Deserialize, Debug)]
pub struct LibYSError {
    /// The error message.
    pub cause: String,
    /// The internal type of the error.
    #[serde(rename = "type")]
    pub type_: String,
    /// Optional error message.
    pub message: Option<String>,
}

impl From<dlopen::Error> for Error {
    fn from(value: dlopen::Error) -> Self {
        Self::Load(value)
    }
}

impl From<serde_json::Error> for Error {
    fn from(value: serde_json::Error) -> Self {
        Self::Serde(value)
    }
}

impl From<Utf8Error> for Error {
    fn from(value: Utf8Error) -> Self {
        Self::Utf8(value)
    }
}
