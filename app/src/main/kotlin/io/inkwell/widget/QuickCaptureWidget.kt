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
import androidx.glance.layout.fillMaxWidth
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
 * 4x1 widget with quick capture button + inbox badge.
 * Material You dynamic color with rounded corners and polished layout.
 * Capture button -> opens Capture screen.
 * Inbox badge -> opens Inbox screen.
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
        val pendingSync = prefs[WidgetStateUpdater.KEY_PENDING_SYNC_COUNT] ?: 0

        val captureIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("inkwell://capture"),
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val inboxIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("inkwell://inbox"),
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Outer container — rounded surface card
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(24.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Capture button area ──
            // Filled tonal button style: primary container background
            Row(
                modifier = GlanceModifier
                    .defaultWeight()
                    .cornerRadius(16.dp)
                    .background(GlanceTheme.colors.primaryContainer)
                    .clickable(actionStartActivity(captureIntent))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_shortcut_capture),
                    contentDescription = "Capture",
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Column {
                    Text(
                        text = "Capture",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onPrimaryContainer,
                        ),
                    )
                    Text(
                        text = "Tap to create a note",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.onPrimaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // ── Inbox badge area ──
            Column(
                modifier = GlanceModifier
                    .clickable(actionStartActivity(inboxIntent))
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Badge circle with count
                Box(
                    modifier = GlanceModifier
                        .size(42.dp)
                        .cornerRadius(21.dp)
                        .background(GlanceTheme.colors.secondary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$inboxCount",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSecondary,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "Inbox",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
                // Pending sync indicator
                if (pendingSync > 0) {
                    Text(
                        text = "$pendingSync pending",
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = GlanceTheme.colors.tertiary,
                        ),
                    )
                }
            }
        }
    }
}
