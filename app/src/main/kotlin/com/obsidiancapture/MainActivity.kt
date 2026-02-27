package com.obsidiancapture

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.obsidiancapture.auth.BiometricAuthManager
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.repository.InboxRepository
import com.obsidiancapture.share.ShareIntentParser
import com.obsidiancapture.ui.auth.LockScreen
import com.obsidiancapture.ui.navigation.CaptureNavHost
import com.obsidiancapture.ui.navigation.DeepLink
import com.obsidiancapture.ui.theme.CaptureTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var inboxRepository: InboxRepository

    private val biometricAuthManager = BiometricAuthManager()
    private var isLocked by mutableStateOf(false)
    private var lastPausedAt: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val biometricEnabled = preferencesManager.biometricEnabled.first()
            if (biometricEnabled) {
                val capability = biometricAuthManager.checkCapability(this@MainActivity)
                if (capability == BiometricAuthManager.Capability.AVAILABLE) {
                    isLocked = true
                }
            }
        }

        // Sync inbox on every app open regardless of which tab is active
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                inboxRepository.syncInbox()
            }
        }

        renderContent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        renderContent(intent)
    }

    override fun onPause() {
        super.onPause()
        lastPausedAt = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        // Re-lock after 60s in background
        if (lastPausedAt > 0L) {
            val elapsed = System.currentTimeMillis() - lastPausedAt
            if (elapsed > LOCK_TIMEOUT_MS) {
                lifecycleScope.launch {
                    val biometricEnabled = preferencesManager.biometricEnabled.first()
                    if (biometricEnabled) {
                        val capability = biometricAuthManager.checkCapability(this@MainActivity)
                        if (capability == BiometricAuthManager.Capability.AVAILABLE) {
                            isLocked = true
                        }
                    }
                }
            }
        }
    }

    private fun renderContent(intent: Intent?) {
        val initialRoute = DeepLink.parseToRoute(intent?.data)
        val shareData = ShareIntentParser.parse(intent)

        setContent {
            CaptureTheme {
                if (isLocked) {
                    LockScreen(
                        onUnlockClick = {
                            lifecycleScope.launch {
                                val success = biometricAuthManager.authenticate(this@MainActivity)
                                if (success) {
                                    isLocked = false
                                }
                            }
                        },
                    )
                } else {
                    CaptureNavHost(
                        initialRoute = initialRoute,
                        sharedText = shareData?.text,
                        sharedTitle = shareData?.title,
                    )
                }
            }
        }
    }

    companion object {
        private const val LOCK_TIMEOUT_MS = 60_000L
    }
}
