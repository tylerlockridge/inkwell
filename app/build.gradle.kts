import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.obsidiancapture"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.obsidiancapture"
        minSdk = 26
        targetSdk = 35
        versionCode = 9
        versionName = "2.1.2"

        testInstrumentationRunner = "com.obsidiancapture.HiltTestRunner"

        // DEFAULT_AUTH_TOKEN removed — baking tokens into the APK is a security risk.
        // Auth token is configured by the user at runtime via Settings → Sign In.
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: findProperty("KEYSTORE_PATH")?.toString()
            val ksPassword = System.getenv("KEYSTORE_PASSWORD") ?: findProperty("KEYSTORE_PASSWORD")?.toString()
            val keyAlias = System.getenv("KEY_ALIAS") ?: findProperty("KEY_ALIAS")?.toString() ?: "release"
            val keyPwd = System.getenv("KEY_PASSWORD") ?: findProperty("KEY_PASSWORD")?.toString()

            if (keystorePath != null && ksPassword != null && keyPwd != null) {
                val ksFile = file(keystorePath)
                if (!ksFile.exists()) {
                    logger.warn("Release keystore not found at: $keystorePath — release build will be unsigned")
                } else {
                    storeFile = ksFile
                    storePassword = ksPassword
                    this.keyAlias = keyAlias
                    keyPassword = keyPwd
                }
            } else {
                logger.warn("Release signing config incomplete — set KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.findByName("release")
            if (releaseConfig?.storeFile != null) {
                signingConfig = releaseConfig
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    debugImplementation(libs.compose.ui.tooling)

    // Core
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.auth)

    // Kotlin
    implementation(libs.serialization.json)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.workmanager)

    // DataStore
    implementation(libs.datastore.preferences)

    // UI extras
    implementation(libs.lottie.compose)
    implementation(libs.biometric)

    // Glance (widgets)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Security
    implementation(libs.security.crypto)

    // Google Sign-In (Credential Manager)
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.compose.test.junit4)
    androidTestImplementation(libs.hilt.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)  // 3.6.2 fixes Android 16 InputManager.getInstance removal
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.compose.test.manifest)
}
