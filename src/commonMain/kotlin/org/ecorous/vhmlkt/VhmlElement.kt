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

import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.ecorous.vhmlkt.internal.*
import org.ecorous.vhmlkt.internal.VhmlElementSerializer
import org.ecorous.vhmlkt.internal.VhmlNullSerializer
import org.ecorous.vhmlkt.internal.parser.ArrayNode
import org.ecorous.vhmlkt.internal.parser.KeyNode
import org.ecorous.vhmlkt.internal.parser.ValueNode

// VhmlElement

/**
 * Represents anything in TOML, including and only including [VhmlNull], [VhmlLiteral], [VhmlArray], [VhmlTable].
 *
 * **Warning: Only use [Vhml] to serialize/deserialize any sub-class.**
 */
@Serializable(with = VhmlElementSerializer::class)
public sealed class VhmlElement {
    /**
     * The content of this VhmlElement. Each sub-class has its own implementation.
     */
    public abstract val content: Any?

    /**
     * Gives a string representation of this VhmlElement. Each sub-class has its own implementation.
     *
     * ```kotlin
     * val table = VhmlTable(mapOf("isEnabled" to true, "port" to 8080))
     * println(table) // { isEnabled = true, port = 8080 }
     * ```
     *
     * @return a JSON-like string containing [content].
     */
    public abstract override fun toString(): String
}

// VhmlNull

/**
 * Represents null.
 *
 * Note: Currently encoded value can NOT be modified.
 */
@Serializable(with = VhmlNullSerializer::class)
public object VhmlNull : VhmlElement() {
    override val content: Nothing? = null

    override fun toString(): String = "null"
}

// To VhmlNull

/**
 * Convert [this] to VhmlNull.
 *
 * @throws IllegalStateException when [this] is not VhmlNull
 */
public fun VhmlElement.toVhmlNull(): VhmlNull = this as? VhmlNull ?: failConversion("VhmlNull")

// VhmlLiteral

/**
 * Represents literal value, which can be booleans, numbers, chars, strings.
 */
@Serializable(with = VhmlLiteralSerializer::class)
public class VhmlLiteral internal constructor(
    /**
     * The converted value. (see creator functions with the same name)
     */
    override val content: String,
    /**
     * Indicates whether this VhmlLiteral is actually a [Char] or [String].
     */
    private val isString: Boolean
) : VhmlElement() {
    override fun toString(): String = if (isString) content.escape().doubleQuoted else content

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as VhmlLiteral
        if (isString != other.isString) return false
        if (content != other.content) return false
        return true
    }

    override fun hashCode(): Int {
        var result = content.hashCode()
        result = 31 * result + isString.hashCode()
        return result
    }
}

// To VhmlLiteral

/**
 * Convert [this] to VhmlLiteral.
 *
 * @throws IllegalStateException when [this] is not VhmlLiteral.
 */
public fun VhmlElement.toVhmlLiteral(): VhmlLiteral = this as? VhmlLiteral ?: failConversion("VhmlLiteral")

/**
 * Creates [VhmlLiteral] from the given boolean [value].
 */
public fun VhmlLiteral(value: Boolean): VhmlLiteral = VhmlLiteral(value.toString(), false)

/**
 * Creates [VhmlLiteral] from the given numeric [value].
 *
 * @see toStringModified
 */
public fun VhmlLiteral(value: Number): VhmlLiteral = VhmlLiteral(value.toStringModified(), false)

/**
 * Creates [VhmlLiteral] from the given char [value].
 */
public fun VhmlLiteral(value: Char): VhmlLiteral = VhmlLiteral(value.toString(), true)

/**
 * Creates [VhmlLiteral] from the given string [value].
 */
public fun VhmlLiteral(value: String): VhmlLiteral = VhmlLiteral(value, true)

/**
 * Creates [VhmlLiteral] from the given enum [value]. Delegates to creator function which consumes string.
 *
 * @param E the enum class which [value] belongs to.
 * @param serializersModule in most case could be ignored, but for contextual it should be present.
 */
public inline fun <reified E : Enum<E>> VhmlLiteral(
    value: E,
    serializersModule: SerializersModule = EmptySerializersModule
): VhmlLiteral = VhmlLiteral(serializersModule.serializer<E>().descriptor.getElementName(value.ordinal))

// From VhmlLiteral

/**
 * Returns content as boolean.
 *
 * @throws IllegalStateException if content cannot be converted into boolean.
 */
public fun VhmlLiteral.toBoolean(): Boolean = toBooleanOrNull() ?: error("Cannot convert $this to Boolean")

/**
 * Returns content as boolean only if content is "true" or "false", otherwise null.
 */
