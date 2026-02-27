package com.obsidiancapture.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.obsidiancapture.data.repository.InboxRepository
import com.obsidiancapture.sync.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles notification action button taps without opening the app.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var inboxRepository: InboxRepository

    @Inject
    lateinit var syncScheduler: SyncScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        when (intent.action) {
            ACTION_MARK_DONE -> {
                val uid = intent.getStringExtra(EXTRA_NOTE_UID) ?: return
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
                scope.launch {
                    try {
                        inboxRepository.updateNoteStatus(uid, "done")
                        // Dismiss the notification
                        if (notificationId != 0) {
                            NotificationManagerCompat.from(context).cancel(notificationId)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_RETRY_SYNC -> {
                syncScheduler.triggerImmediateSync()
                // Dismiss the error notification
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
                if (notificationId != 0) {
                    try {
                        NotificationManagerCompat.from(context).cancel(notificationId)
                    } catch (_: SecurityException) {
                        // POST_NOTIFICATIONS permission not granted
                    }
                }
                pendingResult.finish()
            }
            else -> pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "com.obsidiancapture.ACTION_MARK_DONE"
        const val ACTION_RETRY_SYNC = "com.obsidiancapture.ACTION_RETRY_SYNC"
        const val EXTRA_NOTE_UID = "extra_note_uid"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
