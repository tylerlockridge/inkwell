package com.obsidiancapture.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.obsidiancapture.ui.theme.StatusError
import com.obsidiancapture.ui.theme.StatusPending
import com.obsidiancapture.ui.theme.StatusSynced

@Composable
internal fun ConnectionCard(
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
