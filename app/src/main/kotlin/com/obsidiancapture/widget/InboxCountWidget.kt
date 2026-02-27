package com.obsidiancapture.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.obsidiancapture.MainActivity

/**
 * 2x1 widget showing inbox open count + pending sync count.
 * Taps to open Inbox via deep link.
 */
class InboxCountWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                InboxCountContent()
            }
        }
    }

    @Composable
    private fun InboxCountContent() {
        val prefs = currentState<Preferences>()
        val inboxCount = prefs[WidgetStateUpdater.KEY_INBOX_COUNT] ?: 0
        val pendingSync = prefs[WidgetStateUpdater.KEY_PENDING_SYNC_COUNT] ?: 0

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("obsidiancapture://inbox")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.widgetBackground),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$inboxCount",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                ),
            )
            Text(
                text = "Open Notes",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurface,
                ),
            )
            if (pendingSync > 0) {
                Text(
                    text = "$pendingSync pending sync",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.secondary,
                    ),
                )
            }
        }
    }
}
