package com.obsidiancapture.data.repository

import android.util.Log
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.data.remote.dto.NoteUpdateRequest
import com.obsidiancapture.sync.InboxSyncEngine
import com.obsidiancapture.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxRepository @Inject constructor(
    private val apiService: CaptureApiService,
    private val noteDao: NoteDao,
    private val preferencesManager: PreferencesManager,
    private val syncScheduler: SyncScheduler,
    private val syncEngine: InboxSyncEngine,
) {
    fun getInboxNotes(): Flow<List<NoteEntity>> = noteDao.getInboxNotes()

    fun getReviewQueue(): Flow<List<NoteEntity>> = noteDao.getReviewQueue()

    fun getPendingSyncNotes(): Flow<List<NoteEntity>> = noteDao.getPendingSyncNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        // Use FTS for 3+ character queries for faster ranked results
        return if (query.length >= 3) {
            // Escape FTS4 special operators before appending prefix wildcard
            val escaped = query.replace(Regex("[*^~\"\\-]"), " ").trim()
            noteDao.searchNotesFts("$escaped*")
        } else {
            noteDao.searchNotes(query)
        }
    }

    val pendingSyncCount: Flow<Int> = noteDao.getPendingSyncCount()

    suspend fun getNote(uid: String): NoteEntity? = noteDao.getByUid(uid)

    suspend fun syncInbox(): SyncResult {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return SyncResult.NoServer

        return try {
            val outcome = syncEngine.syncInbox(serverUrl, runTombstoneSweep = true)

            // Also trigger upload of any pending local changes
            syncScheduler.triggerImmediateUpload()

            SyncResult.Success(outcome.successCount)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.w(TAG, "Inbox sync failed", e)
            SyncResult.Error("Sync failed: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    suspend fun updateNoteStatus(uid: String, status: String): Boolean {
        val now = Instant.now().toString()
        noteDao.updateStatus(uid, status, now)

        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) {
            syncScheduler.triggerImmediateUpload()
            return true
        }

        return try {
            apiService.updateNote(serverUrl, uid, NoteUpdateRequest(status = status))
            noteDao.markSynced(uid, now)
            true
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.w(TAG, "Status update failed for $uid, queued for retry: ${e.javaClass.simpleName}", e)
            // Already marked pendingSync, upload worker will retry
            syncScheduler.triggerImmediateUpload()
            true
        }
    }

    suspend fun updateNoteContent(
        uid: String,
        title: String,
        body: String,
        tags: List<String>,
    ): Boolean {
        val now = Instant.now().toString()
        noteDao.updateContent(uid, title, body, NoteEntity.tagsToJson(tags), now)

        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) {
            syncScheduler.triggerImmediateUpload()
            return true
        }

        return try {
            apiService.updateNote(
                serverUrl,
                uid,
                NoteUpdateRequest(title = title, body = body, tags = tags),
            )
            noteDao.markSynced(uid, now)
            true
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.w(TAG, "Content update failed for $uid, queued for retry: ${e.javaClass.simpleName}", e)
            syncScheduler.triggerImmediateUpload()
            true
        }
    }

    companion object {
        private const val TAG = "InboxRepository"
    }
}

sealed class SyncResult {
    data class Success(val count: Int) : SyncResult()
    data object NoServer : SyncResult()
    data class Error(val message: String) : SyncResult()
}
