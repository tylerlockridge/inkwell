package io.inkwell.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import io.inkwell.R

/**
 * 2x1 widget showing inbox open count + pending sync count.
 * Material You dynamic color with rounded corners and polished layout.
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

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("inkwell://inbox")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Outer container — rounded surface card
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(24.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .clickable(actionStartActivity(intent))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Inbox icon in a tonal circle
                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .cornerRadius(18.dp)
                        .background(GlanceTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_shortcut_inbox),
                        contentDescription = "Inbox",
                        modifier = GlanceModifier.size(18.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                    )
                }
                Spacer(modifier = GlanceModifier.width(10.dp))
                // Count and label stacked
                Column(
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "$inboxCount",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary,
                        ),
                    )
                    Text(
                        text = "Open Notes",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
            // Pending sync pill
            if (pendingSync > 0) {
                Spacer(modifier = GlanceModifier.height(6.dp))
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(12.dp)
                        .background(GlanceTheme.colors.tertiaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$pendingSync pending sync",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = GlanceTheme.colors.onTertiaryContainer,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
}
