package com.obsidiancapture.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["status"]),
        Index(value = ["pending_sync"]),
    ],
)
data class NoteEntity(
    @PrimaryKey val uid: String,
    val title: String,
    val body: String,
    val kind: String = "one_shot",
    val status: String = "open",
    val tags: String = "[]",
    val priority: String? = null,
    val calendar: String? = null,
    val date: String? = null,
    @ColumnInfo(name = "start_time") val startTime: String? = null,
    @ColumnInfo(name = "end_time") val endTime: String? = null,
    val source: String = "android",
    @ColumnInfo(name = "gcal_enabled") val gcalEnabled: Boolean = false,
    @ColumnInfo(name = "gcal_event_id") val gcalEventId: String? = null,
    @ColumnInfo(name = "gcal_last_pushed_at") val gcalLastPushedAt: String? = null,
    val created: String,
    val updated: String,
    @ColumnInfo(name = "synced_at") val syncedAt: String? = null,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = false,
    @ColumnInfo(name = "client_uuid") val clientUuid: String? = null,
    @ColumnInfo(name = "sync_error") val syncError: String? = null,
) {
    companion object {
        private val tagJson = Json { ignoreUnknownKeys = true }
        private val tagSerializer = ListSerializer(String.serializer())

        /** Encode a tag list to JSON string for storage */
        fun tagsToJson(tags: List<String>?): String {
            if (tags.isNullOrEmpty()) return "[]"
            return tagJson.encodeToString(tagSerializer, tags)
        }

        /** Decode a JSON string to tag list */
        fun tagsFromJson(json: String): List<String> {
            if (json.isBlank() || json == "[]") return emptyList()
            return try {
                tagJson.decodeFromString(tagSerializer, json)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
