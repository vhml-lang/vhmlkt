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

package org.ecorous.vhmlkt.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import org.ecorous.vhmlkt.VhmlElement
import org.ecorous.vhmlkt.VhmlNull
import org.ecorous.vhmlkt.VhmlLiteral
import org.ecorous.vhmlkt.VhmlArray
import org.ecorous.vhmlkt.VhmlTable
import org.ecorous.vhmlkt.toVhmlLiteral
import org.ecorous.vhmlkt.toVhmlArray
import org.ecorous.vhmlkt.toVhmlTable
import org.ecorous.vhmlkt.toBoolean
import org.ecorous.vhmlkt.toByte
import org.ecorous.vhmlkt.toShort
import org.ecorous.vhmlkt.toInt
import org.ecorous.vhmlkt.toLong
import org.ecorous.vhmlkt.toFloat
import org.ecorous.vhmlkt.toDouble
import org.ecorous.vhmlkt.toChar
import org.ecorous.vhmlkt.toVhmlNull
import org.ecorous.vhmlkt.VhmlConfig
import org.ecorous.vhmlkt.VhmlDecoder

internal class VhmlElementDecoder(
    private val config: VhmlConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
    private val element: VhmlElement
) : Decoder, VhmlDecoder {
    override fun decodeBoolean(): Boolean = element.toVhmlLiteral().toBoolean()
    override fun decodeByte(): Byte = element.toVhmlLiteral().toByte()
    override fun decodeShort(): Short = element.toVhmlLiteral().toShort()
    override fun decodeInt(): Int = element.toVhmlLiteral().toInt()
    override fun decodeLong(): Long = element.toVhmlLiteral().toLong()
    override fun decodeFloat(): Float = element.toVhmlLiteral().toFloat()
    override fun decodeDouble(): Double = element.toVhmlLiteral().toDouble()
    override fun decodeChar(): Char = element.toVhmlLiteral().toChar()
    override fun decodeString(): String = element.toVhmlLiteral().content
    override fun decodeNull(): Nothing? = element.toVhmlNull().content
    override fun decodeNotNullMark(): Boolean = element != VhmlNull
    override fun decodeVhmlElement(): VhmlElement = element

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.getElementIndex(element.toVhmlLiteral().content)
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS -> ClassDecoder(element.toVhmlTable())
        StructureKind.OBJECT -> ClassDecoder(element.toVhmlTable())
        StructureKind.LIST -> ArrayDecoder(element.toVhmlArray())
        StructureKind.MAP -> MapDecoder(element.toVhmlTable())
        else -> throw UnsupportedSerialKindException(descriptor.kind)
    }

    internal abstract inner class AbstractDecoder : Decoder, CompositeDecoder, VhmlDecoder {
        protected var elementIndex: Int = 0

        protected abstract val currentElement: VhmlElement

        final override val serializersModule: SerializersModule = this@VhmlElementDecoder.serializersModule

        final override fun decodeBoolean(): Boolean = currentElement.toVhmlLiteral().toBoolean()
        final override fun decodeByte(): Byte = currentElement.toVhmlLiteral().toByte()
        final override fun decodeShort(): Short = currentElement.toVhmlLiteral().toShort()
        final override fun decodeInt(): Int = currentElement.toVhmlLiteral().toInt()
        final override fun decodeLong(): Long = currentElement.toVhmlLiteral().toLong()
        final override fun decodeFloat(): Float = currentElement.toVhmlLiteral().toFloat()
        final override fun decodeDouble(): Double = currentElement.toVhmlLiteral().toDouble()
        final override fun decodeChar(): Char = currentElement.toVhmlLiteral().toChar()
        final override fun decodeString(): String = currentElement.toVhmlLiteral().content
        final override fun decodeNull(): Nothing? = currentElement.toVhmlNull().content
        final override fun decodeNotNullMark(): Boolean = currentElement != VhmlNull
        final override fun decodeVhmlElement(): VhmlElement = currentElement

        final override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.getElementIndex(currentElement.toVhmlLiteral().content)
        final override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
            StructureKind.CLASS -> ClassDecoder(currentElement.toVhmlTable())
            StructureKind.OBJECT -> ClassDecoder(currentElement.toVhmlTable())
            StructureKind.LIST -> ArrayDecoder(currentElement.toVhmlArray())
            StructureKind.MAP -> MapDecoder(currentElement.toVhmlTable())
            else -> throw UnsupportedSerialKindException(descriptor.kind)
        }

        final override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean = decodeSerializableElement(descriptor, index, Boolean.serializer())
        final override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = decodeSerializableElement(descriptor, index, Byte.serializer())
        final override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = decodeSerializableElement(descriptor, index, Short.serializer())
        final override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = decodeSerializableElement(descriptor, index, Int.serializer())
        final override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = decodeSerializableElement(descriptor, index, Long.serializer())
        final override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = decodeSerializableElement(descriptor, index, Float.serializer())
        final override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = decodeSerializableElement(descriptor, index, Double.serializer())
        final override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = decodeSerializableElement(descriptor, index, Char.serializer())
        final override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = decodeSerializableElement(descriptor, index, String.serializer())

        final override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
            TODO("Not yet implemented")
        }

        final override fun <T : Any> decodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T?>,
            previousValue: T?
        ): T? = if (currentElement == VhmlNull) VhmlNull.content else deserializer.deserialize(this)

        final override fun <T> decodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T>,
            previousValue: T?
        ): T = deserializer.deserialize(this)

        final override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ArrayDecoder(private val array: VhmlArray) : AbstractDecoder() {
        override val currentElement: VhmlElement get() = array[elementIndex++]

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (elementIndex == array.size) CompositeDecoder.DECODE_DONE else elementIndex++
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = array.size

        override fun decodeSequentially(): Boolean = true
    }

    internal inner class ClassDecoder(table: VhmlTable) : AbstractDecoder() {
        override lateinit var currentElement: VhmlElement

        private val iterator: Iterator<Map.Entry<String, VhmlElement>> = table.iterator()

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (elementIndex < descriptor.elementsCount) {
                if (iterator.hasNext()) {
                    val entry = iterator.next()
                    currentElement = entry.value
                    val index = descriptor.getElementIndex(entry.key)
                    if (index == CompositeDecoder.UNKNOWN_NAME && !config.ignoreUnknownKeys)
                        throw UnknownKeyException(entry.key)
                    elementIndex++
                    index
                } else {
                    CompositeDecoder.DECODE_DONE
                }
            } else if (iterator.hasNext() && !config.ignoreUnknownKeys) {
                throw UnknownKeyException(iterator.next().key)
            } else {
                CompositeDecoder.DECODE_DONE
            }
        }
    }

    internal inner class MapDecoder(private val table: VhmlTable) : AbstractDecoder() {
        private val iterator: Iterator<VhmlElement> = iterator {
            for ((k, v) in table) {
                yield(VhmlLiteral(k))
                yield(v)
            }
        }

        override val currentElement: VhmlElement get() = iterator.next()

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (iterator.hasNext()) elementIndex++ else CompositeDecoder.DECODE_DONE
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = table.size

        override fun decodeSequentially(): Boolean = true
    }
}