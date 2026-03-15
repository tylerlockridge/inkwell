package io.inkwell

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.inkwell.auth.BiometricAuthManager
import io.inkwell.data.local.PreferencesManager
import io.inkwell.share.ShareIntentParser
import io.inkwell.ui.auth.LockScreen
import io.inkwell.ui.navigation.CaptureNavHost
import io.inkwell.ui.navigation.DeepLink
import io.inkwell.ui.theme.CaptureTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val viewModel: MainViewModel by viewModels()
    private val biometricAuthManager = BiometricAuthManager()
    private var lastPausedAt: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val biometricEnabled = preferencesManager.biometricEnabled.first()
            if (biometricEnabled) {
                val capability = biometricAuthManager.checkCapability(this@MainActivity)
                if (capability == BiometricAuthManager.Capability.AVAILABLE) {
                    viewModel.lock()
                }
            }
        }

        // Sync inbox on every app open regardless of which tab is active
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.triggerStartupSync()
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
                            viewModel.lock()
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
                val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
                if (isLocked) {
                    LockScreen(
                        onUnlockClick = {
                            lifecycleScope.launch {
                                val success = biometricAuthManager.authenticate(this@MainActivity)
                                if (success) {
                                    viewModel.unlock()
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
