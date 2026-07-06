// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

// Kotlin binding/API for the libyamlstar shared library.
//
// This is a thin idiomatic Kotlin layer over the Java binding (the
// org.yamlstar:yamlstar artifact), which wraps libyamlstar with JNA.
//
// The current user facing API is the YS object, whose load() function
// takes a YAML string as input and returns the value that YAMLStar loads.

package org.yamlstar.yamlstar

import org.json.JSONArray
import org.json.JSONObject

object YS {
    // This value is automatically updated by 'make bump'.
    const val YAMLSTAR_VERSION = "0.1.11"

    /** Load a YAML string and return the result. */
    fun load(input: String): Any? {
        val resp = YAMLStar.loadJSON(input)
        return if (resp.isNull("data")) null else resp.get("data")
    }

    /** Load a YAML string that loads to a mapping. */
    fun loadObject(input: String): JSONObject =
        YAMLStar.loadObject(input)

    /** Load a YAML string that loads to a sequence. */
    fun loadArray(input: String): JSONArray =
        YAMLStar.loadArray(input)

    /** Load a YAML string that loads to a string. */
    fun loadString(input: String): String =
        YAMLStar.loadJSON(input).getString("data")

    /** Load a YAML string that loads to an integer. */
    fun loadInt(input: String): Int =
        YAMLStar.loadInt(input)

    /** Load a YAML string that loads to a boolean. */
    fun loadBoolean(input: String): Boolean =
        YAMLStar.loadBoolean(input)
}
