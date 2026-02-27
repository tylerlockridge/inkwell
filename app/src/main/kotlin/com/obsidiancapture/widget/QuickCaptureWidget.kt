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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

/**
 * 4x1 widget with quick capture button + inbox badge.
 * Capture button → opens Capture screen.
 * Inbox badge → opens Inbox screen.
 */
class QuickCaptureWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                QuickCaptureContent()
            }
        }
    }

    @Composable
    private fun QuickCaptureContent() {
        val prefs = currentState<Preferences>()
        val inboxCount = prefs[WidgetStateUpdater.KEY_INBOX_COUNT] ?: 0

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.widgetBackground),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Capture button area
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "\u270F\uFE0F Capture",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary,
                    ),
                )
                Text(
                    text = "Tap to create a note",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }

            // Inbox badge area
            Column(
                modifier = GlanceModifier.padding(start = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$inboxCount",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.secondary,
                    ),
                )
                Text(
                    text = "Inbox",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
            }
        }
    }
}