public fun VhmlLiteral.toBooleanOrNull(): Boolean? = when (content) {
    "true" -> true
    "false" -> false
    else -> null
}

/**
 * Returns content as byte.
 *
 * @throws NumberFormatException if content cannot be converted into byte.
 */
public fun VhmlLiteral.toByte(): Byte = content.toByte()

/**
 * Returns content as byte only if content can be byte, otherwise null.
 */
public fun VhmlLiteral.toByteOrNull(): Byte? = content.toByteOrNull()

/**
 * Returns content as short.
 *
 * @throws NumberFormatException if content cannot be converted into short.
 */
public fun VhmlLiteral.toShort(): Short = content.toShort()

/**
 * Returns content as short only if content can be short, otherwise null.
 */
public fun VhmlLiteral.toShortOrNull(): Short? = content.toShortOrNull()

/**
 * Returns content as int.
 *
 * @throws NumberFormatException if content cannot be converted into int.
 */
public fun VhmlLiteral.toInt(): Int = content.toInt()

/**
 * Returns content as int only if content can be int, otherwise null.
 */
public fun VhmlLiteral.toIntOrNull(): Int? = content.toIntOrNull()

/**
 * Returns content as long.
 *
 * @throws NumberFormatException if content cannot be converted into long.
 */
public fun VhmlLiteral.toLong(): Long = content.toLong()

/**
 * Returns content as long only if content can be long, otherwise null.
 */
public fun VhmlLiteral.toLongOrNull(): Long? = content.toLongOrNull()

/**
 * Returns content as float.
 *
 * @throws NumberFormatException if content cannot be converted into float.
 */
public fun VhmlLiteral.toFloat(): Float = toFloatOrNull() ?: throw NumberFormatException("Cannot convert $this to Float")

/**
 * Returns content as float only if content can be an exact float or inf/-inf/nan, otherwise null.
 */
public fun VhmlLiteral.toFloatOrNull(): Float? = when (content) {
    "inf" -> Float.POSITIVE_INFINITY
    "-inf" -> Float.NEGATIVE_INFINITY
    "nan" -> Float.NaN
    else -> content.toFloatOrNull()
}

/**
 * Returns content as double.
 *
 * @throws NumberFormatException if content cannot be converted into double.
 */
public fun VhmlLiteral.toDouble(): Double = toDoubleOrNull() ?: throw NumberFormatException("Cannot convert $this to Double")

/**
 * Returns content as double only if content can be an exact double or inf/-inf/nan, otherwise null.
 */
public fun VhmlLiteral.toDoubleOrNull(): Double? = when (content) {
    "inf" -> Double.POSITIVE_INFINITY
    "-inf" -> Double.NEGATIVE_INFINITY
    "nan" -> Double.NaN
    else -> content.toDoubleOrNull()
}

/**
 * Returns content as char.
 *
 * @throws NoSuchElementException if content is empty.
 * @throws IllegalArgumentException if content cannot be converted into char.
 */
public fun VhmlLiteral.toChar(): Char = content.single()

/**
 * Returns content as char only if the length of content is exactly 1, otherwise null.
 */
public fun VhmlLiteral.toCharOrNull(): Char? = content.singleOrNull()

/**
 * Returns content as enum with given enum class context.
 *
 * @param E the enum class which [this] converts to.
 * @param serializersModule in most case could be ignored, but for contextual it should be present.
 *
 * @throws IllegalStateException if content cannot be converted into [E].
 */
public inline fun <reified E : Enum<E>> VhmlLiteral.toEnum(
    serializersModule: SerializersModule = EmptySerializersModule
): E = toEnumOrNull<E>(serializersModule) ?: error("Cannot convert $this to ${E::class.simpleName}")

/**
 * Returns content as enum with given enum class context only if content suits in, otherwise null.
 *
 * @param E the enum class which [this] converts to.
 * @param serializersModule in most case could be ignored, but for contextual it should be present.
 */
public inline fun <reified E : Enum<E>> VhmlLiteral.toEnumOrNull(
    serializersModule: SerializersModule = EmptySerializersModule
): E? {
    val index = serializersModule.serializer<E>().descriptor.elementNames.indexOf(content)
    return if (index != -1) enumValues<E>()[index] else null
}

// VhmlArray

/**
 * Represents array in TOML, which values are [VhmlElement].
 *
 * As it delegates to list [content], everything in [List] could be used.
 */
@Serializable(with = VhmlArraySerializer::class)
public class VhmlArray internal constructor(
    override val content: List<VhmlElement>
) : VhmlElement(), List<VhmlElement> by content {
    override fun toString(): String = content.joinToString(
        prefix = "[ ",
        postfix = " ]"
    )

    override fun equals(other: Any?): Boolean = content == other

    override fun hashCode(): Int = content.hashCode()
}

