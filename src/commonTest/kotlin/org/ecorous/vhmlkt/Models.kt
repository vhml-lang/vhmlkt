package org.ecorous.vhmlkt

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class User(
    @VhmlComment("Name of this user")
    val name: String,
    @VhmlInline
    val account: Account? = null
)

@Serializable
data class Account(
    val username: String,
    val password: String
)

val owner = User("Peanuuutz", Account("peanuuutz", "123456"))
val cooperator = User("Anonymous")

@Serializable
data class Project(
    @VhmlComment("Project name")
    val name: String,
    @VhmlComment("""
        Current maintainability
        Could be HIGH or LOW
    """)
    val maintainability: Maintainability,
    @org.ecorous.vhmlkt.VhmlMultilineString @org.ecorous.vhmlkt.VhmlLiteralString
    val description: String? = null,
    val owner: User,
    @VhmlComment("Thank you! :)")
    val contributors: Set<User> = setOf(owner)
)

val vhmlProject = Project(
    name = "vhmlkt",
    maintainability = Maintainability.HIGH,
    description = """
        This is my first project, so sorry for any inconvenience! \
        Anyway, constructive criticism is welcomed. :)
    """.trimIndent(),
    owner = owner,
    contributors = setOf(owner, cooperator)
)

val yamlProject = Project(
    name = "yamlkt",
    maintainability = Maintainability.LOW,
    owner = User("Him188")
)

val projects = mapOf("Vhml" to vhmlProject, "Yaml" to yamlProject)

@Serializable
enum class Maintainability { HIGH, LOW }

@Serializable
data class Score(
    val examinee: String,
    val scores: Map<String, Int>
)

val exampleScore = Score(
    examinee = "Loney Chou",
    scores = mapOf("Listening" to 80, "Reading" to 95)
)

@Serializable
class EmptyClass

@Serializable
data class Box<T>(val content: T? = null)

@Serializable
data class ByteCode(@VhmlInteger(VhmlInteger.Base.BIN) val code: Byte)

@Serializable
data class Color(@VhmlInteger(VhmlInteger.Base.HEX) val value: Long)

@Serializable
data class NullablePairList<F, S>(
    @VhmlBlockArray(2)
    val list: List<Pair<F, S>?>
)

val nullablePairList = NullablePairList(
    list = listOf(
        Pair("key", 1),
        null,
        Pair("key", 3),
        Pair("key", 4)
    )
)

@Serializable
data class NullableColorMap(
    val map: Map<String, Color?>
)

val nullableColorMap = NullableColorMap(mapOf("1" to null))

@Serializable
data class StringOrColor(val content: @Serializable(StringOrColorSerializer::class) Any)

@Serializable
data class Module(
    val id: ULong,
    val name: String
)

val module = Module(id = 1L.toULong(), name = "core")

object StringOrColorSerializer : KSerializer<Any> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("SOC", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: Any) {
        if (value is String) {
            String.serializer().serialize(encoder, value)
        } else if (value is Color) {
            Color.serializer().serialize(encoder, value)
        }
    }

    override fun deserialize(decoder: Decoder): Any = Any()
}