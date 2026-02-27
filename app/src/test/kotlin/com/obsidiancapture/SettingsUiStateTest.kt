package com.obsidiancapture

import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.ui.settings.ConnectionStatus
import com.obsidiancapture.ui.settings.SYNC_INTERVAL_OPTIONS
import com.obsidiancapture.ui.settings.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUiStateTest {

    @Test
    fun `default state has pre-filled server url`() {
        val state = SettingsUiState()
        assertEquals(PreferencesManager.DEFAULT_SERVER_URL, state.serverUrl)
    }

    @Test
    fun `default state has empty auth token`() {
        val state = SettingsUiState()
        assertEquals("", state.authToken)
    }

    @Test
    fun `default sync interval is 15 minutes`() {
        val state = SettingsUiState()
        assertEquals(15L, state.syncIntervalMinutes)
    }

    @Test
    fun `notifications enabled by default`() {
        val state = SettingsUiState()
        assertTrue(state.notificationsEnabled)
    }

    @Test
    fun `biometric disabled by default`() {
        val state = SettingsUiState()
        assertFalse(state.biometricEnabled)
    }

    @Test
    fun `connection status is unknown by default`() {
        val state = SettingsUiState()
        assertEquals(ConnectionStatus.Unknown, state.connectionStatus)
    }

    @Test
    fun `not testing connection by default`() {
        val state = SettingsUiState()
        assertFalse(state.isTestingConnection)
    }

    @Test
    fun `no snackbar message by default`() {
        val state = SettingsUiState()
        assertNull(state.snackbarMessage)
    }

    @Test
    fun `sync interval options has four entries`() {
        assertEquals(4, SYNC_INTERVAL_OPTIONS.size)
    }

    @Test
    fun `sync interval options includes 15 and 60`() {
        val values = SYNC_INTERVAL_OPTIONS.map { it.first }
        assertTrue(values.contains(15L))
        assertTrue(values.contains(60L))
    }

    @Test
    fun `connection status enum covers all states`() {
        val statuses = ConnectionStatus.entries
        assertEquals(4, statuses.size)
        assertTrue(statuses.contains(ConnectionStatus.Unknown))
        assertTrue(statuses.contains(ConnectionStatus.Connected))
        assertTrue(statuses.contains(ConnectionStatus.Failed))
        assertTrue(statuses.contains(ConnectionStatus.Testing))
    }
}
