package com.obsidiancapture.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 virtual table for full-text search on notes.
 * Linked to the `notes` table — kept in sync via database triggers.
 */
@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(
    val title: String,
    val body: String,
    val tags: String,
)
