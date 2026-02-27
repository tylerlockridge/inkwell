package com.obsidiancapture.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsidiancapture.data.remote.dto.SyncthingDevice
import com.obsidiancapture.data.remote.dto.SyncthingFolder
import com.obsidiancapture.data.remote.dto.SyncthingStatusResponse
import com.obsidiancapture.data.remote.dto.SystemStatusResponse
import com.obsidiancapture.data.repository.CaptureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SystemHealthUiState(
    val isLoading: Boolean = true,
    val isRestarting: Boolean = false,
    val serverStatus: String = "unknown",
    val version: String = "",
    val uptimeSeconds: Long = 0,
    val memoryUsageMB: Int = 0,
    val vaultAccessible: Boolean = false,
    val diskUsagePercent: Int? = null,
    val inboxTotal: Int = 0,
    val inboxToday: Int = 0,
    val reviewPending: Int = 0,
    val gcalLastSync: String? = null,
    val gcalErrors: Int = 0,
    val syncthingReachable: Boolean = false,
    val syncthingEnabled: Boolean = false,
    val syncthingVersion: String = "",
    val syncthingUptime: Long = 0,
    val syncthingDevices: List<SyncthingDevice> = emptyList(),
    val syncthingFolders: List<SyncthingFolder> = emptyList(),
    val snackbarMessage: String? = null,
)

@HiltViewModel
class SystemHealthViewModel @Inject constructor(
    private val repository: CaptureRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemHealthUiState())
    val uiState: StateFlow<SystemHealthUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val status = repository.getSystemStatus()
            if (status != null) {
                applyStatus(status)
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun restartSyncthing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestarting = true) }
            val result = repository.restartSyncthing()
            if (result?.restarted == true) {
                _uiState.update { it.copy(isRestarting = false, snackbarMessage = "Syncthing restart triggered") }
            } else {
                _uiState.update { it.copy(isRestarting = false, snackbarMessage = "Restart failed") }
            }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun applyStatus(status: SystemStatusResponse) {
        val syncthing = status.syncthing
        _uiState.update {
            it.copy(
                serverStatus = status.status,
                version = status.version,
                uptimeSeconds = status.uptimeSeconds,
                memoryUsageMB = status.memoryUsageMB,
                vaultAccessible = status.vaultAccessible,
                diskUsagePercent = status.diskUsagePercent,
                inboxTotal = status.summary?.inbox?.total ?: 0,
                inboxToday = status.summary?.inbox?.today ?: 0,
                reviewPending = status.summary?.review?.pending ?: 0,
                gcalLastSync = status.summary?.gcal?.lastSync,
                gcalErrors = status.summary?.gcal?.errors ?: 0,
                syncthingEnabled = syncthing != null,
                syncthingReachable = syncthing?.reachable ?: false,
                syncthingVersion = syncthing?.system?.version ?: "",
                syncthingUptime = syncthing?.system?.uptime ?: 0,
                syncthingDevices = syncthing?.devices ?: emptyList(),
                syncthingFolders = syncthing?.folders ?: emptyList(),
            )
        }
    }
}
