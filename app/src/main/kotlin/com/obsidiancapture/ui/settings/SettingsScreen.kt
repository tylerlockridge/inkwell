package com.obsidiancapture.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.obsidiancapture.BuildConfig
import com.obsidiancapture.auth.BiometricAuthManager

/**
 * Google Cloud web client ID for token verification.
 * This must match the web client ID configured on the server in config.yaml.
 * From the same GCloud project (765700506610) used for GCal OAuth.
 */
private const val GOOGLE_WEB_CLIENT_ID = "765700506610-fe91rq42adiv4esgks24djdfb0k5bghh.apps.googleusercontent.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHealth: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Check biometric capability on composition
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val manager = BiometricAuthManager()
            val capability = manager.checkCapability(activity)
            viewModel.onBiometricCapabilityChecked(
                capability == BiometricAuthManager.Capability.AVAILABLE,
            )
        }
    }

    // Android 13+ runtime notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onNotificationsToggle()
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // System Health — quick access card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                onClick = onNavigateToHealth,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Outlined.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "System Health",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "Server, Syncthing, GCal sync status",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // Server Connection — state-driven card
            SectionHeader("Server Connection")
            ConnectionCard(
                state = state,
                onServerUrlChange = viewModel::onServerUrlChange,
                onAuthTokenChange = viewModel::onAuthTokenChange,
                onToggleTokenVisibility = viewModel::onToggleTokenVisibility,
                onTestConnection = viewModel::onTestConnection,
                onDisconnect = viewModel::onDisconnect,
                onGoogleSignIn = { ctx ->
                    viewModel.onGoogleSignIn(ctx, GOOGLE_WEB_CLIENT_ID)
                },
            )

            // Sync Settings
            SectionHeader("Sync")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SyncIntervalDropdown(
                        selectedInterval = state.syncIntervalMinutes,
                        onIntervalChange = viewModel::onSyncIntervalChange,
                    )

                    Text(
                        text = "Last synced: ${state.lastSyncedAt ?: "Not yet synced"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    HorizontalDivider()

                    OutlinedButton(
                        onClick = viewModel::onTriggerSync,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Sync, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sync Now")
                    }
                }
            }

            // Notifications
            SectionHeader("Notifications")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsToggle(
                        label = "Push Notifications",
                        description = "Get notified when new captures arrive",
                        icon = Icons.Outlined.Notifications,
                        checked = state.notificationsEnabled,
                        onToggle = {
                            if (!state.notificationsEnabled) {
                                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) != PackageManager.PERMISSION_GRANTED
                                if (needsPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.onNotificationsToggle()
                                }
                            } else {
                                viewModel.onNotificationsToggle()
                            }
                        },
                    )
                }
            }

            // Security & Feedback
            SectionHeader("Security & Feedback")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettingsToggle(
                        label = "Biometric Unlock",
                        description = if (state.biometricAvailable) {
                            "Require fingerprint or face to open app"
                        } else {
                            "Not available on this device"
                        },
                        icon = Icons.Outlined.Fingerprint,
                        checked = state.biometricEnabled,
                        onToggle = viewModel::onBiometricToggle,
                        enabled = state.biometricAvailable,
                    )
                    HorizontalDivider()
                    SettingsToggle(
                        label = "Haptic Feedback",
                        description = "Vibrate on taps, captures, and interactions",
                        icon = Icons.Outlined.Vibration,
                        checked = state.hapticsEnabled,
                        onToggle = viewModel::onHapticsToggle,
                    )
                }
            }

            // Data Export
            SectionHeader("Data")
            ExportSection(
                isExporting = state.isExporting,
                onExportJson = viewModel::onExportJson,
                onExportCsv = viewModel::onExportCsv,
            )

            // App version
            Text(
                text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
