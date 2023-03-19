package org.ecorous.vhmlkt

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.ecorous.vhmlkt.internal.escape
import kotlin.test.Test
import kotlin.test.assertEquals

class EncoderTest {
    @Test
    fun encodeVhmlInteger() {
        printIfDebug(Vhml.encodeToString(ByteCode.serializer(), ByteCode(0b1010)))
        printIfDebug(Vhml.encodeToString(Color.serializer(), Color(0xC0101010)))
    }

    @Test
    fun encodeClass() {
        printIfDebug(Vhml.encodeToString(Project.serializer(), vhmlProject))
        printIfDebug("-----")
        printIfDebug(Vhml.encodeToString(Box.serializer(Project.serializer()), Box(vhmlProject)))
    }

    @Test
    fun encodeArrayOfTable() {
        printIfDebug(
            Vhml.encodeToString(
            serializer = NullablePairList.serializer(String.serializer(), Int.serializer()),
            value = nullablePairList
        ))
    }

    @Test
    fun encodeMap() {
        printIfDebug(Vhml.encodeToString(MapSerializer(String.serializer(), Project.serializer()), projects))
        printIfDebug("-----")
        printIfDebug(Vhml.encodeToString(NullableColorMap.serializer(), nullableColorMap))
        printIfDebug("-----")
        printIfDebug(Vhml.encodeToString(Score.serializer(), exampleScore))
        printIfDebug("-----")
        val emptyArrayOfTableInMap = mapOf("Something" to listOf(), "More" to listOf(vhmlProject))
        val arrayOfTableInMapSerializer = MapSerializer(String.serializer(), ListSerializer(Project.serializer()))
        printIfDebug(
            Vhml.encodeToString(
            serializer = arrayOfTableInMapSerializer,
            value = emptyArrayOfTableInMap
        ))
        printIfDebug("-----")
        printIfDebug(
            Vhml.encodeToString(
            serializer = Box.serializer(arrayOfTableInMapSerializer),
            value = Box(emptyArrayOfTableInMap)
        ))
        printIfDebug("-----")
        printIfDebug(
            Vhml.encodeToString(
            serializer = MapSerializer(Int.serializer(), arrayOfTableInMapSerializer),
            value = mapOf(1 to emptyArrayOfTableInMap)
        ))
    }

    @Test
    fun encodeEmptyClass() {
        printIfDebug(Vhml.encodeToString(EmptyClass.serializer(), EmptyClass()))
    }

    @Test
    fun encodeGeneric() {
        printIfDebug(Vhml.encodeToString(Box.serializer(Int.serializer()), Box(1)))
    }

    @Test
    fun encodeToVhmlLiteral() {
        val int = Vhml.encodeToVhmlElement(Int.serializer(), 2)
        val string = Vhml.encodeToVhmlElement(String.serializer(), "I\n&\nU")

        assertEquals(int.toVhmlLiteral().toInt(), 2)
        assertEquals(string.toVhmlLiteral().content, "I\n&\nU")
    }

    @Test
    fun encodeToVhmlTable() {
        val scoreAsTable = Vhml.encodeToVhmlElement(Score.serializer(), exampleScore)
        printIfDebug(Vhml.decodeFromVhmlElement(Score.serializer(), scoreAsTable))
        assertEquals(scoreAsTable.toVhmlTable()["examinee"]?.toVhmlLiteral()?.content, "Loney Chou")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun encodeInlineClass() {
        val boxedUInt = Box(1.toUInt())
        printIfDebug(Vhml.encodeToString(Box.serializer(UInt.serializer()), boxedUInt))
        printIfDebug("-----")
        printIfDebug(Vhml.encodeToString(Module.serializer(), module))
    }

    @Test
    fun escape() {
        assertEquals(anotherLyrics.trimIndent().escape(), "Oops my baby,\\nyou woke up in my bed.")
    }
}