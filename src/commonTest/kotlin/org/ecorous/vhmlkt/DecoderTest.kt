package org.ecorous.vhmlkt

import kotlinx.serialization.builtins.serializer
import org.ecorous.vhmlkt.internal.unescape
import kotlin.test.Test
import kotlin.test.assertEquals

class DecoderTest {
    @Test
    fun parseVhmlInteger() {
        val integers = Vhml.parseToVhmlTable(integers)
        printIfDebug(integers)
        assertEquals(integers["two"]?.toVhmlLiteral()?.toIntOrNull(), 4)
        assertEquals(integers["eight"]?.toVhmlLiteral()?.toIntOrNull(), 64)
        assertEquals(integers["ten"]?.toVhmlLiteral()?.toIntOrNull(), -100)
        assertEquals(integers["sixteen"]?.toVhmlLiteral()?.toIntOrNull(), 256)
    }

    @Test
    fun parseHugeConfig() {
        val table = Vhml.parseToVhmlTable(cargo)
        printIfDebug(table)
        assertEquals(table["package", "version"]?.toVhmlLiteral()?.content, "0.0.1")
    }

    @Test
    fun decodeClassAndList() {
        val project = Vhml.decodeFromString(Project.serializer(), project)
        printIfDebug(project)
        assertEquals(project.maintainability, Maintainability.HIGH)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun decodeInlineClass() {
        val boxedUInt = Vhml.decodeFromString(Box.serializer(UInt.serializer()), "content = 0x10")
        printIfDebug(boxedUInt)
        assertEquals(boxedUInt.content, 16L.toUInt())
        val externalModule = Vhml.decodeFromString(Module.serializer(), externalModule)
        printIfDebug(externalModule)
        assertEquals(externalModule.id, 4321234L.toULong())
    }

    @Test
    fun decodeGeneric() {
        val box = Vhml.decodeFromString(Box.serializer(Boolean.serializer()), boxContent)
        printIfDebug(box)
        assertEquals(box.content, null)
    }

    @Test
    fun decodeMap() {
        val score = Vhml.decodeFromString(Score.serializer(), score)
        printIfDebug(score)
        assertEquals(score.scores["Listening"]?.equals(91), true)
    }

    @Test
    fun unescape() {
        assertEquals(thirdLyrics.trimIndent().unescape(), "Oops we broke up,\nwe're better off as friends.")
    }
}