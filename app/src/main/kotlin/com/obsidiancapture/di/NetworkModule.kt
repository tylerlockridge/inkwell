package com.obsidiancapture.di

import com.obsidiancapture.data.local.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val REQUEST_TIMEOUT_MS = 30_000L
    private const val SOCKET_TIMEOUT_MS = 30_000L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json, preferencesManager: PreferencesManager): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = preferencesManager.authToken.first()
                        if (token.isNotBlank()) BearerTokens(token, "") else null
                    }
                    refreshTokens {
                        // Re-read token from prefs — it may have been updated by
                        // a Google Sign-In re-auth flow in the UI
                        val token = preferencesManager.authToken.first()
                        if (token.isNotBlank()) BearerTokens(token, "") else null
                    }
                    // sendWithoutRequest deliberately omitted: without it, Ktor only
                    // sends the bearer token after a 401 challenge from the server.
                    // This prevents proactive token transmission to unintended hosts
                    // (e.g., third-party URLs, FCM, image CDNs) at the cost of one
                    // extra round-trip on the first request to each endpoint.
                }
            }
        }
    }

    @Provides
    @Singleton
    @UnauthenticatedClient
    fun provideUnauthenticatedHttpClient(json: Json): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
        }
    }
}
