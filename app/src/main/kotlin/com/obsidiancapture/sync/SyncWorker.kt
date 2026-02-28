package com.obsidiancapture.sync

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.obsidiancapture.MainActivity
import com.obsidiancapture.R
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.notifications.NotificationChannels
import com.obsidiancapture.widget.WidgetStateUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/**
 * Periodic background worker that fetches inbox from server and upserts to Room.
 * Uses last-write-wins conflict resolution based on the `updated` timestamp.
 * Skips notes that have pending local changes (pendingSync = true).
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val noteDao: NoteDao,
    private val apiService: CaptureApiService,
    private val preferencesManager: PreferencesManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return Result.success()

        return try {
            val response = apiService.getInbox(serverUrl)
            val now = java.time.Instant.now().toString()

            // 1. Identify stale items via serial DB reads (fast, local)
            val staleItems = response.items.filter { item ->
                val localNote = noteDao.getByUid(item.uid)
                localNote?.pendingSync != true &&
                    (localNote == null || localNote.updated < item.updated_at)
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

            // 3. Upsert successful fetches (serial, DB writes)
            val successCount = fetchResults.count { it != null }
            val failCount = fetchResults.count { it == null }
            fetchResults.filterNotNull().forEach { noteDao.upsert(it) }

            if (failCount > 0) {
                Log.w(TAG, "Sync completed with errors: $successCount ok, $failCount failed")
            }

            // Record last successful sync time
            if (successCount > 0 || (failCount == 0 && response.items.isEmpty())) {
                preferencesManager.setLastSyncedAt(java.time.Instant.now().toString())
            }

            // Update widget counts
            updateWidgets()

            // Only retry if ALL notes failed (systemic issue like network down)
            if (failCount > 0 && successCount == 0 && response.items.isNotEmpty()) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: ClientRequestException) {
            val status = e.response.status.value
            Log.e(TAG, "Sync failed with HTTP $status: ${e.message}")
            if (status == 401) {
                // Auth invalid — clear stale token and notify user to re-authenticate
                preferencesManager.setAuthToken("")
                postAuthExpiredNotification()
                Result.failure()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
            Result.retry()
        }
    }

    private fun postAuthExpiredNotification() {
        try {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.SYNC_ERROR_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(applicationContext.getString(R.string.notification_auth_expired_title))
                .setContentText(applicationContext.getString(R.string.notification_auth_expired_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID_AUTH_EXPIRED, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to post auth expired notification: ${e.message}")
        }
    }

    private suspend fun updateWidgets() {
        try {
            val inboxCount = noteDao.getInboxNotes().first().size
            val pendingSyncCount = noteDao.getPendingSyncCount().first()
            WidgetStateUpdater.updateCounts(applicationContext, inboxCount, pendingSyncCount)
        } catch (_: Exception) {
            // Non-critical — widget update failure shouldn't affect sync
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val NOTIFICATION_ID_AUTH_EXPIRED = 1001
    }
}
