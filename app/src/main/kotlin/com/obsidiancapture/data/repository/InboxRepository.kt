package com.obsidiancapture.data.repository

import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.data.remote.dto.NoteUpdateRequest
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

            var syncedCount = 0
            for (item in response.items) {
                try {
                    val localNote = noteDao.getByUid(item.uid)

                    // Skip if local note has pending changes
                    if (localNote?.pendingSync == true) continue

                    // Skip if local version is same or newer (last-write-wins)
                    if (localNote != null && localNote.updated >= item.updated_at) continue

                    val detail = apiService.getNote(serverUrl, item.uid)
                    val serverNote = NoteEntity(
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

                    noteDao.upsert(serverNote)
                    syncedCount++
                } catch (e: Exception) {
                    android.util.Log.w("InboxRepository", "Skipping note ${item.uid}: ${e.javaClass.simpleName}: ${e.message}", e)
                }
            }

            // Also trigger upload of any pending local changes
            syncScheduler.triggerImmediateUpload()

            SyncResult.Success(syncedCount)
        } catch (e: Exception) {
            android.util.Log.w("InboxRepository", "Inbox sync failed", e)
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
            android.util.Log.w("InboxRepository", "Status update failed for $uid, queued for retry: ${e.javaClass.simpleName}", e)
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
            android.util.Log.w("InboxRepository", "Content update failed for $uid, queued for retry: ${e.javaClass.simpleName}", e)
            syncScheduler.triggerImmediateUpload()
            true
        }
    }

}

sealed class SyncResult {
    data class Success(val count: Int) : SyncResult()
    data object NoServer : SyncResult()
    data class Error(val message: String) : SyncResult()
}
