package com.obsidiancapture.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttachmentPickerState(
    val onResults: (List<Uri>) -> Unit,
    private val galleryLauncher: () -> Unit,
    private val cameraLauncherWithSetup: (Context) -> Unit,
    private val documentsLauncher: () -> Unit,
) {
    fun launchGallery() = galleryLauncher()
    fun launchCamera(context: Context) = cameraLauncherWithSetup(context)
    fun launchDocuments() = documentsLauncher()
}

@Composable
fun rememberAttachmentPickerState(onResults: (List<Uri>) -> Unit): AttachmentPickerState {
    // Use MutableState so the URI survives recompositions and is shared between
    // the setup lambda (inside remember{}) and the result callback.
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    // Holds the context for the pending camera launch after permission is granted
    val pendingCameraContext = remember { mutableStateOf<Context?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) onResults(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraImageUri.value?.let { onResults(listOf(it)) }
        }
    }

    val documentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) onResults(uris)
    }

    // Helper to create temp file + launch the camera TakePicture contract
    fun doLaunchCamera(context: Context) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile("IMG_${timestamp}_", ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
        cameraImageUri.value = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            pendingCameraContext.value?.let { doLaunchCamera(it) }
        }
        pendingCameraContext.value = null
    }

    return remember {
        AttachmentPickerState(
            onResults = onResults,
            galleryLauncher = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                )
            },
            cameraLauncherWithSetup = { context ->
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    doLaunchCamera(context)
                } else {
                    pendingCameraContext.value = context
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            documentsLauncher = {
                documentsLauncher.launch(
                    arrayOf(
                        "image/*",
                        "application/pdf",
                        "text/*",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.*",
                    ),
                )
            },
        )
    }
}
