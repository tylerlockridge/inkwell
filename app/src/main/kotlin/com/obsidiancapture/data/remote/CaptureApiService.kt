package com.obsidiancapture.data.remote

import com.obsidiancapture.data.remote.dto.CaptureDefaultsResponse
import com.obsidiancapture.data.remote.dto.CaptureRequest
import com.obsidiancapture.data.remote.dto.CaptureResponse
import com.obsidiancapture.data.remote.dto.DeviceRegisterRequest
import com.obsidiancapture.data.remote.dto.DeviceRegisterResponse
import com.obsidiancapture.data.remote.dto.DeviceRemoveResponse
import com.obsidiancapture.data.remote.dto.InboxResponse
import com.obsidiancapture.data.remote.dto.NoteDetailResponse
import com.obsidiancapture.data.remote.dto.NoteUpdateRequest
import com.obsidiancapture.data.remote.dto.NoteUpdateResponse
import com.obsidiancapture.data.remote.dto.GoogleAuthRequest
import com.obsidiancapture.data.remote.dto.GoogleAuthResponse
import com.obsidiancapture.data.remote.dto.SyncthingStatusResponse
import com.obsidiancapture.data.remote.dto.SyncthingRestartResponse
import com.obsidiancapture.data.remote.dto.SystemStatusResponse
import com.obsidiancapture.di.UnauthenticatedClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureApiService @Inject constructor(
    private val httpClient: HttpClient,
    @UnauthenticatedClient private val unauthenticatedClient: HttpClient,
) {
    suspend fun capture(baseUrl: String, request: CaptureRequest): CaptureResponse {
        return httpClient.post("$baseUrl/api/capture") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getInbox(
        baseUrl: String,
        status: String? = null,
        since: String? = null,
        limit: Int? = null,
        offset: Int? = null,
    ): InboxResponse {
        return httpClient.get("$baseUrl/api/inbox") {
            status?.let { parameter("status", it) }
            since?.let { parameter("since", it) }
            limit?.let { parameter("limit", it) }
            offset?.let { parameter("offset", it) }
        }.body()
    }

    suspend fun getNote(baseUrl: String, uid: String): NoteDetailResponse {
        return httpClient.get("$baseUrl/api/note/$uid").body()
    }

    suspend fun updateNote(
        baseUrl: String,
        uid: String,
        request: NoteUpdateRequest,
    ): NoteUpdateResponse {
        return httpClient.patch("$baseUrl/api/note/$uid") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getCaptureDefaults(baseUrl: String): CaptureDefaultsResponse? {
        return try {
            httpClient.get("$baseUrl/api/capture/defaults").body()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun healthCheck(baseUrl: String): Boolean {
        return try {
            val response = httpClient.get("$baseUrl/healthz")
            response.status.value == 200
        } catch (_: Exception) {
            false
        }
    }

    suspend fun registerDevice(
        baseUrl: String,
        request: DeviceRegisterRequest,
    ): DeviceRegisterResponse {
        return httpClient.post("$baseUrl/api/device/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun removeDevice(baseUrl: String, deviceId: String): DeviceRemoveResponse {
        return httpClient.delete("$baseUrl/api/device/$deviceId").body()
    }

    suspend fun getSystemStatus(baseUrl: String): SystemStatusResponse? {
        return try {
            httpClient.get("$baseUrl/api/status").body()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getSyncthingStatus(baseUrl: String): SyncthingStatusResponse? {
        return try {
            httpClient.get("$baseUrl/api/syncthing/status").body()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun restartSyncthing(baseUrl: String): SyncthingRestartResponse? {
        return try {
            httpClient.post("$baseUrl/api/syncthing/restart").body()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun exchangeGoogleToken(baseUrl: String, idToken: String): Result<String> {
        return try {
            val httpResponse = unauthenticatedClient.post("$baseUrl/api/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(GoogleAuthRequest(idToken))
            }
            val bodyText = httpResponse.bodyAsText()
            android.util.Log.d("GoogleSignIn", "Exchange response ${httpResponse.status}: $bodyText")

            if (httpResponse.status.value !in 200..299) {
                return Result.failure(Exception("Server error ${httpResponse.status}: $bodyText"))
            }

            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val response = json.decodeFromString<GoogleAuthResponse>(bodyText)
            Result.success(response.token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
