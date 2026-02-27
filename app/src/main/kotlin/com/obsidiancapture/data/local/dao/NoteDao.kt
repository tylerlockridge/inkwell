package com.obsidiancapture.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.obsidiancapture.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * FROM notes WHERE status = 'open' ORDER BY created DESC")
    fun getInboxNotes(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM notes WHERE status = 'open' AND kind != 'one_shot' ORDER BY created DESC")
    fun getReviewQueue(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM notes WHERE pending_sync = 1 ORDER BY created DESC")
    fun getPendingSyncNotes(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM notes WHERE status = 'open' AND (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') ORDER BY created DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    /**
     * FTS4 full-text search. Use for queries of 3+ characters for ranked results.
     * Falls back to searchNotes() for 1-2 char queries.
     */
    @Transaction
    @Query(
        """
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.rowid = notes_fts.rowid
        WHERE notes_fts MATCH :query
        AND notes.status = 'open'
        ORDER BY notes.created DESC
        """,
    )
    fun searchNotesFts(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE uid = :uid")
    suspend fun getByUid(uid: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Query("SELECT * FROM notes WHERE pending_sync = 1")
    suspend fun getPendingSync(): List<NoteEntity>

    @Query("SELECT COUNT(*) FROM notes WHERE status = 'open'")
    fun getInboxNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE pending_sync = 1")
    fun getPendingSyncCount(): Flow<Int>

    @Query("UPDATE notes SET status = :status, updated = :updated, pending_sync = 1 WHERE uid = :uid")
    suspend fun updateStatus(uid: String, status: String, updated: String)

    @Query("UPDATE notes SET title = :title, body = :body, tags = :tags, updated = :updated, pending_sync = 1 WHERE uid = :uid")
    suspend fun updateContent(uid: String, title: String, body: String, tags: String, updated: String)

    @Query("UPDATE notes SET pending_sync = 0, synced_at = :syncedAt, sync_error = NULL WHERE uid = :uid")
    suspend fun markSynced(uid: String, syncedAt: String)

    @Query("UPDATE notes SET pending_sync = 0, sync_error = :error WHERE uid = :uid")
    suspend fun markSyncError(uid: String, error: String)

    @Query("UPDATE notes SET sync_error = NULL WHERE uid = :uid")
    suspend fun clearSyncError(uid: String)

    /** Get all notes (for export). */
    @Query("SELECT * FROM notes ORDER BY created DESC")
    suspend fun getAllNotes(): List<NoteEntity>
}
