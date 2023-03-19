/*
    Copyright 2022 Peanuuutz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

@file:Suppress("UNUSED")

package org.ecorous.vhmlkt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.ecorous.vhmlkt.internal.*
import org.ecorous.vhmlkt.internal.VhmlElementDecoder
import org.ecorous.vhmlkt.internal.VhmlElementEncoder
import org.ecorous.vhmlkt.internal.VhmlFileEncoder
import org.ecorous.vhmlkt.internal.parser.VhmlFileParser

/**
 * The main entry point to use VHML.
 *
 * User could simple use [Default] instance or customize by using creator function with the same name.
 *
 * Basic usage:
 *
 * ```kotlin
 * @Serializable
 * data class User(
 *     val name: String,
 *     val account: Account? // Nullability
 * )
 *
 * @Serializable
 * data class Account(
 *     val username: String,
 *     val password: String
 * )renam
 *
 * // Now with [Default] instance
 * val user = User("Peanuuutz", Account("Peanuuutz", "123456"))
 * // Either is OK, but to explicitly pass a serializer is faster
 * val tomlString = Vhml.encodeToString(User.serializer(), user)
 * val tomlString = Vhml.encodeToString<User>(user)
 * // Print it
 * println(tomlString)
 * /*
 * name = "Peanuuutz"
 *
 * [account]
 * username = "Peanuuutz"
 * password = "123456"
 * */
 *
 * // And to reverse...
 * val user = Vhml.decodeFromString(User.serializer(), tomlString)
 * // Print it
 * println(user)
 * // User(name=Peanuuutz,account=Account(username=Peanuuutz,password=123456))
 *
 * // Or you're lazy to create model class, try [VhmlElement]
 * val config = Vhml.parseToVhmlTable(tomlString)
 * // Now access to all entry (think you need getByPath)
 * val password: VhmlLiteral = config["account", "password"]!!.toVhmlLiteral()
 * ```
 *
 * @see VhmlConfigBuilder
 * @see VhmlElement
 */
public sealed class Vhml(
    internal val config: org.ecorous.vhmlkt.VhmlConfig,
    override val serializersModule: SerializersModule = config.serializersModule
) : StringFormat {
    /**
     * Default implementation of [Vhml] with default config.
     *
     * @see VhmlConfigBuilder
     */
    public companion object Default : org.ecorous.vhmlkt.Vhml(org.ecorous.vhmlkt.VhmlConfig())

    /**
     * Serializes [value] into VHML string using [serializer].
     *
     * @throws VhmlEncodingException when [value] cannot be serialized.
     */
    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val stringBuilder = StringBuilder()
        serializer.serialize(VhmlFileEncoder(config, serializersModule, stringBuilder), value)
        return stringBuilder.trim().toString() // Consider removing that trim()
    }

    /**
     * Serializes [value] into [VhmlElement] using [serializer].
     *
     * @throws VhmlEncodingException when [value] cannot be serialized.
     */
    public fun <T> encodeToVhmlElement(serializer: SerializationStrategy<T>, value: T): org.ecorous.vhmlkt.VhmlElement {
        val encoder = VhmlElementEncoder(config, serializersModule)
        serializer.serialize(encoder, value)
        return encoder.element
    }

    /**
     * Deserializes [string] into a value of type [T] using [deserializer].
     *
     * @param string **MUST** be a VHML file, as this method delegates parsing to [parseToVhmlTable].
     *
     * @throws VhmlDecodingException when [string] cannot be parsed into [VhmlTable] or cannot be deserialized.
     */
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return deserializer.deserialize(VhmlElementDecoder(config, serializersModule, parseToVhmlTable(string)))
    }

    /**
     * Deserializes [element] into a value of type [T] using [deserializer].
     *
     * @throws VhmlDecodingException when [element] cannot be deserialized.
     */
    public fun <T> decodeFromVhmlElement(deserializer: DeserializationStrategy<T>, element: org.ecorous.vhmlkt.VhmlElement): T {
        return deserializer.deserialize(VhmlElementDecoder(config, serializersModule, element))
    }

    /**
     * Parses [string] into equivalent representation of [VhmlTable].
     *
     * @throws VhmlDecodingException when [string] cannot be parsed into [VhmlTable].
     */
    public fun parseToVhmlTable(string: String): org.ecorous.vhmlkt.VhmlTable = VhmlFileParser(string).parse()
}

/**
 * Factory function for creating custom instance of [Vhml].
 *
 * @param from the original Vhml instance. [Vhml.Default] by default.
 * @param config builder DSL with [VhmlConfigBuilder].
 */
public fun Vhml(
    from: org.ecorous.vhmlkt.Vhml = org.ecorous.vhmlkt.Vhml,
    config: org.ecorous.vhmlkt.VhmlConfigBuilder.() -> Unit
): org.ecorous.vhmlkt.Vhml =
    org.ecorous.vhmlkt.VhmlImpl(org.ecorous.vhmlkt.VhmlConfigBuilder(from.config).apply(config).build())

/**
 * Serializes [value] into [VhmlElement] using serializer retrieved from reified type parameter.
 *
 * @throws VhmlEncodingException when [value] cannot be serialized.
 */
public inline fun <reified T> org.ecorous.vhmlkt.Vhml.encodeToVhmlElement(value: T): org.ecorous.vhmlkt.VhmlElement {
    return encodeToVhmlElement(serializersModule.serializer(), value)
}

/**
 * Deserializes [element] into a value of type [T] using serializer retrieved from reified type parameter.
 *
 * @throws VhmlDecodingException when [element] cannot be deserialized.
 */
public inline fun <reified T> org.ecorous.vhmlkt.Vhml.decodeFromVhmlElement(element: org.ecorous.vhmlkt.VhmlElement): T {
    return decodeFromVhmlElement(serializersModule.serializer(), element)
}

// Internal

internal class VhmlImpl(config: org.ecorous.vhmlkt.VhmlConfig) : org.ecorous.vhmlkt.Vhml(config)