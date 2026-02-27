package com.obsidiancapture.ui.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.local.entity.NoteEntity.Companion.tagsFromJson
import com.obsidiancapture.ui.components.MarkdownText
import com.obsidiancapture.ui.theme.StatusGcal
import com.obsidiancapture.ui.theme.StatusPending

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDoneDialog by remember { mutableStateOf(false) }
    var showDropDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarDismissed()
        }
    }

    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) {
            onNavigateBack()
        }
    }

    if (showDoneDialog) {
        AlertDialog(
            onDismissRequest = { showDoneDialog = false },
            title = { Text("Mark Done?") },
            text = { Text("Mark this note as complete?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onMarkDone()
                    showDoneDialog = false
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDoneDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showDropDialog) {
        AlertDialog(
            onDismissRequest = { showDropDialog = false },
            title = { Text("Drop Note?") },
            text = { Text("Drop this note? It will be removed from your inbox.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onMarkDropped()
                    showDropDialog = false
                }) { Text("Drop") }
            },
            dismissButton = {
                TextButton(onClick = { showDropDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onToggleEdit) {
                        Icon(
                            if (state.isEditing) Icons.Filled.Check else Icons.Filled.Edit,
                            contentDescription = if (state.isEditing) "Save" else "Edit",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.note == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Note not found", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                val note = state.note!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Title with crossfade between view/edit
                    Crossfade(targetState = state.isEditing, label = "title-edit") { editing ->
                        if (editing) {
                            OutlinedTextField(
                                value = state.editTitle,
                                onValueChange = viewModel::onEditTitleChange,
                                label = { Text("Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text(
                                text = note.title.ifBlank { "(No title)" },
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }

                    // Body with crossfade
                    Crossfade(targetState = state.isEditing, label = "body-edit") { editing ->
                        if (editing) {
                            OutlinedTextField(
                                value = state.editBody,
                                onValueChange = viewModel::onEditBodyChange,
                                label = { Text("Body") },
                                minLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            if (note.body.isBlank()) {
                                Text(
                                    text = "(No body)",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            } else {
                                MarkdownText(
                                    text = note.body,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    // Tags
                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.editTags,
                            onValueChange = viewModel::onEditTagsChange,
                            label = { Text("Tags (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        val tags = tagsFromJson(note.tags)
                        if (tags.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                tags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(tag) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    // Schedule section card
                    SectionCard(
                        title = "Schedule",
                        icon = Icons.Outlined.Schedule,
                    ) {
                        InfoRow("Date", note.date ?: "Not set")
                        InfoRow("Time", formatTimeRange(note.startTime, note.endTime))
                        InfoRow("Calendar", note.calendar ?: "Auto")
                    }

                    // Status section card
                    SectionCard(
                        title = "Status",
                        icon = Icons.Outlined.CheckCircle,
                    ) {
                        InfoRow("Kind", note.kind.replace("_", " "))
                        if (note.source == "gcal") {
                            SourceInfoRow("Source", "Google Calendar", StatusGcal)
                        } else {
                            InfoRow("Source", note.source.replaceFirstChar { it.uppercase() })
                        }
                        InfoRow("Priority", note.priority ?: "None")
                        InfoRow("GCal", formatGcalStatus(note))

                        if (note.pendingSync) {
                            Text(
                                "Pending sync",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusPending,
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { showDoneDialog = true },
                            enabled = !state.isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Done")
                        }
                        OutlinedButton(
                            onClick = { showDropDialog = true },
                            enabled = !state.isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Text("Drop")
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(20.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val isPlaceholder = value == "Not set" || value == "None" || value == "Not pushed"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium,
            ),
            color = if (isPlaceholder) MaterialTheme.colorScheme.outlineVariant else Color.Unspecified,
        )
    }
}

@Composable
private fun SourceInfoRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

private fun formatTimeRange(start: String?, end: String?): String {
    return when {
        start != null && end != null -> "$start - $end"
        start != null -> "$start"
        else -> "Not set"
    }
}

private fun formatGcalStatus(note: NoteEntity): String {
    return when {
        note.gcalEventId != null -> "Pushed"
        note.gcalEnabled -> "Pending"
        else -> "Not pushed"
    }
}
