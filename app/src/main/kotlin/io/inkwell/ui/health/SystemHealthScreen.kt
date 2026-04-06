package io.inkwell.ui.health

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.inkwell.ui.theme.StatusError
import io.inkwell.ui.theme.StatusPending
import io.inkwell.ui.theme.StatusSynced

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemHealthScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemHealthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestartDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "System Health",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Server
                SectionLabel("Server")
                HealthCard {
                    StatusRow("Status", state.serverStatus, statusColor(state.serverStatus))
                    StatusRow("Version", state.version)
                    StatusRow("Uptime", formatUptime(state.uptimeSeconds))
                    StatusRow("Memory", "${state.memoryUsageMB} MB")
                    StatusRow("Vault", if (state.vaultAccessible) "Accessible" else "Inaccessible",
                        if (state.vaultAccessible) StatusSynced else StatusError)
                    state.diskUsagePercent?.let { disk ->
                        StatusRow("Disk", "$disk%", if (disk > 90) StatusError else if (disk > 75) StatusPending else StatusSynced)
                    }
                }

                // Inbox Summary
                SectionLabel("Inbox")
                HealthCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatBlock(Icons.Outlined.Inbox, state.inboxTotal.toString(), "Open")
                        StatBlock(Icons.Outlined.FolderOpen, state.inboxToday.toString(), "Today")
                        StatBlock(Icons.Outlined.Sync, state.reviewPending.toString(), "Review")
                    }
                }

                // Google Calendar
                SectionLabel("Google Calendar")
                HealthCard {
                    StatusRow("Last Sync", state.gcalLastSync ?: "Never")
                    StatusRow("Errors", state.gcalErrors.toString(),
                        if (state.gcalErrors > 0) StatusError else StatusSynced)
                }

                // Syncthing
                if (state.syncthingEnabled) {
                    SectionLabel("Syncthing")
                    HealthCard {
                        // Status header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Canvas(modifier = Modifier.size(8.dp)) {
                                drawCircle(color = if (state.syncthingReachable) StatusSynced else StatusError)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (state.syncthingReachable) "Reachable" else "Unreachable",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (state.syncthingReachable) StatusSynced else StatusError,
                            )
                            if (state.syncthingVersion.isNotEmpty()) {
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = state.syncthingVersion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }

                        if (state.syncthingReachable && state.syncthingUptime > 0) {
                            StatusRow("Uptime", formatUptime(state.syncthingUptime))
                        }

                        // Devices
                        if (state.syncthingDevices.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            SubsectionLabel("Devices")
                            state.syncthingDevices.forEach { device ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Canvas(modifier = Modifier.size(6.dp)) {
                                            drawCircle(color = if (device.connected) StatusSynced else StatusError)
                                        }
                                        Text(device.name, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(
                                        if (device.connected) "Connected" else "Offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (device.connected) StatusSynced else MaterialTheme.colorScheme.outlineVariant,
                                    )
                                }
                            }
                        }

                        // Folders
                        if (state.syncthingFolders.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            SubsectionLabel("Folders")
                            state.syncthingFolders.forEach { folder ->
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(folder.label, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "${folder.completion}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { folder.completion / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp),
                                    )
                                    if (folder.errors > 0) {
                                        Text(
                                            "${folder.errors} error(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusError,
                                        )
                                    }
                                }
                            }
                        }

                        // Restart button
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        FilledTonalButton(
                            onClick = { showRestartDialog = true },
                            enabled = state.syncthingReachable && !state.isRestarting,
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                        ) {
                            if (state.isRestarting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Restarting...", style = MaterialTheme.typography.labelMedium)
                            } else {
                                Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Restart Syncthing", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Restart confirmation dialog
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart Syncthing?") },
            text = { Text("This will briefly interrupt file sync. Active transfers will resume after restart.") },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    viewModel.restartSyncthing()
                }) { Text("Restart") }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) { Text("Cancel") }
            },
        )
    }
}

// =============================================================================
// Health screen building blocks
// =============================================================================

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 11.sp,
        ),
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun SubsectionLabel(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 2.dp),
    )
}

@Composable
private fun HealthCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    dotColor: androidx.compose.ui.graphics.Color? = null,
) {
    val isPlaceholder = value == "Never" || value == "unknown" || value == "0" || value == "0 MB"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (dotColor != null) {
                Canvas(modifier = Modifier.size(6.dp)) {
                    drawCircle(color = dotColor)
                }
            }
            Text(
                value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium,
                ),
                color = if (isPlaceholder) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun StatBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun statusColor(status: String) = when (status) {
    "ok" -> StatusSynced
    "degraded" -> StatusPending
    "error" -> StatusError
    else -> StatusPending
}

private fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
