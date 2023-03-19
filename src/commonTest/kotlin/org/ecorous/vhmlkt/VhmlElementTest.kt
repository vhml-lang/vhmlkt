package org.ecorous.vhmlkt

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class VhmlElementTest {
    @Test
    fun vhmlNull() {
        val vhmlString = Vhml.encodeToString(VhmlNull)
        printIfDebug(vhmlString)
    }

    @Test
    fun vhmlLiteral() {
        val vhmlString = Vhml.encodeToString(VhmlLiteral(lyrics))
        printIfDebug(vhmlString) // Since '\n' will be converted to "\\n"
        assertEquals(VhmlLiteral(Maintainability.HIGH).content, "HIGH")
        assertEquals(VhmlLiteral("LOW").toEnum(), Maintainability.LOW)
    }

    @Test
    fun vhmlArray() {
        val array = VhmlArray(listOf('1', null, '\b', true))
        assertEquals(array[0].toVhmlLiteral().toInt(), 1)
        val vhmlString = Vhml.encodeToString(array) // Maybe add an 'alwaysInlineArrayOfPrimitive' config...
        printIfDebug(vhmlString)
    }

    @Test
    fun vhmlTable() {
        val table = VhmlTable(mapOf("1" to null, 1 to 'b', '\b' to listOf(true))) // "1" is equal to 1 when converted to key
        assertEquals(table[1]!!.toVhmlLiteral().toChar(), 'b')
        val vhmlString = Vhml.encodeToString(table)
        printIfDebug(vhmlString)
    }

    @Test
    fun elementToString() {
        printIfDebug(VhmlArray(listOf('1', null, '\b', true)))
        printIfDebug(VhmlTable(mapOf('1' to null, '\b' to true))) // Note the difference where strings behave
    }

    @Test
    fun nestedElement() {
        val boxedArray = Box(VhmlArray(listOf(1, 2)))
        printIfDebug(Vhml.encodeToString(Box.serializer(VhmlArray.serializer()), boxedArray))
        printIfDebug("-----")
        val mapOfTables = mapOf(1 to VhmlTable(mapOf(1 to 1)))
        val mapOfTablesSerializer = MapSerializer(Int.serializer(), VhmlTable.serializer())
        printIfDebug(
            Vhml.encodeToString(
            serializer = mapOfTablesSerializer,
            value = mapOfTables
        ))
        printIfDebug("-----")
        printIfDebug(
            Vhml.encodeToString(
            serializer = MapSerializer(Int.serializer(), mapOfTablesSerializer),
            value = mapOf(1 to mapOfTables)
        ))
        printIfDebug("-----")
        printIfDebug(
            Vhml.encodeToString(
            serializer = Box.serializer(MapSerializer(Int.serializer(), Box.serializer(mapOfTablesSerializer))),
            value = Box(mapOf(1 to Box(mapOfTables)))
        ))
    }
}

