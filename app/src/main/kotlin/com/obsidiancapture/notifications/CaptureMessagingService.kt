package com.obsidiancapture.notifications

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.obsidiancapture.MainActivity
import com.obsidiancapture.R
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.data.remote.dto.DeviceRegisterRequest
import com.obsidiancapture.sync.SyncScheduler
import com.obsidiancapture.ui.navigation.DeepLink
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CaptureMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var apiService: CaptureApiService

    @Inject
    lateinit var syncScheduler: SyncScheduler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            preferencesManager.setFcmToken(token)
            registerTokenWithServer(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return

        when (type) {
            "new_capture" -> handleNewCapture(data)
            "sync_required" -> handleSyncRequired()
            "sync_error" -> handleSyncError(data)
        }
    }

    private fun handleNewCapture(data: Map<String, String>) {
        val title = data["title"] ?: "New Capture"
        val body = data["body"] ?: "A new note was captured"
        val uid = data["uid"]

        // Trigger a sync to pull the new note
        syncScheduler.triggerImmediateSync()

        // Tap intent: deep link to note detail (or inbox if no uid)
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            this.data = if (uid != null) DeepLink.noteUri(uid) else DeepLink.inboxUri()
        }
        val tapPendingIntent = PendingIntent.getActivity(
            this,
            CAPTURE_NOTIFICATION_ID,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, NotificationChannels.CAPTURE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)

        // "View" action — same as tap, opens the note
        val viewIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            this.data = if (uid != null) DeepLink.noteUri(uid) else DeepLink.inboxUri()
        }
        val viewPendingIntent = PendingIntent.getActivity(
            this,
            CAPTURE_NOTIFICATION_ID + 100,
            viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification,
                "View",
                viewPendingIntent,
            ).build(),
        )

        // "Mark Done" action — updates note without opening app
        if (uid != null) {
            val doneIntent = Intent(this, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_DONE
                putExtra(NotificationActionReceiver.EXTRA_NOTE_UID, uid)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, CAPTURE_NOTIFICATION_ID)
            }
            val donePendingIntent = PendingIntent.getBroadcast(
                this,
                CAPTURE_NOTIFICATION_ID + 200,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_notification,
                    "Mark Done",
                    donePendingIntent,
                ).build(),
            )
        }

        showNotification(CAPTURE_NOTIFICATION_ID, builder.build())
    }

    private fun handleSyncRequired() {
        syncScheduler.triggerImmediateSync()
    }

    private fun handleSyncError(data: Map<String, String>) {
        val errorMessage = data["message"] ?: "Sync failed"

        // Tap intent: open app to settings
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            this.data = DeepLink.systemHealthUri()
        }
        val tapPendingIntent = PendingIntent.getActivity(
            this,
            SYNC_ERROR_NOTIFICATION_ID,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, NotificationChannels.SYNC_ERROR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Sync Error")
            .setContentText(errorMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)

        // "Retry Sync" action
        val retryIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_RETRY_SYNC
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, SYNC_ERROR_NOTIFICATION_ID)
        }
        val retryPendingIntent = PendingIntent.getBroadcast(
            this,
            SYNC_ERROR_NOTIFICATION_ID + 100,
            retryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification,
                "Retry Sync",
                retryPendingIntent,
            ).build(),
        )

        showNotification(SYNC_ERROR_NOTIFICATION_ID, builder.build())
    }

    private fun showNotification(notificationId: Int, notification: android.app.Notification) {
        try {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    private suspend fun registerTokenWithServer(token: String) {
        try {
            val serverUrl = preferencesManager.serverUrl.first()
            if (serverUrl.isBlank()) return

            val deviceId = preferencesManager.deviceId.first()
            if (deviceId.isBlank()) return

            apiService.registerDevice(
                serverUrl,
                DeviceRegisterRequest(deviceId = deviceId, fcmToken = token),
            )
        } catch (_: Exception) {
            // Will retry on next token refresh or app startup
        }
    }

    companion object {
        private const val CAPTURE_NOTIFICATION_ID = 1001
        private const val SYNC_ERROR_NOTIFICATION_ID = 1002
    }
}
