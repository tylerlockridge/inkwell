package com.obsidiancapture.data.repository

import android.util.Log
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.data.remote.dto.NoteUpdateRequest
import com.obsidiancapture.sync.SyncScheduler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
            val response = apiService.getInbox(serverUrl, limit = 200)
            val now = Instant.now().toString()

            // Bulk DB lookup instead of O(n) serial getByUid() calls
            val localNoteMap = noteDao.getAllByUids(response.items.map { it.uid })
                .associateBy { it.uid }

            // Filter to items that need syncing
            val staleItems = response.items.filter { item ->
                val localNote = localNoteMap[item.uid]
                localNote?.pendingSync != true &&
                    (localNote == null || isServerNewer(localNote.updated, item.updated_at))
            }

            // Concurrent detail fetches instead of serial N+1
            val fetchResults: List<NoteEntity?> = coroutineScope {
                staleItems.map { item ->
                    async {
                        try {
                            val detail = apiService.getNote(serverUrl, item.uid)
                            NoteEntity(
                                uid = detail.uid,
                                title = detail.frontmatter.title ?: "",
                                body = detail.body,
                                kind = detail.frontmatter.kind,
                                status = detail.frontmatter.status,
                                tags = NoteEntity.tagsToJson(detail.frontmatter.tags),
                                priority = detail.frontmatter.priority,
                                calendar = detail.frontmatter.calendar,
                                date = detail.frontmatter.date,
                                startTime = detail.frontmatter.startTime,
                                endTime = detail.frontmatter.endTime,
                                source = detail.frontmatter.source ?: "web",
                                gcalEnabled = detail.gcalStatus != null,
                                gcalEventId = detail.gcalStatus?.eventId,
                                gcalLastPushedAt = detail.gcalStatus?.lastPushedAt,
                                created = detail.frontmatter.created,
                                updated = detail.frontmatter.updated,
                                syncedAt = now,
                                pendingSync = false,
                            )
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            Log.w(TAG, "Skipping note ${item.uid}: ${e.javaClass.simpleName}: ${e.message}")
                            null
                        }
                    }
                }.awaitAll()
            }

            // Upsert successful fetches
            val syncedCount = fetchResults.filterNotNull().also { notes ->
                notes.forEach { noteDao.upsert(it) }
            }.size

            // Also trigger upload of any pending local changes
            syncScheduler.triggerImmediateUpload()

            SyncResult.Success(syncedCount)
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

    /**
     * Returns true if the server timestamp is strictly newer than the local timestamp.
     * Parses as Instant to handle mixed-precision ISO 8601 (e.g. with/without millis).
     * Falls back to treating server as newer on parse failure to avoid stale data.
     */
    private fun isServerNewer(localTs: String, serverTs: String): Boolean {
        return try {
            Instant.parse(localTs) < Instant.parse(serverTs)
        } catch (_: Exception) {
            true
        }
    }
}

sealed class SyncResult {
    data class Success(val count: Int) : SyncResult()
    data object NoServer : SyncResult()
    data class Error(val message: String) : SyncResult()
}
