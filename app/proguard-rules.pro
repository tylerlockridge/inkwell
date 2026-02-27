# Ktor
-keep class io.ktor.** { *; }
-keep class io.ktor.client.engine.okhttp.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.obsidiancapture.**$$serializer { *; }
-keepclassmembers class com.obsidiancapture.** {
    *** Companion;
}
-keepclasseswithmembers class com.obsidiancapture.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class com.obsidiancapture.data.local.entity.** { *; }
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# WorkManager + Hilt Worker
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# Google Identity (Credential Manager)
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# DataStore Preferences
-keep class androidx.datastore.preferences.** { *; }

# App DTO classes (kotlinx.serialization)
-keep class com.obsidiancapture.data.remote.dto.** { *; }

# Encrypted SharedPreferences (security-crypto)
-keep class androidx.security.crypto.** { *; }
