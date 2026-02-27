package com.obsidiancapture.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsidiancapture.auth.GoogleSignInManager
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val preferencesManager: PreferencesManager,
    private val apiService: CaptureApiService,
    private val syncScheduler: SyncScheduler,
    private val googleSignInManager: GoogleSignInManager,
    private val noteDao: NoteDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val serverUrl = preferencesManager.serverUrl.first()
            val authToken = preferencesManager.authToken.first()
            val syncInterval = preferencesManager.syncIntervalMinutes.first()
            val notificationsEnabled = preferencesManager.notificationsEnabled.first()
            val biometricEnabled = preferencesManager.biometricEnabled.first()
            val hapticsEnabled = preferencesManager.hapticsEnabled.first()
            val lastSynced = preferencesManager.lastSyncedAt.first()

            _uiState.update {
                it.copy(
                    serverUrl = serverUrl,
                    authToken = authToken,
                    syncIntervalMinutes = syncInterval,
                    notificationsEnabled = notificationsEnabled,
                    biometricEnabled = biometricEnabled,
                    hapticsEnabled = hapticsEnabled,
                    lastSyncedAt = lastSynced.ifBlank { "Never" },
                )
            }

            if (serverUrl.isNotBlank()) {
                onTestConnection()
            }
        }
    }

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url, connectionStatus = ConnectionStatus.Unknown) }
        viewModelScope.launch {
            try {
                preferencesManager.setServerUrl(url)
            } catch (_: IllegalArgumentException) {
                _uiState.update { it.copy(snackbarMessage = "URL must use HTTPS (or localhost for dev)") }
            }
        }
    }

    fun onAuthTokenChange(token: String) {
        _uiState.update { it.copy(authToken = token, connectionStatus = ConnectionStatus.Unknown) }
        viewModelScope.launch { preferencesManager.setAuthToken(token) }
    }

    fun onToggleTokenVisibility() {
        _uiState.update { it.copy(isTokenVisible = !it.isTokenVisible) }
    }

    fun onDisconnect() {
        viewModelScope.launch {
            preferencesManager.setAuthToken("")
            _uiState.update {
                it.copy(
                    authToken = "",
                    connectionStatus = ConnectionStatus.Unknown,
                    googleSignInEmail = null,
                    snackbarMessage = "Disconnected",
                )
            }
        }
    }

    fun onSyncIntervalChange(minutes: Long) {
        _uiState.update { it.copy(syncIntervalMinutes = minutes) }
        viewModelScope.launch {
            preferencesManager.setSyncIntervalMinutes(minutes)
            syncScheduler.cancelAll()
            syncScheduler.schedulePeriodicSync()
        }
    }

    fun onNotificationsToggle() {
        val newValue = !_uiState.value.notificationsEnabled
        _uiState.update { it.copy(notificationsEnabled = newValue) }
        viewModelScope.launch { preferencesManager.setNotificationsEnabled(newValue) }
    }

    fun onHapticsToggle() {
        val newValue = !_uiState.value.hapticsEnabled
        _uiState.update { it.copy(hapticsEnabled = newValue) }
        viewModelScope.launch { preferencesManager.setHapticsEnabled(newValue) }
    }

    fun onBiometricToggle() {
        val newValue = !_uiState.value.biometricEnabled
        _uiState.update { it.copy(biometricEnabled = newValue) }
        viewModelScope.launch { preferencesManager.setBiometricEnabled(newValue) }
    }

    /**
     * Update biometric capability status (called from SettingsScreen after checking device support).
     */
    fun onBiometricCapabilityChecked(isAvailable: Boolean) {
        _uiState.update { it.copy(biometricAvailable = isAvailable) }
    }

    fun onTestConnection() {
        val url = _uiState.value.serverUrl
        if (url.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Enter a server URL first") }
            return
        }

        _uiState.update { it.copy(isTestingConnection = true, connectionStatus = ConnectionStatus.Testing) }

        viewModelScope.launch {
            val healthy = apiService.healthCheck(url)
            _uiState.update {
                it.copy(
                    isTestingConnection = false,
                    connectionStatus = if (healthy) ConnectionStatus.Connected else ConnectionStatus.Failed,
                    snackbarMessage = if (healthy) "Connected successfully" else "Connection failed",
                )
            }
        }
    }

    fun onTriggerSync() {
        syncScheduler.triggerImmediateSync()
        _uiState.update { it.copy(snackbarMessage = "Sync triggered") }
    }

    fun onGoogleSignIn(activityContext: android.content.Context, webClientId: String) {
        _uiState.update { it.copy(isGoogleSignInLoading = true) }

        viewModelScope.launch {
            val serverUrl = _uiState.value.serverUrl
            if (serverUrl.isBlank()) {
                _uiState.update {
                    it.copy(isGoogleSignInLoading = false, snackbarMessage = "Enter a server URL first")
                }
                return@launch
            }

            // Step 1: Get Google ID token from Credential Manager
            val idTokenResult = googleSignInManager.signIn(activityContext, webClientId)
            if (idTokenResult.isFailure) {
                val msg = idTokenResult.exceptionOrNull()?.message ?: "Sign-in failed"
                _uiState.update {
                    it.copy(isGoogleSignInLoading = false, snackbarMessage = msg)
                }
                return@launch
            }

            val idToken = idTokenResult.getOrThrow()
            // Extract email from JWT payload before exchanging the token
            val email = extractEmailFromJwt(idToken)

            // Step 2: Exchange ID token for app auth token
            val exchangeResult = apiService.exchangeGoogleToken(serverUrl, idToken)
            if (exchangeResult.isFailure) {
                val msg = exchangeResult.exceptionOrNull()?.message ?: "Token exchange failed"
                _uiState.update {
                    it.copy(isGoogleSignInLoading = false, snackbarMessage = msg)
                }
                return@launch
            }

            val appToken = exchangeResult.getOrThrow()

            // Step 3: Save token and update UI
            preferencesManager.setAuthToken(appToken)
            _uiState.update {
                it.copy(
                    authToken = appToken,
                    isGoogleSignInLoading = false,
                    googleSignInEmail = email,
                    snackbarMessage = if (email != null) "Signed in as $email" else "Signed in successfully",
                    connectionStatus = ConnectionStatus.Unknown,
                )
            }

            // Step 4: Auto-test connection with the new token
            onTestConnection()
        }
    }

    /**
     * Decode the JWT payload (middle segment) and extract the `email` claim.
     * No signature verification needed — the server already validates the token.
     */
    private fun extractEmailFromJwt(idToken: String): String? {
        return try {
            val payload = idToken.split(".").getOrNull(1) ?: return null
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decoded = android.util.Base64.decode(padded, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val json = String(decoded, Charsets.UTF_8)
            Regex(""""email"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.getOrNull(1)
        } catch (_: Exception) {
            null
        }
    }

    // --- Export ---

    fun onExportJson(outputUri: Uri) {
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            try {
                val notes = noteDao.getAllNotes()
                val json = NoteExportHelper.toJson(notes)
                withContext(Dispatchers.IO) {
                    application.contentResolver.openOutputStream(outputUri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
                _uiState.update { it.copy(isExporting = false, snackbarMessage = "Exported ${notes.size} notes as JSON") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, snackbarMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun onExportCsv(outputUri: Uri) {
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            try {
                val notes = noteDao.getAllNotes()
                val csv = NoteExportHelper.toCsv(notes)
                withContext(Dispatchers.IO) {
                    application.contentResolver.openOutputStream(outputUri)?.use { stream ->
                        stream.write(csv.toByteArray(Charsets.UTF_8))
                    }
                }
                _uiState.update { it.copy(isExporting = false, snackbarMessage = "Exported ${notes.size} notes as CSV") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, snackbarMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
