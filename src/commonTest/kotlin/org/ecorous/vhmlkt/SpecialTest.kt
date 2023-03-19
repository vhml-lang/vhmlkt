package org.ecorous.vhmlkt

import kotlin.test.Test
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class SpecialTest {
    @Test
    fun contextual() {
        val stringOrColor = StringOrColor(Color(0xFF0000))
        printIfDebug(Vhml.encodeToString(StringOrColor.serializer(), stringOrColor))
        printIfDebug("-----")
        val mapOfStringOrColors: Map<String, Any> = mapOf("Red" to Color(0xFF0000))
        val mapOfStringOrColorsSerializer = MapSerializer(String.serializer(), StringOrColorSerializer)
        printIfDebug(Vhml.encodeToString(mapOfStringOrColorsSerializer, mapOfStringOrColors))
        printIfDebug("-----")
        printIfDebug(
            Vhml.encodeToString(
            serializer = MapSerializer(Int.serializer(), mapOfStringOrColorsSerializer),
            value = mapOf(1 to mapOfStringOrColors)
        ))
    }
}