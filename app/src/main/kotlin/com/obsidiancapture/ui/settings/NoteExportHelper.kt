package com.obsidiancapture.ui.settings

import com.obsidiancapture.data.local.entity.NoteEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Serializes notes to JSON or CSV for export.
 */
object NoteExportHelper {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    @Serializable
    data class ExportNote(
        val uid: String,
        val title: String,
        val body: String,
        val kind: String,
        val status: String,
        val tags: List<String>,
        val priority: String?,
        val calendar: String?,
        val date: String?,
        val startTime: String?,
        val endTime: String?,
        val source: String,
        val created: String,
        val updated: String,
    )

    fun toJson(notes: List<NoteEntity>): String {
        val exportNotes = notes.map { it.toExportNote() }
        return json.encodeToString(ListSerializer(ExportNote.serializer()), exportNotes)
    }

    fun toCsv(notes: List<NoteEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("uid,title,body,kind,status,tags,priority,calendar,date,startTime,endTime,source,created,updated")
        for (note in notes) {
            sb.appendLine(
                listOf(
                    csvEscape(note.uid),
                    csvEscape(note.title),
                    csvEscape(note.body),
                    csvEscape(note.kind),
                    csvEscape(note.status),
                    csvEscape(NoteEntity.tagsFromJson(note.tags).joinToString("; ")),
                    csvEscape(note.priority ?: ""),
                    csvEscape(note.calendar ?: ""),
                    csvEscape(note.date ?: ""),
                    csvEscape(note.startTime ?: ""),
                    csvEscape(note.endTime ?: ""),
                    csvEscape(note.source),
                    csvEscape(note.created),
                    csvEscape(note.updated),
                ).joinToString(","),
            )
        }
        return sb.toString()
    }

    /** CSV-escape a value: quote if it contains commas, quotes, or newlines. */
    internal fun csvEscape(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun NoteEntity.toExportNote() = ExportNote(
        uid = uid,
        title = title,
        body = body,
        kind = kind,
        status = status,
        tags = NoteEntity.tagsFromJson(tags),
        priority = priority,
        calendar = calendar,
        date = date,
        startTime = startTime,
        endTime = endTime,
        source = source,
        created = created,
        updated = updated,
    )
}
