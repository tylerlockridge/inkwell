package io.inkwell.ui.inbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.local.entity.NoteEntity.Companion.attachmentsFromJson
import io.inkwell.data.local.entity.NoteEntity.Companion.tagsFromJson
import io.inkwell.data.local.entity.browseType
import io.inkwell.data.local.entity.BrowseType
import io.inkwell.ui.theme.AmberGlow
import io.inkwell.ui.theme.StatusGcal
import io.inkwell.ui.theme.StatusSynced
import io.inkwell.ui.theme.TagDefault
import io.inkwell.ui.theme.TagFamily
import io.inkwell.ui.theme.TagFinance
import io.inkwell.ui.theme.TagHealth
import io.inkwell.ui.theme.TagPersonal
import io.inkwell.ui.theme.TagWork
import androidx.compose.material.icons.outlined.PushPin
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// =============================================================================
// Screen
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNoteClick: (String) -> Unit,
    onNavigateToCapture: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingAction by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarDismissed()
        }
    }

    pendingAction?.let { (uid, action) ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(if (action == "done") "Mark Done?" else "Drop Item?") },
            text = {
                Text(
                    if (action == "done") "Mark this item as complete?"
                    else "This item will be removed from your inbox.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (action == "done") viewModel.onMarkDone(uid)
                    else viewModel.onMarkDropped(uid)
                    pendingAction = null
                }) {
                    Text(if (action == "done") "Done" else "Drop")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.isSearchActive) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = {
                                Text(
                                    "Search ${state.selectedTab.name.lowercase()}...",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            "Inbox",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                            ),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onSearchToggle) {
                        Icon(
                            if (state.isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (state.isSearchActive) "Close search" else "Search",
                        )
                    }
                    if (!state.isSearchActive) {
                        IconButton(onClick = viewModel::onRefresh) {
                            Icon(Icons.Filled.Sync, contentDescription = "Sync")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCapture,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New capture")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Connection banner — compact, tappable
            if (!state.isServerConfigured) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                        .clickable(onClick = onNavigateToSettings)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Not connected",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "Settings \u2192",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    )
                }
            }

            // Tab row — cleaner, with count integrated into label
            if (!state.isSearchActive) {
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTab.ordinal,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    divider = {},
                ) {
                    InboxTab.entries.forEach { tab ->
                        val count = state.countForTab(tab)
                        val isSelected = state.selectedTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.onTabChange(tab) },
                            text = {
                                Text(
                                    text = if (count > 0) "${tab.name} $count" else tab.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                )
                            },
                        )
                    }
                }
            }

            // Item list with pull-to-refresh
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.notes.isEmpty()) {
                    InboxEmptyState(
                        isServerConfigured = state.isServerConfigured,
                        isSearchActive = state.isSearchActive,
                        selectedTab = state.selectedTab,
                        onNavigateToCapture = onNavigateToCapture,
                        onNavigateToSettings = onNavigateToSettings,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(
                            start = 12.dp, end = 12.dp,
                            top = 6.dp, bottom = 80.dp,
                        ),
                    ) {
                        items(
                            items = state.notes,
                            key = { it.uid },
                        ) { note ->
                            SwipeableNoteCard(
                                note = note,
                                onClick = { onNoteClick(note.uid) },
                                onDone = { pendingAction = note.uid to "done" },
                                onDrop = { pendingAction = note.uid to "drop" },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Swipeable card wrapper
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onDone: () -> Unit,
    onDrop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onDone(); false }
                SwipeToDismissBoxValue.EndToStart -> { onDrop(); false }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> StatusSynced
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                label = "swipe-color",
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Check
                SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                else -> null
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment,
            ) {
                icon?.let { Icon(it, contentDescription = null, tint = Color.White) }
            }
        },
    ) {
        NoteCard(note = note, onClick = onClick)
    }
}

// =============================================================================
// Note card — redesigned for density + hierarchy
// =============================================================================

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
) {
    val tags = tagsFromJson(note.tags)
    val primaryTag = tags.firstOrNull()?.lowercase() ?: ""
    val stripeColor = captureTypeStripeColor(note.captureType) ?: tagStripeColor(primaryTag)
    val typeLabel = captureTypeLabel(note.captureType, note.kind)

    val cardBackground = if (note.pendingSync) {
        MaterialTheme.colorScheme.surfaceContainerLow.copy()
            .let { base -> Color(
                red = base.red * 0.95f + io.inkwell.ui.theme.StatusPending.red * 0.05f,
                green = base.green * 0.95f + io.inkwell.ui.theme.StatusPending.green * 0.05f,
                blue = base.blue * 0.95f + io.inkwell.ui.theme.StatusPending.blue * 0.05f,
                alpha = base.alpha,
            ) }
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground,
        ),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left type stripe — slightly wider for visibility
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripeColor),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            ) {
                // Row 1: Type badge + title + sync indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TypeBadge(typeLabel, stripeColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = note.title.ifBlank { note.body.take(60) },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    // Sync: only show when NOT synced (pending or error)
                    SyncIndicator(note)
                }

                // Row 2: Body preview or list preview
                if (note.captureType == "list_item" && !note.listItemsJson.isNullOrBlank()) {
                    val items = attachmentsFromJson(note.listItemsJson)
                    if (items.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = items.take(3).joinToString(" \u00B7 "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else if (note.body.isNotBlank() && note.body != note.title) {
                    Spacer(Modifier.height(4.dp))
                    val previewLines = if (note.browseType == BrowseType.NOTE) 2 else 1
                    Text(
                        text = note.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = previewLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Row 3: Timestamp + secondary badges
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (note.pinned) {
                        Icon(
                            Icons.Outlined.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = formatRelativeTime(note.created),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                    if (note.source == "gcal") {
                        SecondaryChip("GCal", StatusGcal)
                    }
                    if (note.persistent) {
                        SecondaryChip("Persistent", MaterialTheme.colorScheme.tertiary)
                    }
                    if (tags.isNotEmpty()) {
                        Text(
                            text = tags.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Card sub-components
// =============================================================================

@Composable
private fun TypeBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.18f),
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp,
            ),
            color = color,
        )
    }
}

@Composable
private fun SyncIndicator(note: NoteEntity) {
    when {
        note.pendingSync -> {
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Outlined.Circle,
                contentDescription = "Pending sync",
                modifier = Modifier.size(14.dp),
                tint = io.inkwell.ui.theme.StatusPending,
            )
        }
        note.syncError != null -> {
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Outlined.Error,
                contentDescription = "Sync error",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.error,
            )
        }
        // Synced → show nothing (clean state is default)
    }
}

@Composable
private fun SecondaryChip(label: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
        ),
        color = color.copy(alpha = 0.8f),
    )
}

