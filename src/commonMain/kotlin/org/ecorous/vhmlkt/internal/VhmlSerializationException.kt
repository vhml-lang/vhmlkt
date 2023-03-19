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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialKind
import org.ecorous.vhmlkt.internal.parser.Path

internal sealed class VhmlEncodingException : SerializationException {
    constructor()
    constructor(message: String) : super(message)
}

internal class NonPrimitiveKeyException : VhmlEncodingException()

internal class UnsupportedSerialKindException(kind: SerialKind) : VhmlEncodingException("$kind")

internal class NullInArrayOfTableException : VhmlEncodingException(
    message = "Null is not allowed in array of table, " +
            "please mark the corresponding property as @VhmlBlockArray or @VhmlInline"
)

internal class EmptyArrayOfTableInMapException : VhmlEncodingException(
    message = "Empty array of table can only be the first in map"
)

internal sealed class VhmlDecodingException(message: String) : SerializationException(message)

internal class UnexpectedTokenException(token: Char, line: Int) : VhmlDecodingException(
    message = "'${if (token != '\'') token.escape() else "\\'"}' (L$line)"
)

internal class IncompleteException(line: Int) : VhmlDecodingException("(L$line)")

internal class ConflictEntryException(path: Path) : VhmlDecodingException(
    message = path.joinToString(".") { it.escape().doubleQuotedIfNeeded() }
)

internal class UnknownKeyException(key: String) : VhmlDecodingException(key)