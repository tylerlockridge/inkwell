package io.inkwell.data.repository

import android.content.Context
import android.util.Log
import io.inkwell.BuildConfig
import io.inkwell.data.local.PreferencesManager
import io.inkwell.data.local.dao.NoteDao
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.remote.CaptureApiService
import io.inkwell.data.remote.dto.CaptureDefaultsResponse
import io.inkwell.data.remote.dto.CaptureRequest
import io.inkwell.data.remote.dto.CaptureResponse
import io.inkwell.data.remote.dto.SyncthingStatusResponse
import io.inkwell.data.remote.dto.SyncthingRestartResponse
import io.inkwell.data.remote.dto.SystemStatusResponse
import io.inkwell.widget.WidgetStateUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class CaptureResult {
    data class Online(val response: CaptureResponse) : CaptureResult()
    data class Offline(val clientUuid: String) : CaptureResult()
    data class Error(val message: String) : CaptureResult()
}

@Singleton
class CaptureRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: CaptureApiService,
    private val noteDao: NoteDao,
    private val preferencesManager: PreferencesManager,
) {
    val pendingSyncCount: Flow<Int> = noteDao.getPendingSyncCount()

    suspend fun capture(
        body: String,
        title: String?,
        tags: List<String>?,
        kind: String?,
        date: String?,
        startTime: String?,
        endTime: String?,
        calendar: String?,
        priority: String?,
        captureType: String? = null,
        listName: String? = null,
        items: List<String>? = null,
        persistent: Boolean? = null,
        attachmentUris: List<String>? = null,
    ): CaptureResult {
        val clientUuid = UUID.randomUUID().toString()
        val now = Instant.now().toString()

        val attachmentUrisJson = attachmentUris?.takeIf { it.isNotEmpty() }
            ?.let { Json.encodeToString(ListSerializer(String.serializer()), it) }

        val request = CaptureRequest(
            body = body,
            title = title,
            tags = tags,
            kind = kind,
            date = date,
            startTime = startTime,
            endTime = endTime,
            calendar = calendar,
            priority = priority,
            source = "android",
            uuid = clientUuid,
            captureType = captureType,
            listName = listName,
            items = items,
            persistent = persistent,
        )

        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) {
            return saveOffline(request, clientUuid, now, attachmentUrisJson)
        }

        return try {
            val response = if (attachmentUris?.isNotEmpty() == true) {
                apiService.captureWithAttachments(
                    serverUrl, request, attachmentUris,
                    context.contentResolver,
                )
            } else {
                apiService.capture(serverUrl, request)
            }
            // Save locally with synced state
            noteDao.upsert(
                NoteEntity(
                    uid = response.uid,
                    title = title ?: "",
                    body = body,
                    kind = kind ?: "one_shot",
                    tags = NoteEntity.tagsToJson(tags),
                    priority = priority,
                    calendar = calendar,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    source = "android",
                    created = now,
                    updated = now,
                    syncedAt = now,
                    pendingSync = false,
                    clientUuid = clientUuid,
                    attachmentUris = attachmentUrisJson,
                )
            )
            updateWidgets()
            CaptureResult.Online(response)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (BuildConfig.DEBUG) Log.i(TAG, "Network unavailable, saving offline: ${e.javaClass.simpleName}")
            saveOffline(request, clientUuid, now, attachmentUrisJson)
        }
    }

    private suspend fun saveOffline(
        request: CaptureRequest,
        clientUuid: String,
        now: String,
        attachmentUrisJson: String? = null,
    ): CaptureResult {
        val tempUid = "pending_$clientUuid"
        noteDao.upsert(
            NoteEntity(
                uid = tempUid,
                title = request.title ?: "",
                body = request.body,
                kind = request.kind ?: "one_shot",
                tags = NoteEntity.tagsToJson(request.tags),
                priority = request.priority,
                calendar = request.calendar,
                date = request.date,
                startTime = request.startTime,
                endTime = request.endTime,
                source = "android",
                created = now,
                updated = now,
                pendingSync = true,
                clientUuid = clientUuid,
                attachmentUris = attachmentUrisJson,
            )
        )
        updateWidgets()
        return CaptureResult.Offline(clientUuid)
    }

    private suspend fun updateWidgets() {
        try {
            val inboxCount = noteDao.getInboxNotesCount().first()
            val pendingSyncCount = noteDao.getPendingSyncCount().first()
            WidgetStateUpdater.updateCounts(context, inboxCount, pendingSyncCount)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Widget update failed", e)
        }
    }

    suspend fun getCaptureDefaults(): CaptureDefaultsResponse? {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return null
        return try {
            apiService.getCaptureDefaults(serverUrl)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch capture defaults", e)
            null
        }
    }

    suspend fun getSystemStatus(): SystemStatusResponse? {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return null
        return try {
            apiService.getSystemStatus(serverUrl)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch system status", e)
            null
        }
    }

    suspend fun getSyncthingStatus(): SyncthingStatusResponse? {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return null
        return try {
            apiService.getSyncthingStatus(serverUrl)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch Syncthing status", e)
            null
        }
    }

    suspend fun restartSyncthing(): SyncthingRestartResponse? {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return null
        return try {
            apiService.restartSyncthing(serverUrl)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (BuildConfig.DEBUG) Log.w(TAG, "Failed to restart Syncthing", e)
            null
        }
    }

    suspend fun getPendingNotes(): List<NoteEntity> = noteDao.getPendingSync()

    suspend fun markSynced(uid: String) {
        noteDao.markSynced(uid, Instant.now().toString())
    }

    companion object {
        private const val TAG = "CaptureRepository"
    }
}
