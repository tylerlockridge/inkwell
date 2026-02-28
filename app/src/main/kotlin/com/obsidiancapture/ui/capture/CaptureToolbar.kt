package com.obsidiancapture.ui.capture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.obsidiancapture.ui.theme.StatusPending
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val AVAILABLE_TAGS = listOf("work", "personal", "family", "health", "finance")
internal val KIND_OPTIONS = listOf("one_shot" to "Quick", "complex" to "Complex", "brainstorming" to "Ideas")
internal val CALENDAR_OPTIONS = listOf(null to "Auto", "primary" to "Primary", "work" to "Work", "family" to "Family")
internal val PRIORITY_OPTIONS = listOf(null to "None", "high" to "High", "medium" to "Medium", "low" to "Low")

@Composable
internal fun SmartToolbar(
    state: CaptureUiState,
    onMetadataExpandToggle: () -> Unit,
    onToolbarPanelToggle: (ToolbarPanel) -> Unit,
    onTagToggle: (String) -> Unit,
    onKindChange: (String) -> Unit,
    onCalendarChange: (String?) -> Unit,
    onPriorityChange: (String?) -> Unit,
    onQuickDate: (String) -> Unit,
    onDateClear: () -> Unit,
    onPickDate: () -> Unit,
    onPickStartTime: () -> Unit,
    onPickEndTime: () -> Unit,
    onStartTimeClear: () -> Unit,
    onEndTimeClear: () -> Unit,
    onCapture: () -> Unit,
    onBatchModeToggle: () -> Unit,
    isSubmitting: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        // Drag handle — bottom sheet affordance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(2.dp),
                    ),
            )
        }

        // Batch mode row (visible when expanded and no panel open)
        AnimatedVisibility(
            visible = state.isMetadataExpanded && state.activeToolbarPanel == null,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (state.batchMode && state.batchCount > 0) {
                        "Batch mode (${state.batchCount})"
                    } else {
                        "Batch mode"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = state.batchMode,
                    onCheckedChange = { onBatchModeToggle() },
                )
            }
        }

        // Expanded panel (slides up above icon row)
        AnimatedVisibility(
            visible = state.activeToolbarPanel != null,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                expandFrom = Alignment.Bottom,
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                shrinkTowards = Alignment.Bottom,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                when (state.activeToolbarPanel) {
                    ToolbarPanel.Tags -> TagsPanel(state.selectedTags, state.suggestedTags, onTagToggle)
                    ToolbarPanel.Schedule -> SchedulePanel(
                        selectedDate = state.date,
                        startTime = state.startTime,
                        endTime = state.endTime,
                        onQuickDate = onQuickDate,
                        onDateClear = onDateClear,
                        onPickDate = onPickDate,
                        onPickStartTime = onPickStartTime,
                        onPickEndTime = onPickEndTime,
                        onStartTimeClear = onStartTimeClear,
                        onEndTimeClear = onEndTimeClear,
                    )
                    ToolbarPanel.Type -> TypePanel(state.kind, onKindChange)
                    ToolbarPanel.Calendar -> CalendarPanel(state.calendar, onCalendarChange)
                    ToolbarPanel.Priority -> PriorityPanel(state.priority, onPriorityChange)
                    null -> {}
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Toolbar icon row: [+] [optional icons] [spacer] [optional badge] [SEND]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // [+] / [x] toggle button — prominent
            FilledIconButton(
                onClick = onMetadataExpandToggle,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (state.isMetadataExpanded) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (state.isMetadataExpanded) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                ),
            ) {
                Icon(
                    imageVector = if (state.isMetadataExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = if (state.isMetadataExpanded) "Collapse metadata" else "Expand metadata",
                    modifier = Modifier.size(24.dp),
                )
            }

            // Metadata icon row (animated expand/collapse)
            AnimatedVisibility(
                visible = state.isMetadataExpanded,
                enter = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
                exit = shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
            ) {
                Row {
                    CompactToolbarIcon(
                        icon = Icons.Outlined.Tag,
                        contentDescription = "Tags",
                        isActive = state.activeToolbarPanel == ToolbarPanel.Tags,
                        hasSelection = state.selectedTags.isNotEmpty(),
                        onClick = { onToolbarPanelToggle(ToolbarPanel.Tags) },
                    )
                    CompactToolbarIcon(
                        icon = Icons.Outlined.Schedule,
                        contentDescription = "Time",
                        isActive = state.activeToolbarPanel == ToolbarPanel.Schedule,
                        hasSelection = state.date != null || state.startTime != null || state.endTime != null,
                        onClick = { onToolbarPanelToggle(ToolbarPanel.Schedule) },
                    )
                    CompactToolbarIcon(
                        icon = Icons.Outlined.Category,
                        contentDescription = "Type",
                        isActive = state.activeToolbarPanel == ToolbarPanel.Type,
                        hasSelection = state.kind != "one_shot",
                        onClick = { onToolbarPanelToggle(ToolbarPanel.Type) },
                    )
                    CompactToolbarIcon(
                        icon = Icons.Outlined.CalendarMonth,
                        contentDescription = "Calendar",
                        isActive = state.activeToolbarPanel == ToolbarPanel.Calendar,
                        hasSelection = state.calendar != null,
                        onClick = { onToolbarPanelToggle(ToolbarPanel.Calendar) },
                    )
                    CompactToolbarIcon(
                        icon = Icons.Outlined.Flag,
                        contentDescription = "Priority",
                        isActive = state.activeToolbarPanel == ToolbarPanel.Priority,
                        hasSelection = state.priority != null,
                        onClick = { onToolbarPanelToggle(ToolbarPanel.Priority) },
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Pending sync badge (centered when metadata collapsed)
            if (!state.isMetadataExpanded && state.pendingSyncCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(StatusPending.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "${state.pendingSyncCount} pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusPending,
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            // Send button — large, prominent, primary action
            SendButton(onCapture = onCapture, isSubmitting = isSubmitting)
        }
    }
}

@Composable
private fun CompactToolbarIcon(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    hasSelection: Boolean,
    onClick: () -> Unit,
) {
    val tint = when {
        isActive -> MaterialTheme.colorScheme.primary
        hasSelection -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                contentColor = tint,
            ),
        ) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(20.dp))
        }
        // Badge dot — TopEnd position (like app icon notification badge)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 5.dp, end = 5.dp),
        ) {
            AnimatedVisibility(
                visible = hasSelection && !isActive,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialScale = 0f,
                ) + fadeIn(tween(100)),
                exit = scaleOut(tween(100)) + fadeOut(tween(80)),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun SendButton(
    onCapture: () -> Unit,
    isSubmitting: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "send-scale",
    )

    FilledIconButton(
        onClick = onCapture,
        enabled = !isSubmitting,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(52.dp)
            .scale(scale),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private val consistentFilterChipColors
    @Composable get() = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    )

@Composable
private fun TagsPanel(selectedTags: Set<String>, suggestedTags: List<String>, onTagToggle: (String) -> Unit) {
    val orderedTags = suggestedTags.filter { it in AVAILABLE_TAGS } +
        AVAILABLE_TAGS.filter { it !in suggestedTags }

    val suggestedChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(orderedTags) { tag ->
            val isSuggested = tag in suggestedTags
            val isSelected = selectedTags.contains(tag)
            FilterChip(
                selected = isSelected,
                onClick = { onTagToggle(tag) },
                label = { Text(tag) },
                colors = if (isSuggested) suggestedChipColors else consistentFilterChipColors,
            )
        }
    }
}

@Composable
private fun SchedulePanel(
    selectedDate: String?,
    startTime: String?,
    endTime: String?,
    onQuickDate: (String) -> Unit,
    onDateClear: () -> Unit,
    onPickDate: () -> Unit,
    onPickStartTime: () -> Unit,
    onPickEndTime: () -> Unit,
    onStartTimeClear: () -> Unit,
    onEndTimeClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val isToday = selectedDate == LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                FilterChip(
                    selected = isToday,
                    onClick = { if (isToday) onDateClear() else onQuickDate("today") },
                    label = { Text("Today") },
                    colors = consistentFilterChipColors,
                )
            }
            item {
                val isTomorrow = selectedDate == LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                FilterChip(
                    selected = isTomorrow,
                    onClick = { if (isTomorrow) onDateClear() else onQuickDate("tomorrow") },
                    label = { Text("Tomorrow") },
                    colors = consistentFilterChipColors,
                )
            }
            item {
                val isCustom = selectedDate != null &&
                    selectedDate != LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) &&
                    selectedDate != LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                FilterChip(
                    selected = isCustom,
                    onClick = { onPickDate() },
                    label = { Text(if (isCustom) selectedDate!! else "Pick date...") },
                    colors = consistentFilterChipColors,
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = startTime != null,
                    onClick = { if (startTime != null) onStartTimeClear() else onPickStartTime() },
                    label = { Text(if (startTime != null) "Start: $startTime" else "Start time") },
                    colors = consistentFilterChipColors,
                )
            }
            item {
                FilterChip(
                    selected = endTime != null,
                    onClick = { if (endTime != null) onEndTimeClear() else onPickEndTime() },
                    label = { Text(if (endTime != null) "End: $endTime" else "End time") },
                    colors = consistentFilterChipColors,
                )
            }
        }
    }
}

@Composable
private fun TypePanel(kind: String, onKindChange: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(KIND_OPTIONS) { (value, label) ->
            FilterChip(
                selected = kind == value,
                onClick = { onKindChange(value) },
                label = { Text(label) },
                colors = consistentFilterChipColors,
            )
        }
    }
}

@Composable
private fun CalendarPanel(calendar: String?, onCalendarChange: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CALENDAR_OPTIONS) { (value, label) ->
            FilterChip(
                selected = calendar == value,
                onClick = { onCalendarChange(value) },
                label = { Text(label) },
                colors = consistentFilterChipColors,
            )
        }
    }
}

@Composable
private fun PriorityPanel(priority: String?, onPriorityChange: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PRIORITY_OPTIONS) { (value, label) ->
            FilterChip(
                selected = priority == value,
                onClick = { onPriorityChange(value) },
                label = { Text(label) },
                colors = consistentFilterChipColors,
            )
        }
    }
}
