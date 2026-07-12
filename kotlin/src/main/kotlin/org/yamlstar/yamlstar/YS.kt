// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

// Kotlin binding/API for the libyamlstar shared library.
//
// This is a thin idiomatic Kotlin layer over the Java binding (the
// com.yaml:yamlstar artifact.
//
// The current user facing API is the YS object, whose load() function
// takes a YAML string as input and returns the value that YAMLStar loads.

package org.yamlstar.yamlstar

import com.yaml.YAMLStar

object YS {
    // This value is automatically updated by 'make bump'.
    const val YAMLSTAR_VERSION = "0.1.14"

    /** Load a YAML string and return the result. */
    fun load(input: String): Any? {
        return YAMLStar.load(input)
    }

    /** Load a YAML string that loads to a mapping. */
    @Suppress("UNCHECKED_CAST")
    fun loadObject(input: String): Map<String, Any?> =
        YAMLStar.load(input) as Map<String, Any?>

    /** Load a YAML string that loads to a sequence. */
    @Suppress("UNCHECKED_CAST")
    fun loadArray(input: String): List<Any?> =
        YAMLStar.load(input) as List<Any?>

    /** Load a YAML string that loads to a string. */
    fun loadString(input: String): String =
        YAMLStar.load(input) as String

    /** Load a YAML string that loads to an integer. */
    fun loadInt(input: String): Int =
        (YAMLStar.load(input) as Number).toInt()

    /** Load a YAML string that loads to a boolean. */
    fun loadBoolean(input: String): Boolean =
        YAMLStar.load(input) as Boolean
}
