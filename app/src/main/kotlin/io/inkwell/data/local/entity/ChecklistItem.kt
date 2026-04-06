package io.inkwell.data.local.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A single checklist item with text and local checked state.
 *
 * ## Storage format
 *
 * Stored in [NoteEntity.listItemsJson] as a JSON array. Two formats are supported:
 *
 * ### Legacy (I1 — string array)
 * ```json
 * ["milk", "eggs", "bread"]
 * ```
 * All items parse as unchecked.
 *
 * ### Structured (I4 — object array)
 * ```json
 * [{"text":"milk","checked":true},{"text":"eggs","checked":false}]
 * ```
 *
 * The parser auto-detects format by inspecting the first array element.
 * Serialization always writes the structured format.
 *
 * ## Sync caveat
 *
 * Checked state is **local-only**. The server detail API does not return list items,
 * and [NoteUpdateRequest] cannot save them. The sync engine preserves the local
 * `listItemsJson` column during sync overwrites.
 */
@Serializable
data class ChecklistItem(
    val text: String,
    val checked: Boolean = false,
)

object ChecklistItems {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parse [listItemsJson] into a list of [ChecklistItem].
     * Handles both legacy string arrays and structured object arrays.
     * Returns an empty list for null, blank, or malformed input.
     */
    fun parse(listItemsJson: String?): List<ChecklistItem> {
        if (listItemsJson.isNullOrBlank() || listItemsJson == "[]") return emptyList()
        return try {
            val array = json.parseToJsonElement(listItemsJson).jsonArray
            if (array.isEmpty()) return emptyList()
            when (val first = array[0]) {
                is JsonPrimitive -> parseLegacy(array)
                is JsonObject -> parseStructured(array)
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Serialize a list of [ChecklistItem] to the structured JSON format.
     * Returns `null` if the list is empty (matches the nullable column convention).
     */
    fun serialize(items: List<ChecklistItem>): String? {
        if (items.isEmpty()) return null
        val array = JsonArray(items.map { item ->
            JsonObject(mapOf(
                "text" to JsonPrimitive(item.text),
                "checked" to JsonPrimitive(item.checked),
            ))
        })
        return array.toString()
    }

    /**
     * Toggle the checked state of the item at [index].
     * Returns a new list (items are immutable data classes).
     * Returns the original list unchanged if [index] is out of bounds.
     */
    fun toggle(items: List<ChecklistItem>, index: Int): List<ChecklistItem> {
        if (index !in items.indices) return items
        return items.mapIndexed { i, item ->
            if (i == index) item.copy(checked = !item.checked) else item
        }
    }

    private fun parseLegacy(array: JsonArray): List<ChecklistItem> {
        return array.mapNotNull { element ->
            element.jsonPrimitive.contentOrNull?.let { ChecklistItem(text = it) }
        }
    }

    private fun parseStructured(array: JsonArray): List<ChecklistItem> {
        return array.mapNotNull { element ->
            try {
                val obj = element.jsonObject
                val text = obj["text"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val checked = obj["checked"]?.jsonPrimitive?.booleanOrNull ?: false
                ChecklistItem(text = text, checked = checked)
            } catch (_: Exception) {
                null
            }
        }
    }
}
