package io.inkwell

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import io.inkwell.notifications.DeviceRegistrationManager
import io.inkwell.notifications.NotificationChannels
import io.inkwell.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CaptureApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var deviceRegistrationManager: DeviceRegistrationManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        appScope.launch {
            syncScheduler.schedulePeriodicSync()
            deviceRegistrationManager.ensureDeviceId()
        }
    }
}
