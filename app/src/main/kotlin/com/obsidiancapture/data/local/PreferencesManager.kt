package com.obsidiancapture.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.obsidiancapture.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val serverUrlKey = stringPreferencesKey("server_url")
    private val authTokenKey = stringPreferencesKey("auth_token")
    private val syncIntervalKey = longPreferencesKey("sync_interval_minutes")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")
    private val fcmTokenKey = stringPreferencesKey("fcm_token")
    private val deviceIdKey = stringPreferencesKey("device_id")
    private val hapticsEnabledKey = booleanPreferencesKey("haptics_enabled")
    private val lastSyncedAtKey = stringPreferencesKey("last_synced_at")
    private val lastCalendarKey = stringPreferencesKey("last_calendar")
    private val lastKindKey = stringPreferencesKey("last_kind")
    private val lastPriorityKey = stringPreferencesKey("last_priority")

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        try {
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (_: Exception) {
            // Keyset corrupted (e.g. after reinstall / Keystore invalidation) — wipe and recreate
            context.deleteSharedPreferences(ENCRYPTED_PREFS_NAME)
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[serverUrlKey] ?: DEFAULT_SERVER_URL
    }

    val authToken: Flow<String> = flow {
        // Migrate from plaintext DataStore if needed (one-time)
        val dataStoreToken = context.dataStore.data.first()[authTokenKey]
        if (!dataStoreToken.isNullOrBlank()) {
            encryptedPrefs.edit().putString(ENCRYPTED_AUTH_TOKEN_KEY, dataStoreToken).apply()
            context.dataStore.edit { prefs -> prefs.remove(authTokenKey) }
        }
        val stored = encryptedPrefs.getString(ENCRYPTED_AUTH_TOKEN_KEY, "") ?: ""
        // Fall back to baked-in token for fresh installs
        emit(stored.ifBlank { BuildConfig.DEFAULT_AUTH_TOKEN })
    }

    val syncIntervalMinutes: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[syncIntervalKey] ?: DEFAULT_SYNC_INTERVAL
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[notificationsEnabledKey] ?: true
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[biometricEnabledKey] ?: false
    }

    val fcmToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[fcmTokenKey] ?: ""
    }

    val deviceId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[deviceIdKey] ?: ""
    }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[hapticsEnabledKey] ?: true
    }

    val lastSyncedAt: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[lastSyncedAtKey] ?: ""
    }

    val lastCalendar: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[lastCalendarKey]
    }

    val lastKind: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[lastKindKey] ?: "one_shot"
    }

    val lastPriority: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[lastPriorityKey]
    }

    suspend fun setServerUrl(url: String) {
        val normalized = url.trimEnd('/')
        require(isValidServerUrl(normalized)) {
            "Server URL must use HTTPS (or localhost/10.0.2.2 for development)"
        }
        context.dataStore.edit { prefs -> prefs[serverUrlKey] = normalized }
    }

    suspend fun setAuthToken(token: String) {
        encryptedPrefs.edit().putString(ENCRYPTED_AUTH_TOKEN_KEY, token).apply()
        // Ensure no plaintext copy remains in DataStore
        context.dataStore.edit { prefs -> prefs.remove(authTokenKey) }
    }

    suspend fun setSyncIntervalMinutes(minutes: Long) {
        context.dataStore.edit { prefs -> prefs[syncIntervalKey] = minutes }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[notificationsEnabledKey] = enabled }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[biometricEnabledKey] = enabled }
    }

    suspend fun setFcmToken(token: String) {
        context.dataStore.edit { prefs -> prefs[fcmTokenKey] = token }
    }

    suspend fun setDeviceId(id: String) {
        context.dataStore.edit { prefs -> prefs[deviceIdKey] = id }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[hapticsEnabledKey] = enabled }
    }

    suspend fun setLastSyncedAt(timestamp: String) {
        context.dataStore.edit { prefs -> prefs[lastSyncedAtKey] = timestamp }
    }

    suspend fun setLastCalendar(calendar: String?) {
        context.dataStore.edit { prefs ->
            if (calendar != null) {
                prefs[lastCalendarKey] = calendar
            } else {
                prefs.remove(lastCalendarKey)
            }
        }
    }

    suspend fun setLastKind(kind: String) {
        context.dataStore.edit { prefs -> prefs[lastKindKey] = kind }
    }

    suspend fun setLastPriority(priority: String?) {
        context.dataStore.edit { prefs ->
            if (priority != null) {
                prefs[lastPriorityKey] = priority
            } else {
                prefs.remove(lastPriorityKey)
            }
        }
    }

    companion object {
        const val DEFAULT_SERVER_URL = "https://tyler-capture.duckdns.org"
        const val DEFAULT_SYNC_INTERVAL = 15L
        private const val ENCRYPTED_PREFS_NAME = "secure_prefs"
        private const val ENCRYPTED_AUTH_TOKEN_KEY = "auth_token"

        /** Allowed URL schemes: HTTPS required, except localhost/emulator for dev */
        fun isValidServerUrl(url: String): Boolean {
            if (url.isBlank()) return true // empty is allowed (means "not configured")
            val lower = url.lowercase()
            if (lower.startsWith("https://")) return true
            // Allow plaintext only for local development
            if (lower.startsWith("http://localhost") || lower.startsWith("http://10.0.2.2")) return true
            return false
        }
    }
}
