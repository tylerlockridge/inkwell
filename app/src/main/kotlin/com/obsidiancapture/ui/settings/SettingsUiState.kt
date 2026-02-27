package com.obsidiancapture.ui.settings

import com.obsidiancapture.data.local.PreferencesManager

data class SettingsUiState(
    val serverUrl: String = PreferencesManager.DEFAULT_SERVER_URL,
    val authToken: String = "",
    val isTokenVisible: Boolean = false,
    val syncIntervalMinutes: Long = 15L,
    val lastSyncedAt: String? = null,
    val notificationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = true,
    val isTestingConnection: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Unknown,
    val snackbarMessage: String? = null,
    val isGoogleSignInLoading: Boolean = false,
    val googleSignInEmail: String? = null,
    val isExporting: Boolean = false,
)

enum class ConnectionStatus {
    Unknown,
    Connected,
    Failed,
    Testing,
}

val SYNC_INTERVAL_OPTIONS = listOf(
    15L to "15 minutes",
    30L to "30 minutes",
    60L to "1 hour",
    120L to "2 hours",
)
