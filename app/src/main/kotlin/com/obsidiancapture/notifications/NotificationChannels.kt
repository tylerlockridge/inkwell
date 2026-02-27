package com.obsidiancapture.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val CAPTURE_CHANNEL_ID = "capture_new"
    const val GCAL_CHANNEL_ID = "capture_gcal"
    const val SYNC_STATUS_CHANNEL_ID = "sync_status"
    const val SYNC_ERROR_CHANNEL_ID = "sync_error"

    // Keep old IDs for migration — Android won't crash on these, just delete
    private const val OLD_CAPTURE_ID = "capture_notifications"
    private const val OLD_SYNC_ID = "sync_notifications"

    fun createAll(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Delete old channels (safe no-op if they don't exist)
        notificationManager.deleteNotificationChannel(OLD_CAPTURE_ID)
        notificationManager.deleteNotificationChannel(OLD_SYNC_ID)

        val captureChannel = NotificationChannel(
            CAPTURE_CHANNEL_ID,
            "New Captures",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifications when new captures arrive from other devices"
        }

        val gcalChannel = NotificationChannel(
            GCAL_CHANNEL_ID,
            "Calendar Sync",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Google Calendar event sync notifications"
        }

        val syncStatusChannel = NotificationChannel(
            SYNC_STATUS_CHANNEL_ID,
            "Sync Status",
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = "Background sync progress updates"
        }

        val syncErrorChannel = NotificationChannel(
            SYNC_ERROR_CHANNEL_ID,
            "Sync Errors",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Sync failure alerts requiring attention"
        }

        notificationManager.createNotificationChannels(
            listOf(captureChannel, gcalChannel, syncStatusChannel, syncErrorChannel),
        )
    }
}
