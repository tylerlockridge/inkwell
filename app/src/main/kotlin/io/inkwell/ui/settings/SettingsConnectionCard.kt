package io.inkwell.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.inkwell.ui.theme.StatusError
import io.inkwell.ui.theme.StatusPending
import io.inkwell.ui.theme.StatusSynced

@Composable
internal fun ConnectionCard(
    state: SettingsUiState,
    onServerUrlChange: (String) -> Unit,
    onAuthTokenChange: (String) -> Unit,
    onToggleTokenVisibility: () -> Unit,
    onTestConnection: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val isConnected = state.serverUrl.isNotBlank() && state.authToken.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isConnected) {
                // Connected header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = StatusSynced)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onDisconnect) {
                        Icon(
                            Icons.Outlined.LinkOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Disconnect",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        )
                    }
                }

                // Server URL display
                Text(
                    text = state.serverUrl
                        .removePrefix("https://")
                        .removePrefix("http://"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Connection test status
                ConnectionStatusIndicator(
                    status = state.connectionStatus,
                    onRetry = onTestConnection,
                )
            } else {
                // Not connected header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = StatusPending)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Not connected",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
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

                OutlinedTextField(
                    value = state.authToken,
                    onValueChange = onAuthTokenChange,
                    label = { Text("Auth Token") },
                    placeholder = { Text("Paste your server token") },
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Canvas(modifier = Modifier.size(6.dp)) {
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Canvas(modifier = Modifier.size(6.dp)) {
                    drawCircle(color = StatusSynced)
                }
                Text(
                    text = "Connection verified",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusSynced,
                )
            }
        }
        ConnectionStatus.Failed -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Canvas(modifier = Modifier.size(6.dp)) {
                    drawCircle(color = StatusError)
                }
                Text(
                    text = "Connection failed",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusError,
                )
                TextButton(onClick = onRetry) {
                    Text("Retry", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
