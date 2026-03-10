package com.obsidiancapture.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun AttachmentPreview(
    attachments: List<Uri>,
    onRemove: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier) {
        items(attachments) { uri ->
            AttachmentChip(
                uri = uri,
                onRemove = { onRemove(uri) },
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun AttachmentChip(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mimeType = uri.path?.let { path ->
        when {
            path.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            path.endsWith(".doc", ignoreCase = true) -> "application/msword"
            path.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats"
            path.endsWith(".txt", ignoreCase = true) -> "text/plain"
            else -> null
        }
    }
    val isImage = mimeType == null || mimeType.startsWith("image")

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(72.dp),
        ) {
            if (isImage) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove attachment",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