// To VhmlArray

/**
 * Convert [this] to VhmlArray.
 *
 * @throws IllegalStateException when [this] is not VhmlArray.
 */
public fun VhmlElement.toVhmlArray(): VhmlArray = this as? VhmlArray ?: failConversion("VhmlArray")

/**
 * Creates [VhmlArray] from the given iterable [value].
 */
public fun VhmlArray(value: Iterable<*>): VhmlArray = VhmlArray(value.map(Any?::toVhmlElement))

// VhmlTable

/**
 * Represents table in TOML, which keys are strings and values are [VhmlElement].
 *
 * As it delegates to map [content], everything in [Map] could be used.
 */
@Serializable(with = VhmlTableSerializer::class)
public class VhmlTable internal constructor(
    override val content: Map<String, VhmlElement>
) : VhmlElement(), Map<String, VhmlElement> by content {
    /**
     * More convenient than [Map.get] if this VhmlTable is originally a map with **primitive** keys
     *
     * @throws NonPrimitiveKeyException if provide non-primitive key
     */
    public operator fun get(key: Any?): VhmlElement? = get(key.toVhmlKey())

    override fun toString(): String = content.entries.joinToString(
        prefix = "{ ",
        postfix = " }"
    ) { (k, v) -> "${k.escape().doubleQuotedIfNeeded()} = $v" }

    override fun equals(other: Any?): Boolean = content == other

    override fun hashCode(): Int = content.hashCode()
}

// To VhmlTable

/**
 * Convert [this] to VhmlTable.
 *
 * @throws IllegalStateException when [this] is not VhmlTable.
 */
public fun VhmlElement.toVhmlTable(): VhmlTable = this as? VhmlTable ?: failConversion("VhmlTable")

/**
 * Creates [VhmlTable] from the given map [value].
 */
public fun VhmlTable(value: Map<*, *>): VhmlTable = VhmlTable(buildMap(value.size) {
    for ((k, v) in value)
        put(k.toVhmlKey(), v.toVhmlElement())
})

// Extensions for VhmlTable

/**
 * Get value along with path constructed by [keys].
 *
 * @param keys one for a single path segment.
 *
 * @throws NonPrimitiveKeyException if provide non-primitive key
 */
public operator fun VhmlTable.get(vararg keys: Any?): VhmlElement? = getByPathRecursively(keys, 0)

// Internal

internal fun VhmlTable(value: KeyNode): VhmlTable = VhmlTable(buildMap(value.children.size) {
    for (node in value.children)
        put(node.key, node.toVhmlElement())
})

private tailrec fun VhmlTable.getByPathRecursively(
    keys: Array<out Any?>,
    index: Int
): VhmlElement? {
    val value = get(keys[index])
    return if (index == keys.lastIndex) {
        value
    } else when (value) {
        is VhmlTable -> value.getByPathRecursively(keys, index + 1)
        VhmlNull, is VhmlLiteral, is VhmlArray, null -> null
    }
}

internal fun Any?.toVhmlKey(): String = when (this) {
    is Boolean, is Number, is Char -> toString()
    is String -> this
    else -> throw NonPrimitiveKeyException()
}

private fun Any?.toVhmlElement(): VhmlElement = when (this) {
    null -> VhmlNull
    is VhmlElement -> this
    is Boolean -> VhmlLiteral(this)
    is Byte -> VhmlLiteral(this)
    is Short -> VhmlLiteral(this)
    is Int -> VhmlLiteral(this)
    is Long -> VhmlLiteral(this)
    is Float -> VhmlLiteral(this)
    is Double -> VhmlLiteral(this)
    is Char -> VhmlLiteral(this)
    is String -> VhmlLiteral(this)
    is BooleanArray -> VhmlArray(this.asIterable())
    is ByteArray -> VhmlArray(this.asIterable())
    is ShortArray -> VhmlArray(this.asIterable())
    is IntArray -> VhmlArray(this.asIterable())
    is LongArray -> VhmlArray(this.asIterable())
    is FloatArray -> VhmlArray(this.asIterable())
    is DoubleArray -> VhmlArray(this.asIterable())
    is CharArray -> VhmlArray(this.asIterable())
    is Array<*> -> VhmlArray(this.asIterable())
    is Iterable<*> -> VhmlArray(this)
    is Map<*, *> -> VhmlTable(this)
    is KeyNode -> VhmlTable(this)
    is ArrayNode -> VhmlArray(array)
    is ValueNode -> value
    else -> error("Unsupported class: ${this::class.simpleName}")
}

private fun VhmlElement.failConversion(target: String): Nothing = error("Cannot convert ${this::class.simpleName} to $target")