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
import com.obsidiancapture.notifications.NotificationChannels
import com.obsidiancapture.widget.WidgetStateUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.plugins.ClientRequestException
import kotlinx.serialization.SerializationException
import kotlinx.coroutines.flow.first

/**
 * Periodic background worker that fetches inbox from server and upserts to Room.
 * Delegates core sync logic to [InboxSyncEngine] to share code with manual pull-to-refresh.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val noteDao: NoteDao,
    private val preferencesManager: PreferencesManager,
    private val syncEngine: InboxSyncEngine,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return Result.success()

        return try {
            val outcome = syncEngine.syncInbox(serverUrl, runTombstoneSweep = true)

            // Update widget counts
            updateWidgets()

            // Only retry if ALL notes failed (systemic issue like network down)
            if (outcome.failCount > 0 && outcome.successCount == 0) {
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
        } catch (e: SerializationException) {
            // Server returned non-InboxResponse JSON (e.g. error envelope).
            Log.e(TAG, "Sync failed: unexpected response format: ${e.message}")
            Result.retry()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e // Respect structured cancellation — don't retry cancelled work
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
            val inboxCount = noteDao.getInboxNotesCount().first()
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
