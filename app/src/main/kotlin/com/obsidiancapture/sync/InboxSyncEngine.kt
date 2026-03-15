package com.obsidiancapture.sync

import android.util.Log
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.remote.CaptureApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared inbox sync logic used by both [SyncWorker] (periodic background)
 * and [com.obsidiancapture.data.repository.InboxRepository] (manual pull-to-refresh).
 *
 * Consolidates fetch → stale detection → concurrent detail fetch → bulk upsert → tombstone sweep
 * into a single code path to prevent drift between the two callers.
 */
@Singleton
class InboxSyncEngine @Inject constructor(
    private val noteDao: NoteDao,
    private val apiService: CaptureApiService,
    private val preferencesManager: PreferencesManager,
) {
    data class SyncOutcome(
        val successCount: Int,
        val failCount: Int,
        val tombstoneCount: Int,
    )

    /**
     * Fetch inbox from server, upsert stale items, and optionally run tombstone sweep.
     *
     * @param serverUrl Base URL of the capture server
     * @param runTombstoneSweep Whether to delete locally-cached notes that the server has marked deleted
     * @return [SyncOutcome] with counts, or throws on network/auth errors (caller handles)
     */
    suspend fun syncInbox(serverUrl: String, runTombstoneSweep: Boolean = true): SyncOutcome {
        // Capture before any sync writes so tombstone sweep only fetches
        // items orphaned since the last successful sync
        val priorSyncedAt = preferencesManager.lastSyncedAt.first().ifBlank { null }

        val response = apiService.getInbox(serverUrl, limit = INBOX_FETCH_LIMIT)
        val now = Instant.now().toString()

        // 1. Identify stale items — one bulk DB read instead of O(n) serial queries
        val localNoteMap = noteDao.getAllByUids(response.items.map { it.uid })
            .associateBy { it.uid }
        val staleItems = response.items.filter { item ->
            val localNote = localNoteMap[item.uid]
            localNote?.pendingSync != true &&
                (localNote == null || isServerNewer(localNote.updated, item.updated_at))
        }

        // 2. Fetch full detail for all stale items concurrently
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
                        Log.w(TAG, "Failed to sync note ${item.uid}: ${e.message}")
                        null
                    }
                }
            }.awaitAll()
        }

        // 3. Bulk upsert successful fetches (single transaction)
        val successNotes = fetchResults.filterNotNull()
        if (successNotes.isNotEmpty()) {
            noteDao.upsertAll(successNotes)
        }
        val successCount = successNotes.size
        val failCount = fetchResults.size - successCount

        if (failCount > 0) {
            Log.w(TAG, "Sync completed with errors: $successCount ok, $failCount failed")
        }

        // 4. Tombstone sweep: delete local notes the server has marked deleted
        var tombstoneCount = 0
        if (runTombstoneSweep) {
            try {
                val deleted = apiService.getDeletedInbox(serverUrl, since = priorSyncedAt)
                val deletedUids = deleted.items.map { it.uid }
                if (deletedUids.isNotEmpty()) {
                    noteDao.deleteByUidsIfSynced(deletedUids)
                    tombstoneCount = deletedUids.size
                    Log.d(TAG, "Tombstone sweep: removed $tombstoneCount orphaned note(s)")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w(TAG, "Tombstone sweep failed (non-fatal): ${e.message}")
            }
        }

        // 5. Record last successful sync time
        if (successCount > 0 || (failCount == 0 && response.items.isEmpty())) {
            preferencesManager.setLastSyncedAt(Instant.now().toString())
        }

        return SyncOutcome(successCount, failCount, tombstoneCount)
    }

    /**
     * Returns true if the server timestamp is strictly newer than the local timestamp.
     * Parses both as [Instant] to handle mixed-precision ISO 8601 strings
     * (e.g. "2026-02-28T10:00:00Z" vs "2026-02-28T10:00:00.000Z") correctly.
     * Falls back to treating the server as newer on any parse failure to avoid stale data.
     */
    private fun isServerNewer(localTs: String, serverTs: String): Boolean {
        return try {
            Instant.parse(localTs) < Instant.parse(serverTs)
        } catch (_: Exception) {
            true
        }
    }

    companion object {
        private const val TAG = "InboxSyncEngine"
        private const val INBOX_FETCH_LIMIT = 200
    }
}
