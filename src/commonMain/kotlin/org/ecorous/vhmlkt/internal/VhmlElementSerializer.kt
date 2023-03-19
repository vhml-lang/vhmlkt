@file:OptIn(InternalSerializationApi::class)

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

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ecorous.vhmlkt.VhmlElement
import org.ecorous.vhmlkt.VhmlNull
import org.ecorous.vhmlkt.VhmlLiteral
import org.ecorous.vhmlkt.VhmlArray
import org.ecorous.vhmlkt.VhmlTable
import org.ecorous.vhmlkt.asVhmlDecoder
import org.ecorous.vhmlkt.asVhmlEncoder
import org.ecorous.vhmlkt.toVhmlNull
import org.ecorous.vhmlkt.toVhmlLiteral
import org.ecorous.vhmlkt.toVhmlArray
import org.ecorous.vhmlkt.toVhmlTable

internal object VhmlElementSerializer : KSerializer<VhmlElement> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("org.ecorous.vhmlkt.VhmlElement", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: VhmlElement) {
        encoder.asVhmlEncoder().encodeVhmlElement(value)
    }

    override fun deserialize(decoder: Decoder): VhmlElement = decoder.asVhmlDecoder().decodeVhmlElement()
}

internal object VhmlNullSerializer : KSerializer<VhmlNull> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("org.ecorous.vhmlkt.VhmlNull", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: VhmlNull) {
        encoder.asVhmlEncoder().encodeVhmlElement(VhmlNull)
    }

    override fun deserialize(decoder: Decoder): VhmlNull = decoder.asVhmlDecoder().decodeVhmlElement().toVhmlNull()
}

internal object VhmlLiteralSerializer : KSerializer<VhmlLiteral> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("org.ecorous.vhmlkt.VhmlLiteral", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: VhmlLiteral) {
        encoder.asVhmlEncoder().encodeVhmlElement(value)
    }

    override fun deserialize(decoder: Decoder): VhmlLiteral = decoder.asVhmlDecoder().decodeVhmlElement().toVhmlLiteral()
}

internal object VhmlArraySerializer : KSerializer<VhmlArray> {
    private val delegate: KSerializer<List<VhmlElement>> = ListSerializer(VhmlElementSerializer)

    override val descriptor: SerialDescriptor = object : SerialDescriptor by delegate.descriptor {
        override val serialName: String = "org.ecorous.vhmlkt.VhmlArray"
    }

    override fun serialize(encoder: Encoder, value: VhmlArray) {
        delegate.serialize(encoder.asVhmlEncoder(), value)
    }

    override fun deserialize(decoder: Decoder): VhmlArray = decoder.asVhmlDecoder().decodeVhmlElement().toVhmlArray()
}

internal object VhmlTableSerializer : KSerializer<VhmlTable> {
    private val delegate: KSerializer<Map<String, VhmlElement>> = MapSerializer(String.serializer(), VhmlElementSerializer)

    override val descriptor: SerialDescriptor = object : SerialDescriptor by delegate.descriptor {
        override val serialName: String = "org.ecorous.vhmlkt.VhmlTable"
    }

    override fun serialize(encoder: Encoder, value: VhmlTable) {
        delegate.serialize(encoder.asVhmlEncoder(), value)
    }

    override fun deserialize(decoder: Decoder): VhmlTable = decoder.asVhmlDecoder().decodeVhmlElement().toVhmlTable()
}