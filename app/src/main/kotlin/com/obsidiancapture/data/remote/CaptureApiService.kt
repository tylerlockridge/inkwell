package com.obsidiancapture.data.remote

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
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
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.timeout
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import com.obsidiancapture.BuildConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureApiService @Inject constructor(
    private val httpClient: HttpClient,
    @UnauthenticatedClient private val unauthenticatedClient: HttpClient,
    private val json: Json,
) {
    suspend fun capture(baseUrl: String, request: CaptureRequest): CaptureResponse {
        return httpClient.post("$baseUrl/api/capture") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Upload a capture with file attachments as multipart/form-data.
     * The server's capture endpoint accepts both JSON and multipart — this path is used
     * when the note has one or more local content URIs to attach.
     */
    suspend fun captureWithAttachments(
        baseUrl: String,
        request: CaptureRequest,
        attachmentUris: List<String>,
        contentResolver: ContentResolver,
    ): CaptureResponse {
        return httpClient.submitFormWithBinaryData(
            url = "$baseUrl/api/capture",
            formData = formData {
                append("body", request.body)
                request.title?.let { append("title", it) }
                request.tags?.takeIf { it.isNotEmpty() }?.let { append("tags", it.joinToString(",")) }
                request.kind?.let { append("kind", it) }
                request.date?.let { append("date", it) }
                request.priority?.let { append("priority", it) }
                append("source", request.source)
                request.uuid?.let { append("uuid", it) }
                request.captureType?.let { append("captureType", it) }

                for (uriString in attachmentUris) {
                    val uri = Uri.parse(uriString) ?: continue
                    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                    val rawFilename = resolveFilename(uri, contentResolver)
                    val filename = sanitizeFilename(rawFilename)
                    val fileSize = resolveFileSize(uri, contentResolver)
                    val headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    }
                    if (fileSize != null && fileSize > 0) {
                        // Stream the file content without loading it entirely into memory
                        append(filename, ChannelProvider(fileSize) {
                            contentResolver.openInputStream(uri)!!.toByteReadChannel()
                        }, headers)
                    } else {
                        // Fallback: file size unknown, must read into memory
                        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: continue
                        append(filename, bytes, headers)
                    }
                }
            },
        ) {
            timeout { requestTimeoutMillis = 120_000L; socketTimeoutMillis = 120_000L }
        }.body()
    }

    private fun resolveFilename(uri: Uri, contentResolver: ContentResolver): String {
        return contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: uri.lastPathSegment ?: "attachment"
    }

    /**
     * Resolve the file size in bytes for a content URI.
     * Tries OpenableColumns.SIZE first (works for most content providers),
     * then falls back to AssetFileDescriptor. Returns null if size cannot be determined.
     */
    private fun resolveFileSize(uri: Uri, contentResolver: ContentResolver): Long? {
        // Try cursor query first — most reliable for content:// URIs
        val cursorSize = contentResolver.query(
            uri, arrayOf(OpenableColumns.SIZE), null, null, null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
            } else null
        }
        if (cursorSize != null && cursorSize > 0) return cursorSize

        // Fallback to AssetFileDescriptor
        return try {
            contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length.takeIf { len -> len >= 0 } }
        } catch (_: Exception) {
            null
        }
    }

    /** Strip characters that could cause header injection in Content-Disposition */
    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\"\\r\\n\\\\]"), "_")
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

    suspend fun getDeletedInbox(baseUrl: String, since: String? = null): InboxResponse {
        return httpClient.get("$baseUrl/api/inbox/deleted") {
            since?.let { parameter("since", it) }
        }.body()
    }

    suspend fun exchangeGoogleToken(baseUrl: String, idToken: String): Result<String> {
        return try {
            val httpResponse = unauthenticatedClient.post("$baseUrl/api/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(GoogleAuthRequest(idToken))
            }
            val bodyText = httpResponse.bodyAsText()
            // Only log in debug builds — response body contains the JWT token
            if (BuildConfig.DEBUG) {
                android.util.Log.d("GoogleSignIn", "Exchange response: ${httpResponse.status.value}")
            }

            if (httpResponse.status.value !in 200..299) {
                return Result.failure(Exception("Server error ${httpResponse.status}: $bodyText"))
            }

            val response = json.decodeFromString<GoogleAuthResponse>(bodyText)
            Result.success(response.token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
