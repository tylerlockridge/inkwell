package io.inkwell.ui.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.inkwell.data.local.entity.BrowseType
import io.inkwell.data.local.entity.ChecklistItem
import io.inkwell.data.local.entity.ChecklistItems
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.local.entity.NoteEntity.Companion.tagsFromJson
import io.inkwell.data.local.entity.browseType
import io.inkwell.ui.components.MarkdownText
import io.inkwell.ui.theme.StatusGcal
import io.inkwell.ui.theme.StatusPending
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow

// =============================================================================
// Screen
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
        if (state.navigateBack) onNavigateBack()
    }

    if (showDoneDialog) {
        AlertDialog(
            onDismissRequest = { showDoneDialog = false },
            title = { Text("Mark Done?") },
            text = { Text("Mark this item as complete?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onMarkDone(); showDoneDialog = false }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDoneDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showDropDialog) {
        AlertDialog(
            onDismissRequest = { showDropDialog = false },
            title = { Text("Drop Item?") },
            text = { Text("This item will be removed from your inbox.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onMarkDropped(); showDropDialog = false }) { Text("Drop") }
            },
            dismissButton = {
                TextButton(onClick = { showDropDialog = false }) { Text("Cancel") }
            },
        )
    }

    val type = state.note?.browseType ?: BrowseType.TASK

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TypeBadge(type)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            type.singularLabel.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                            ),
                        )
                    }
                },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.note == null -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Item not found", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                val note = state.note!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Status indicators — inline with content, not cards
                    val statusLabels = buildList {
                        if (note.pendingSync) add("Pending sync" to StatusPending)
                        if (note.pinned) add("Pinned" to MaterialTheme.colorScheme.primary)
                        if (note.shared) add("Shared" to MaterialTheme.colorScheme.tertiary)
                    }
                    if (statusLabels.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            statusLabels.forEach { (label, color) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (label == "Pinned") {
                                        Icon(
                                            Icons.Outlined.PushPin,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = color,
                                        )
                                        Spacer(Modifier.width(3.dp))
                                    }
                                    if (label == "Shared") {
                                        Icon(
                                            Icons.Outlined.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = color,
                                        )
                                        Spacer(Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                        ),
                                        color = color,
                                    )
                                }
                            }
                        }
                    }

                    // Title
                    EditableTitle(state.isEditing, note.title.ifBlank { "(No title)" }, state.editTitle, viewModel::onEditTitleChange)

                    // Type-dispatched content
                    when (type) {
                        BrowseType.TASK -> TaskDetailContent(note, state, viewModel)
                        BrowseType.NOTE -> NoteDetailContent(note, state, viewModel)
                        BrowseType.LIST -> ListDetailContent(note, state, viewModel)
                        BrowseType.IDEA -> IdeaDetailContent(note, state, viewModel)
                    }

                    // Action buttons
                    Spacer(Modifier.height(4.dp))
                    ActionButtons(state.isSaving, onDone = { showDoneDialog = true }, onDrop = { showDropDialog = true })
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// =============================================================================
// Type badge (matching inbox visual language)
// =============================================================================

@Composable
private fun TypeBadge(type: BrowseType) {
    val color = when (type) {
        BrowseType.TASK -> MaterialTheme.colorScheme.primary
        BrowseType.NOTE -> Color(0xFF6B7FD7)
        BrowseType.LIST -> Color(0xFF4DB6AC)
        BrowseType.IDEA -> Color(0xFFFFB74D)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = type.singularLabel.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp,
            ),
            color = color,
        )
    }
}

