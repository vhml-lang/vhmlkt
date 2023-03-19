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

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import org.ecorous.vhmlkt.*

internal class VhmlElementEncoder(
    private val config: VhmlConfig,
    override val serializersModule: SerializersModule = config.serializersModule
) : Encoder, VhmlEncoder {
    lateinit var element: VhmlElement

    override fun encodeBoolean(value: Boolean) { element = VhmlLiteral(value) }
    override fun encodeByte(value: Byte) { element = VhmlLiteral(value) }
    override fun encodeShort(value: Short) { element = VhmlLiteral(value) }
    override fun encodeInt(value: Int) { element = VhmlLiteral(value) }
    override fun encodeLong(value: Long) { element = VhmlLiteral(value) }
    override fun encodeFloat(value: Float) { element = VhmlLiteral(value) }
    override fun encodeDouble(value: Double) { element = VhmlLiteral(value) }
    override fun encodeChar(value: Char) { element = VhmlLiteral(value) }
    override fun encodeString(value: String) { element = VhmlLiteral(value) }
    override fun encodeNull() { element = VhmlNull }
    override fun encodeVhmlElement(value: VhmlElement) { element = value }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = encodeString(enumDescriptor.getElementName(index))
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = beginStructure(descriptor, ::element::set)

    private inline fun beginStructure(
        descriptor: SerialDescriptor,
        elementConsumer: (VhmlElement) -> Unit
    ) : CompositeEncoder =  when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> {
            val builder = mutableMapOf<String, VhmlElement>()
            elementConsumer(VhmlTable(builder))
            ClassEncoder(builder)
        }
        StructureKind.LIST -> {
            val builder = mutableListOf<VhmlElement>()
            elementConsumer(VhmlArray(builder))
            ArrayEncoder(builder)
        }
        StructureKind.MAP -> {
            val builder = mutableMapOf<String, VhmlElement>()
            elementConsumer(VhmlTable(builder))
            MapEncoder(builder)
        }
        else -> throw UnsupportedSerialKindException(descriptor.kind)
    }

    internal abstract inner class AbstractEncoder : Encoder, CompositeEncoder, VhmlEncoder {
        lateinit var currentElement: VhmlElement

        final override val serializersModule: SerializersModule = this@VhmlElementEncoder.serializersModule

        final override fun encodeBoolean(value: Boolean) { currentElement = VhmlLiteral(value) }
        final override fun encodeByte(value: Byte) { currentElement = VhmlLiteral(value) }
        final override fun encodeShort(value: Short) { currentElement = VhmlLiteral(value) }
        final override fun encodeInt(value: Int) { currentElement = VhmlLiteral(value) }
        final override fun encodeLong(value: Long) { currentElement = VhmlLiteral(value) }
        final override fun encodeFloat(value: Float) { currentElement = VhmlLiteral(value) }
        final override fun encodeDouble(value: Double) { currentElement = VhmlLiteral(value) }
        final override fun encodeChar(value: Char) { currentElement = VhmlLiteral(value) }
        final override fun encodeString(value: String) { currentElement = VhmlLiteral(value) }
        final override fun encodeNull() { currentElement = VhmlNull }
        final override fun encodeVhmlElement(value: VhmlElement) { currentElement = value }

        final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = encodeString(enumDescriptor.getElementName(index))
        final override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this

        final override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = beginStructure(descriptor, ::currentElement::set)

        final override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) { encodeSerializableElement(descriptor, index, Boolean.serializer(), value) }
        final override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) { encodeSerializableElement(descriptor, index, Byte.serializer(), value) }
        final override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) { encodeSerializableElement(descriptor, index, Short.serializer(), value) }
        final override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) { encodeSerializableElement(descriptor, index, Int.serializer(), value) }
        final override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) { encodeSerializableElement(descriptor, index, Long.serializer(), value) }
        final override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) { encodeSerializableElement(descriptor, index, Float.serializer(), value) }
        final override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) { encodeSerializableElement(descriptor, index, Double.serializer(), value) }
        final override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) { encodeSerializableElement(descriptor, index, Char.serializer(), value) }
        final override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) { encodeSerializableElement(descriptor, index, String.serializer(), value) }

        final override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
            TODO("Not yet implemented")
        }

        final override fun <T : Any> encodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T?
        ) {
            if (value == null)
                currentElement = VhmlNull
            else
                encodeSerializableElement(descriptor, index, serializer, value)
        }

        final override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ArrayEncoder(
        private val builder: MutableList<VhmlElement>
    ) : AbstractEncoder() {
        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            serializer.serialize(this, value)
            builder.add(currentElement)
        }
    }

    internal inner class ClassEncoder(
        private val builder: MutableMap<String, VhmlElement>
    ) : AbstractEncoder() {
        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            val key = descriptor.getElementName(index)
            serializer.serialize(this, value)
            builder[key] = currentElement
        }
    }

    internal inner class MapEncoder(
        private val builder: MutableMap<String, VhmlElement>
    ) : AbstractEncoder() {
        private var isKey: Boolean = true

        private lateinit var key: String

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (isKey) {
                key = value.toVhmlKey()
            } else {
                serializer.serialize(this, value)
                builder[key] = currentElement
            }
            isKey = !isKey
        }
    }
}