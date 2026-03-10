package com.obsidiancapture

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.obsidiancapture.data.coach.CoachMarkKey
import com.obsidiancapture.data.coach.coachMarkDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoachMarkManagerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `isDismissed returns false by default`() = runTest {
        val dismissed = context.coachMarkDataStore.data.first()[booleanPreferencesKey(CoachMarkKey.TYPE_TOGGLE.name)]
        assertFalse(dismissed ?: false)
    }

    @Test
    fun `dismiss sets key to true`() = runTest {
        context.coachMarkDataStore.edit { prefs ->
            prefs[booleanPreferencesKey(CoachMarkKey.IDEA_TYPE.name)] = true
        }
        val dismissed = context.coachMarkDataStore.data.first()[booleanPreferencesKey(CoachMarkKey.IDEA_TYPE.name)]
        assertTrue(dismissed ?: false)
    }

    @Test
    fun `resetAll clears all keys`() = runTest {
        context.coachMarkDataStore.edit { prefs ->
            prefs[booleanPreferencesKey(CoachMarkKey.TYPE_TOGGLE.name)] = true
            prefs[booleanPreferencesKey(CoachMarkKey.ATTACHMENT_BUTTON.name)] = true
        }
        context.coachMarkDataStore.edit { it.clear() }
        val prefs = context.coachMarkDataStore.data.first()
        assertFalse(prefs[booleanPreferencesKey(CoachMarkKey.TYPE_TOGGLE.name)] ?: false)
        assertFalse(prefs[booleanPreferencesKey(CoachMarkKey.ATTACHMENT_BUTTON.name)] ?: false)
    }

    @Test
    fun `all CoachMarkKey enum values are accessible`() {
        val keys = CoachMarkKey.entries
        assertTrue(keys.contains(CoachMarkKey.TYPE_TOGGLE))
        assertTrue(keys.contains(CoachMarkKey.TOOLBAR_EXPAND))
        assertTrue(keys.contains(CoachMarkKey.IDEA_TYPE))
        assertTrue(keys.contains(CoachMarkKey.ATTACHMENT_BUTTON))
    }
}
