package com.obsidiancapture.ui.inbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.local.entity.NoteEntity.Companion.tagsFromJson
import com.obsidiancapture.ui.theme.StatusGcal
import com.obsidiancapture.ui.theme.StatusSynced
import com.obsidiancapture.ui.theme.TagDefault
import com.obsidiancapture.ui.theme.TagFamily
import com.obsidiancapture.ui.theme.TagFinance
import com.obsidiancapture.ui.theme.TagHealth
import com.obsidiancapture.ui.theme.TagPersonal
import com.obsidiancapture.ui.theme.TagWork
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNoteClick: (String) -> Unit,
    onNavigateToCapture: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
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
            title = { Text(if (action == "done") "Mark Done?" else "Drop Note?") },
            text = {
                Text(
                    if (action == "done") "Mark this note as complete?"
                    else "Drop this note? It will be removed from your inbox.",
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
                            placeholder = { Text("Search notes...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Column {
                            Text(
                                "Inbox",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            if (state.lastSyncedAt != null) {
                                Text(
                                    text = "Last synced: ${state.lastSyncedAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onSearchToggle) {
                        Icon(
                            if (state.isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (state.isSearchActive) "Close search" else "Search",
                        )
                    }
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sync")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCapture) {
                Icon(Icons.Filled.Add, contentDescription = "New capture")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Connection banner
            if (!state.isServerConfigured) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .clickable(onClick = onNavigateToSettings)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Not connected \u2014 sign in to start syncing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Go to settings",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Tab row
            if (!state.isSearchActive) {
                TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                    InboxTab.entries.forEach { tab ->
                        Tab(
                            selected = state.selectedTab == tab,
                            onClick = { viewModel.onTabChange(tab) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tab.name)
                                    val badgeCount = when (tab) {
                                        InboxTab.All -> state.allCount
                                        InboxTab.Review -> state.reviewCount
                                        InboxTab.Pending -> state.pendingSyncCount
                                    }
                                    if (badgeCount > 0) {
                                        Spacer(Modifier.width(4.dp))
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) { Text("$badgeCount") }
                                    }
                                }
                            },
                        )
                    }
                }
            }

            // Note list with pull-to-refresh
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.notes.isEmpty()) {
                    // Polished empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val emptyIcon = when {
                                !state.isServerConfigured -> Icons.Outlined.CloudOff
                                state.selectedTab == InboxTab.Pending -> Icons.Outlined.CloudDone
                                else -> Icons.Outlined.Inbox
                            }
                            Icon(
                                emptyIcon,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant,
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                text = when {
                                    state.isSearchActive -> "No results found"
                                    !state.isServerConfigured -> "Not connected"
                                    state.selectedTab == InboxTab.Pending -> "All synced up"
                                    else -> "All clear!"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = when {
                                    state.isSearchActive -> "Try a different search term."
                                    !state.isServerConfigured -> "Sign in with Google in Settings to see your inbox."
                                    state.selectedTab == InboxTab.Pending -> "Nothing pending sync."
                                    else -> "Tap + to capture a thought."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(16.dp))
                            // Action button for empty states
                            when {
                                !state.isServerConfigured -> {
                                    FilledTonalButton(onClick = onNavigateToSettings) {
                                        Text("Sign in")
                                    }
                                }
                                !state.isSearchActive && state.selectedTab != InboxTab.Pending -> {
                                    FilledTonalButton(onClick = onNavigateToCapture) {
                                        Text("Capture something")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp,
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
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDone()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDrop()
                    false
                }
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
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment,
            ) {
                icon?.let {
                    Icon(it, contentDescription = null, tint = Color.White)
                }
            }
        },
    ) {
        NoteCard(note = note, onClick = onClick)
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
) {
    val tags = tagsFromJson(note.tags)
    val primaryTag = tags.firstOrNull()?.lowercase() ?: ""
    val stripeColor = tagStripeColor(primaryTag)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left color stripe (wider for better visibility)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .background(stripeColor),
            )

            // Card body content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp),
            ) {
                Text(
                    text = note.title.ifBlank { note.body.take(50) },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (note.body.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = note.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = formatRelativeTime(note.created),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    if (note.source == "gcal") {
                        SourceBadge()
                    }
                }
            }

            // Sync status icon (right side)
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .align(Alignment.CenterVertically),
            ) {
                when {
                    note.pendingSync -> Icon(
                        Icons.Outlined.Circle,
                        contentDescription = "Pending sync",
                        modifier = Modifier.size(20.dp),
                        tint = com.obsidiancapture.ui.theme.StatusPending,
                    )
                    note.syncedAt != null -> Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = "Synced",
                        modifier = Modifier.size(20.dp),
                        tint = StatusSynced,
                    )
                    else -> Icon(
                        Icons.Outlined.Error,
                        contentDescription = "Sync failed",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceBadge() {
    Box(
        modifier = Modifier
            .background(
                color = StatusGcal.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.extraSmall,
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = "GCal",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
            ),
            color = StatusGcal,
        )
    }
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
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            duration.toDays() == 1L -> "Yesterday"
            duration.toDays() < 7 -> "${duration.toDays()}d ago"
            else -> {
                val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                val currentYear = LocalDate.now().year
                if (date.year == currentYear) {
                    date.format(DateTimeFormatter.ofPattern("MMM d"))
                } else {
                    date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                }
            }
        }
    } catch (_: Exception) {
        isoTimestamp
    }
}
