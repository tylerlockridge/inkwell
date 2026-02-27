package com.obsidiancapture.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.obsidiancapture.BuildConfig
import com.obsidiancapture.auth.BiometricAuthManager
import com.obsidiancapture.ui.settings.ExportSection
import com.obsidiancapture.ui.theme.StatusError
import com.obsidiancapture.ui.theme.StatusPending
import com.obsidiancapture.ui.theme.StatusSynced

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHealth: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
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

@Composable
private fun ConnectionCard(
    state: SettingsUiState,
    onServerUrlChange: (String) -> Unit,
    onAuthTokenChange: (String) -> Unit,
    onToggleTokenVisibility: () -> Unit,
    onTestConnection: () -> Unit,
    onDisconnect: () -> Unit,
    onGoogleSignIn: (context: android.content.Context) -> Unit,
) {
    val isConnected = state.serverUrl.isNotBlank() && state.authToken.isNotBlank()
    var showManualToken by remember { mutableStateOf(false) }

    if (isConnected) {
        // Connected state card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = StatusSynced)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(onClick = onDisconnect) {
                        Icon(
                            Icons.Outlined.LinkOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Disconnect", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Server URL (truncated display)
                val displayUrl = state.serverUrl
                    .removePrefix("https://")
                    .removePrefix("http://")
                Text(
                    text = displayUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )

                if (state.googleSignInEmail != null) {
                    Text(
                        text = "as ${state.googleSignInEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }

                ConnectionStatusIndicator(
                    status = state.connectionStatus,
                    onRetry = onTestConnection,
                )
            }
        }
    } else {
        // Not connected state card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Not Connected",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }

                OutlinedTextField(
                    value = state.serverUrl,
                    onValueChange = onServerUrlChange,
                    label = { Text("Server URL") },
                    placeholder = { Text("https://your-server.example.com") },
                    leadingIcon = { Icon(Icons.Outlined.Cloud, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                GoogleSignInButton(
                    isLoading = state.isGoogleSignInLoading,
                    onSignIn = onGoogleSignIn,
                )

                // Collapsible manual token entry
                TextButton(
                    onClick = { showManualToken = !showManualToken },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (showManualToken) "Hide manual token entry" else "Enter token manually",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }

                AnimatedVisibility(
                    visible = showManualToken,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    OutlinedTextField(
                        value = state.authToken,
                        onValueChange = onAuthTokenChange,
                        label = { Text("Auth Token") },
                        placeholder = { Text("Bearer token") },
                        leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = onToggleTokenVisibility) {
                                Icon(
                                    imageVector = if (state.isTokenVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (state.isTokenVisible) "Hide token" else "Show token",
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (state.isTokenVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    onRetry: () -> Unit,
) {
    when (status) {
        ConnectionStatus.Unknown -> {}
        ConnectionStatus.Testing -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = StatusPending)
                }
                Text(
                    text = "Checking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusPending,
                )
            }
        }
        ConnectionStatus.Connected -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = StatusSynced)
                }
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusSynced,
                )
            }
        }
        ConnectionStatus.Failed -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = StatusError)
                }
                Text(
                    text = "Connection failed",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusError,
                )
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(1.5.dp),
                ),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            )
        }
        Switch(checked = checked, onCheckedChange = { onToggle() }, enabled = enabled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyncIntervalDropdown(
    selectedInterval: Long,
    onIntervalChange: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = SYNC_INTERVAL_OPTIONS.find { it.first == selectedInterval }?.second ?: "15 minutes"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sync Interval") },
            leadingIcon = { Icon(Icons.Outlined.Sync, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SYNC_INTERVAL_OPTIONS.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onIntervalChange(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    onSignIn: (context: android.content.Context) -> Unit,
) {
    val context = LocalContext.current

    FilledTonalButton(
        onClick = { onSignIn(context) },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(8.dp))
            Text("Signing in...")
        } else {
            Icon(Icons.Outlined.PersonAdd, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sign in with Google")
        }
    }
}

/**
 * Google Cloud web client ID for token verification.
 * This must match the web client ID configured on the server in config.yaml.
 * From the same GCloud project (765700506610) used for GCal OAuth.
 */
private const val GOOGLE_WEB_CLIENT_ID = "765700506610-fe91rq42adiv4esgks24djdfb0k5bghh.apps.googleusercontent.com"
