package com.obsidiancapture.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll

/**
 * Cross-process safe state updater for widgets.
 * Uses DataStore for persistence across process boundaries.
 */
object WidgetStateUpdater {

    private val Context.widgetDataStore by preferencesDataStore(name = "widget_state")

    val KEY_INBOX_COUNT = intPreferencesKey("inbox_count")
    val KEY_PENDING_SYNC_COUNT = intPreferencesKey("pending_sync_count")

    /**
     * Update widget counts and trigger widget refresh.
     */
    suspend fun updateCounts(context: Context, inboxCount: Int, pendingSyncCount: Int) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_INBOX_COUNT] = inboxCount
            prefs[KEY_PENDING_SYNC_COUNT] = pendingSyncCount
        }

        // Refresh both widgets
        try {
            InboxCountWidget().updateAll(context)
            QuickCaptureWidget().updateAll(context)
        } catch (_: Exception) {
            // Widget may not be placed — no-op
        }
    }
}
