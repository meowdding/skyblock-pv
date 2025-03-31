package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.intellij.lang.annotations.Language
import java.util.*

private const val DELIMITER = "."

fun JsonElement.getPath(@Language("JSONPath") path: String): JsonElement? {
    if (this !is JsonObject) {
        throw UnsupportedOperationException("Called json path on non json object!")
    }

    val split = LinkedList(path.split(DELIMITER))

    var current: JsonElement? = this
    while (split.isNotEmpty()) {
        if (current == null) {
            return null
        }
        current = current.asJsonObject[split.removeFirst()]
    }

    return current
}
