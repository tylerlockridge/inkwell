package io.inkwell.sync

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.inkwell.MainActivity
import io.inkwell.R
import io.inkwell.notifications.NotificationChannels
import io.inkwell.data.local.PreferencesManager
import io.inkwell.data.local.dao.NoteDao
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.remote.CaptureApiService
import io.inkwell.data.remote.dto.CaptureRequest
import io.inkwell.data.remote.dto.NoteUpdateRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.first

/**
 * Background worker that uploads pending local changes to the server.
 *
 * Handles two types of pending notes:
 * 1. New captures (uid starts with "pending_") → POST /api/capture
 * 2. Existing note updates (status/content changes) → PATCH /api/note/:uid
 *
 * Error classification:
 * - 4xx (client errors): Permanent failure — mark sync_error, stop retrying
 * - 401: Auth invalid — return failure, stop all retries
 * - 5xx / network errors: Transient — retry with backoff
 */
@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val noteDao: NoteDao,
    private val apiService: CaptureApiService,
    private val preferencesManager: PreferencesManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return Result.success()

        val pendingNotes = noteDao.getPendingSync()
        if (pendingNotes.isEmpty()) return Result.success()

        var successCount = 0
        var retryableFailures = 0
        var permanentFailures = 0
        var authFailure = false

        for (note in pendingNotes) {
            try {
                if (note.uid.startsWith("pending_")) {
                    uploadNewCapture(serverUrl, note)
                } else {
                    uploadNoteUpdate(serverUrl, note)
                }
                successCount++
            } catch (e: ClientRequestException) {
                val status = e.response.status.value
                Log.w(TAG, "Upload failed for ${note.uid}: HTTP $status")
                when {
                    status == 401 -> {
                        // Mirror SyncWorker: clear stale token + notify user
                        preferencesManager.setAuthToken("")
                        postAuthExpiredNotification()
                        authFailure = true
                        break // Stop processing — auth is invalid
                    }
                    status in 400..499 -> {
                        // Permanent client error — stop retrying this note
                        permanentFailures++
                        noteDao.markSyncError(note.uid, "HTTP $status: ${e.message?.take(200)}")
                    }
                    else -> retryableFailures++
                }
            } catch (e: ServerResponseException) {
                // 5xx — transient server error
                Log.w(TAG, "Upload failed for ${note.uid}: server error ${e.response.status.value}")
                retryableFailures++
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Network or other transient error
                Log.w(TAG, "Upload failed for ${note.uid}: ${e.message}")
                retryableFailures++
            }
        }

        Log.i(TAG, "Upload complete: $successCount ok, $retryableFailures retryable, $permanentFailures permanent")

        return when {
            authFailure -> Result.failure()
            retryableFailures > 0 -> Result.retry()
            else -> Result.success()
        }
    }

    private suspend fun uploadNewCapture(serverUrl: String, note: NoteEntity) {
        val tags = NoteEntity.tagsFromJson(note.tags)
        val attachmentUris = NoteEntity.attachmentsFromJson(note.attachmentUris)
        val request = CaptureRequest(
            body = note.body,
            title = note.title.ifBlank { null },
            tags = tags.ifEmpty { null },
            kind = note.kind,
            date = note.date,
            startTime = note.startTime,
            endTime = note.endTime,
            calendar = note.calendar,
            priority = note.priority,
            source = "android",
            uuid = note.clientUuid,
        )

        val response = if (attachmentUris.isNotEmpty()) {
            apiService.captureWithAttachments(
                serverUrl, request, attachmentUris,
                applicationContext.contentResolver,
            )
        } else {
            apiService.capture(serverUrl, request)
        }
        val now = java.time.Instant.now().toString()

        // Atomically delete the pending_ row and insert the server-assigned row.
        // A plain upsert would create a second row (different PK), leaving the old
        // pending_ row to trigger duplicate uploads on every subsequent worker run.
        noteDao.replacePendingWithServer(
            pendingUid = note.uid,
            serverNote = note.copy(
                uid = response.uid,
                pendingSync = false,
                syncedAt = now,
                syncError = null,
            ),
        )
    }

    private suspend fun uploadNoteUpdate(serverUrl: String, note: NoteEntity) {
        val tags = NoteEntity.tagsFromJson(note.tags)
        val request = NoteUpdateRequest(
            status = note.status,
            title = note.title,
            body = note.body,
            tags = tags,
        )

        apiService.updateNote(serverUrl, note.uid, request)
        val now = java.time.Instant.now().toString()
        noteDao.markSynced(note.uid, now)
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

    companion object {
        private const val TAG = "UploadWorker"
        private const val NOTIFICATION_ID_AUTH_EXPIRED = 1002
    }
}
