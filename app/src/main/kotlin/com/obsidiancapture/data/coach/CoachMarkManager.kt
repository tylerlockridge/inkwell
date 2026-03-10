package com.obsidiancapture.data.coach

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.coachMarkDataStore by preferencesDataStore(name = "coach_marks")

enum class CoachMarkKey {
    TYPE_TOGGLE,
    TOOLBAR_EXPAND,
    IDEA_TYPE,
    ATTACHMENT_BUTTON
}

@Singleton
class CoachMarkManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isDismissed(key: CoachMarkKey): Flow<Boolean> =
        context.coachMarkDataStore.data.map { prefs ->
            prefs[booleanPreferencesKey(key.name)] ?: false
        }

    suspend fun dismiss(key: CoachMarkKey) {
        context.coachMarkDataStore.edit { prefs ->
            prefs[booleanPreferencesKey(key.name)] = true
        }
    }

    suspend fun resetAll() {
        context.coachMarkDataStore.edit { it.clear() }
    }
}