// =============================================================================
// Type-specific content
// =============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskDetailContent(note: NoteEntity, state: NoteDetailUiState, viewModel: NoteDetailViewModel) {
    EditableBody(state.isEditing, note.body, state.editBody, viewModel::onEditBodyChange)
    EditableTags(state.isEditing, note.tags, state.editTags, viewModel::onEditTagsChange)

    // Schedule
    SectionLabel("Schedule")
    MetadataCard {
        MetadataRow("Date", note.date ?: "Not set")
        MetadataRow("Time", formatTimeRange(note.startTime, note.endTime))
        MetadataRow("Calendar", note.calendar ?: "Auto")
    }

    // Status
    SectionLabel("Status")
    MetadataCard {
        MetadataRow("Priority", note.priority ?: "None")
        MetadataRow("Kind", note.kind.replace("_", " "))
        MetadataRow("Source", note.source.replaceFirstChar { it.uppercase() })
        MetadataRow("GCal", formatGcalStatus(note))
        ColorDot(note.color)
        SourceUrlRow(note.sourceUrl)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteDetailContent(note: NoteEntity, state: NoteDetailUiState, viewModel: NoteDetailViewModel) {
    EditableBody(state.isEditing, note.body, state.editBody, viewModel::onEditBodyChange, minEditLines = 10)
    EditableTags(state.isEditing, note.tags, state.editTags, viewModel::onEditTagsChange)

    SectionLabel("Details")
    MetadataCard {
        MetadataRow("Source", note.source.replaceFirstChar { it.uppercase() })
        MetadataRow("Kind", note.kind.replace("_", " "))
        ColorDot(note.color)
        SourceUrlRow(note.sourceUrl)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListDetailContent(note: NoteEntity, state: NoteDetailUiState, viewModel: NoteDetailViewModel) {
    // List info
    if (!note.listName.isNullOrBlank() || note.persistent) {
        MetadataCard {
            if (!note.listName.isNullOrBlank()) MetadataRow("List", note.listName)
            if (note.persistent) MetadataRow("Persistent", "Yes")
        }
    }

    // Interactive checklist
    val items = ChecklistItems.parse(note.listItemsJson)
    if (items.isNotEmpty()) {
        val checkedCount = items.count { it.checked }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${items.size} items",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (checkedCount > 0) {
                        Text(
                            "$checkedCount/${items.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
                items.forEachIndexed { index, item ->
                    ChecklistItemRow(item) { viewModel.onToggleListItem(index) }
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 48.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        )
                    }
                }
                Text(
                    "Saved locally",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }

    // Body (if no list items, or in edit mode)
    if (note.body.isNotBlank() && items.isEmpty()) {
        EditableBody(state.isEditing, note.body, state.editBody, viewModel::onEditBodyChange)
    } else if (state.isEditing) {
        EditableBody(true, note.body, state.editBody, viewModel::onEditBodyChange)
    }

    EditableTags(state.isEditing, note.tags, state.editTags, viewModel::onEditTagsChange)

    SectionLabel("Details")
    MetadataCard {
        MetadataRow("Source", note.source.replaceFirstChar { it.uppercase() })
        ColorDot(note.color)
        SourceUrlRow(note.sourceUrl)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdeaDetailContent(note: NoteEntity, state: NoteDetailUiState, viewModel: NoteDetailViewModel) {
    Text(
        "Brainstorm",
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
        ),
        fontStyle = FontStyle.Italic,
        color = Color(0xFFFFB74D).copy(alpha = 0.8f),
    )

    EditableBody(state.isEditing, note.body, state.editBody, viewModel::onEditBodyChange, minEditLines = 8)
    EditableTags(state.isEditing, note.tags, state.editTags, viewModel::onEditTagsChange)

    SectionLabel("Details")
    MetadataCard {
        MetadataRow("Source", note.source.replaceFirstChar { it.uppercase() })
        ColorDot(note.color)
        SourceUrlRow(note.sourceUrl)
    }
}

// =============================================================================
// Checklist row
// =============================================================================

@Composable
private fun ChecklistItemRow(item: ChecklistItem, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
    ) {
        Checkbox(checked = item.checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(4.dp))
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
            ),
            color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// =============================================================================
// Shared composables
// =============================================================================

@Composable
private fun EditableTitle(isEditing: Boolean, displayTitle: String, editTitle: String, onEditTitleChange: (String) -> Unit) {
    Crossfade(targetState = isEditing, label = "title-edit") { editing ->
        if (editing) {
            OutlinedTextField(
                value = editTitle, onValueChange = onEditTitleChange,
                label = { Text("Title") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 28.sp,
                ),
            )
        }
    }
}

@Composable
private fun EditableBody(isEditing: Boolean, body: String, editBody: String, onEditBodyChange: (String) -> Unit, minEditLines: Int = 6) {
    Crossfade(targetState = isEditing, label = "body-edit") { editing ->
        if (editing) {
            OutlinedTextField(
                value = editBody, onValueChange = onEditBodyChange,
                label = { Text("Body") }, minLines = minEditLines,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            if (body.isBlank()) {
                Text("(No body)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outlineVariant)
            } else {
                MarkdownText(text = body, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditableTags(isEditing: Boolean, tagsJson: String, editTags: String, onEditTagsChange: (String) -> Unit) {
    if (isEditing) {
        OutlinedTextField(
            value = editTags, onValueChange = onEditTagsChange,
            label = { Text("Tags (comma separated)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        val tags = tagsFromJson(tagsJson)
        if (tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    SuggestionChip(
                        onClick = {}, label = { Text(tag, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(isSaving: Boolean, onDone: () -> Unit, onDrop: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilledTonalButton(
            onClick = onDone, enabled = !isSaving,
            modifier = Modifier.weight(1f).height(44.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Done", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onDrop, enabled = !isSaving,
            modifier = Modifier.weight(1f).height(44.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
            Text("Drop")
        }
    }
}

// =============================================================================
// Metadata building blocks
// =============================================================================

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 11.sp,
        ),
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun MetadataCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            content()
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    val isPlaceholder = value == "Not set" || value == "None" || value == "Not pushed" || value == "Auto"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium,
            ),
            color = if (isPlaceholder) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SourceUrlRow(sourceUrl: String?) {
    if (sourceUrl.isNullOrBlank()) return
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Source", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = sourceUrl.removePrefix("https://").removePrefix("http://").take(40),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
                try { uriHandler.openUri(sourceUrl) } catch (_: Exception) {}
            },
        )
    }
}

@Composable
private fun ColorDot(color: String?) {
    if (color.isNullOrBlank()) return
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (_: Exception) { return }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Color", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(parsedColor, RoundedCornerShape(3.dp)),
        )
    }
}

// =============================================================================
// Pure helpers
// =============================================================================

private fun formatTimeRange(start: String?, end: String?): String = when {
    start != null && end != null -> "$start \u2013 $end"
    start != null -> start
    else -> "Not set"
}

private fun formatGcalStatus(note: NoteEntity): String = when {
    note.gcalEventId != null -> "Pushed"
    note.gcalEnabled -> "Pending"
    else -> "Not pushed"
}
