package com.obsidiancapture

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner that replaces the app's Application with Hilt's test application,
 * enabling @HiltAndroidTest to inject fakes/stubs into instrumented tests.
 *
 * Also initializes WorkManager manually because:
 * - The manifest disables WorkManagerInitializer (for on-demand init in production)
 * - HiltTestApplication doesn't implement Configuration.Provider
 * - SyncScheduler calls WorkManager.getInstance() at construction time
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun callApplicationOnCreate(app: Application) {
        WorkManager.initialize(app, Configuration.Builder().build())
        super.callApplicationOnCreate(app)
    }
}