// =============================================================================
// Empty state
// =============================================================================

@Composable
private fun InboxEmptyState(
    isServerConfigured: Boolean,
    isSearchActive: Boolean,
    selectedTab: InboxTab,
    onNavigateToCapture: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // Subtle focal point — smaller, less decorative
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp),
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawCircle(color = AmberGlow, radius = size.minDimension / 2f)
                }
                Canvas(modifier = Modifier.size(36.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val rx = size.width * 0.45f
                    val ry = size.height * 0.45f
                    val diamond = Path().apply {
                        moveTo(cx, cy - ry)
                        lineTo(cx + rx, cy)
                        lineTo(cx, cy + ry)
                        lineTo(cx - rx, cy)
                        close()
                    }
                    drawPath(diamond, color = accent.copy(alpha = 0.15f))
                    drawPath(diamond, color = accent.copy(alpha = 0.5f), style = Stroke(width = 1.5.dp.toPx()))
                    drawLine(
                        color = accent.copy(alpha = 0.3f),
                        start = Offset(cx, cy - ry * 0.55f),
                        end = Offset(cx + rx * 0.55f, cy),
                        strokeWidth = 0.8.dp.toPx(),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = when {
                    isSearchActive -> "No results"
                    !isServerConfigured -> "Not connected"
                    selectedTab == InboxTab.Pending -> "All synced"
                    selectedTab != InboxTab.All -> "No ${selectedTab.name.lowercase()} yet"
                    else -> "Inbox clear"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = when {
                    isSearchActive -> "Try a different search term."
                    !isServerConfigured -> "Add your server in Settings to start syncing."
                    selectedTab == InboxTab.Pending -> "Everything is synced."
                    selectedTab == InboxTab.Tasks -> "Capture a task to see it here."
                    selectedTab == InboxTab.Notes -> "Capture a note to see it here."
                    selectedTab == InboxTab.Lists -> "Capture a list to see it here."
                    selectedTab == InboxTab.Ideas -> "Capture an idea to see it here."
                    else -> "Tap + to capture something."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            when {
                !isServerConfigured -> FilledTonalButton(onClick = onNavigateToSettings) {
                    Text("Open Settings")
                }
                !isSearchActive && selectedTab != InboxTab.Pending -> {
                    FilledTonalButton(onClick = onNavigateToCapture) {
                        Text("Capture")
                    }
                }
            }
        }
    }
}

// =============================================================================
// Pure helpers
// =============================================================================

private fun captureTypeLabel(captureType: String?, kind: String): String = when (captureType) {
    "task" -> "Task"
    "note" -> "Note"
    "list_item" -> "List"
    "idea" -> "Idea"
    else -> when (kind) {
        "note" -> "Note"
        "brainstorming" -> "Idea"
        else -> "Task"
    }
}

private fun captureTypeStripeColor(captureType: String?): Color? = when (captureType) {
    "note" -> Color(0xFF6B7FD7)
    "list_item" -> Color(0xFF4DB6AC)
    "idea" -> Color(0xFFFFB74D)
    else -> null
}

private fun tagStripeColor(tag: String): Color = when (tag) {
    "work" -> TagWork
    "personal" -> TagPersonal
    "family" -> TagFamily
    "health" -> TagHealth
    "finance" -> TagFinance
    else -> TagDefault
}

private fun formatRelativeTime(isoTimestamp: String): String {
    return try {
        val instant = Instant.parse(isoTimestamp)
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        when {
            duration.toMinutes() < 1 -> "now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
            duration.toHours() < 24 -> "${duration.toHours()}h"
            duration.toDays() == 1L -> "1d"
            duration.toDays() < 7 -> "${duration.toDays()}d"
            else -> {
                val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                if (date.year == LocalDate.now().year) {
                    date.format(DateTimeFormatter.ofPattern("MMM d"))
                } else {
                    date.format(DateTimeFormatter.ofPattern("MMM d, yy"))
                }
            }
        }
    } catch (_: Exception) {
        isoTimestamp
    }
}
