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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.obsidiancapture.R
import com.obsidiancapture.ui.theme.StatusPending
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val AVAILABLE_TAGS = listOf("work", "personal", "family", "health", "finance")
private val KIND_OPTIONS = listOf("one_shot" to "Quick", "complex" to "Complex", "brainstorming" to "Ideas")
private val CALENDAR_OPTIONS = listOf(null to "Auto", "primary" to "Primary", "work" to "Work", "family" to "Family")
private val PRIORITY_OPTIONS = listOf(null to "None", "high" to "High", "medium" to "Medium", "low" to "Low")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CaptureScreen(
    sharedText: String? = null,
    sharedTitle: String? = null,
    onNavigateToSettings: () -> Unit = {},
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    // Pre-fill from share intent (one-time)
    LaunchedEffect(sharedText, sharedTitle) {
        if (sharedText != null || sharedTitle != null) {
            val prefill = buildString {
                if (sharedTitle != null) appendLine(sharedTitle)
                if (sharedText != null) append(sharedText)
            }.trim()
            if (prefill.isNotBlank()) {
                viewModel.onUnifiedTextChange(prefill)
            }
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarDismissed()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showDatePicker) {
        CaptureDialogDatePicker(
            onDateSelected = { dateStr ->
                viewModel.onDateChange(dateStr)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showStartTimePicker) {
        CaptureTimePickerDialog(
            onTimeSelected = { time ->
                viewModel.onStartTimeChange(time)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
        )
    }

    if (showEndTimePicker) {
        CaptureTimePickerDialog(
            onTimeSelected = { time ->
                viewModel.onEndTimeChange(time)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        bottomBar = {
            SmartToolbar(
                state = state,
                onMetadataExpandToggle = viewModel::onMetadataExpandToggle,
                onToolbarPanelToggle = viewModel::onToolbarPanelToggle,
                onTagToggle = viewModel::onTagToggle,
                onKindChange = viewModel::onKindChange,
                onCalendarChange = viewModel::onCalendarChange,
                onPriorityChange = viewModel::onPriorityChange,
                onQuickDate = viewModel::onQuickDate,
                onDateClear = { viewModel.onDateChange(null) },
                onPickDate = { showDatePicker = true },
                onPickStartTime = { showStartTimePicker = true },
                onPickEndTime = { showEndTimePicker = true },
                onStartTimeClear = { viewModel.onStartTimeChange(null) },
                onEndTimeClear = { viewModel.onEndTimeChange(null) },
                onCapture = viewModel::onCapture,
                onBatchModeToggle = viewModel::onBatchModeToggle,
                isSubmitting = state.isSubmitting,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (!state.isServerConfigured && !state.isBannerDismissed) {
                ConnectionHint(
                    onNavigateToSettings = onNavigateToSettings,
                    onDismiss = viewModel::onDismissBanner,
                )
            }

            // Writing surface — card with ghost watermark behind transparent TextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                // Ghost brand mark — ink drop at 4% opacity, centred on writing surface
                if (state.unifiedText.isBlank()) {
                    Image(
                        painter = painterResource(R.drawable.ic_inkwell_watermark),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(148.dp),
                        alpha = 0.04f,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    )
                }
                TextField(
                    value = state.unifiedText,
                    onValueChange = viewModel::onUnifiedTextChange,
                    placeholder = {
                        Column {
                            Text(
                                text = "Title",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.2).sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "What's on your mind?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
            }

            // Visual divider after first line (when there's text with a newline)
            if (state.unifiedText.contains("\n")) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            // Selection chips showing active metadata
            ActiveMetadataChips(
                state = state,
                onTagToggle = viewModel::onTagToggle,
                onKindClear = { viewModel.onKindChange("one_shot") },
                onCalendarClear = { viewModel.onCalendarChange(null) },
                onPriorityClear = { viewModel.onPriorityChange(null) },
                onDateClear = { viewModel.onDateChange(null) },
                onStartTimeClear = { viewModel.onStartTimeChange(null) },
                onEndTimeClear = { viewModel.onEndTimeChange(null) },
            )
        }
    }
}

@Composable
private fun ConnectionHint(onNavigateToSettings: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Tap Settings to connect",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onNavigateToSettings,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveMetadataChips(
    state: CaptureUiState,
    onTagToggle: (String) -> Unit,
    onKindClear: () -> Unit,
    onCalendarClear: () -> Unit,
    onPriorityClear: () -> Unit,
    onDateClear: () -> Unit,
    onStartTimeClear: () -> Unit = {},
    onEndTimeClear: () -> Unit = {},
) {
    val hasMetadata = state.selectedTags.isNotEmpty() ||
        state.kind != "one_shot" ||
        state.calendar != null ||
        state.priority != null ||
        state.date != null ||
        state.startTime != null ||
        state.endTime != null

    if (!hasMetadata) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 2.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        state.selectedTags.forEach { tag ->
            DismissibleChip(
                label = "#$tag",
                onDismiss = { onTagToggle(tag) },
            )
        }
        if (state.kind != "one_shot") {
            val kindLabel = KIND_OPTIONS.find { it.first == state.kind }?.second ?: state.kind
            DismissibleChip(label = kindLabel, onDismiss = onKindClear)
        }
        if (state.calendar != null) {
            val calLabel = CALENDAR_OPTIONS.find { it.first == state.calendar }?.second ?: state.calendar
            DismissibleChip(label = calLabel, onDismiss = onCalendarClear)
        }
        if (state.priority != null) {
            val priLabel = PRIORITY_OPTIONS.find { it.first == state.priority }?.second ?: state.priority
            DismissibleChip(label = priLabel, onDismiss = onPriorityClear)
        }
        if (state.date != null) {
            val dateLabel = formatDateLabel(state.date)
            DismissibleChip(label = dateLabel, onDismiss = onDateClear)
        }
        if (state.startTime != null) {
            DismissibleChip(label = "Start: ${state.startTime}", onDismiss = onStartTimeClear)
        }
        if (state.endTime != null) {
            DismissibleChip(label = "End: ${state.endTime}", onDismiss = onEndTimeClear)
        }
    }
}

@Composable
private fun DismissibleChip(
    label: String,
    onDismiss: () -> Unit,
) {
    AssistChip(
        onClick = onDismiss,
        label = {
            Text(label, style = MaterialTheme.typography.labelSmall)
        },
        trailingIcon = {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(12.dp),
            )
        },
        shape = CircleShape,
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            trailingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        ),
    )
}

@Composable
private fun SmartToolbar(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaptureDialogDatePicker(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                },
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaptureTimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val time = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onTimeSelected(time)
                },
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            TimeInput(state = timePickerState)
        },
    )
}

private fun formatDateLabel(date: String): String {
    return try {
        val parsed = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now()
        when (parsed) {
            today -> "Today"
            today.plusDays(1) -> "Tomorrow"
            else -> date
        }
    } catch (_: Exception) {
        date
    }
}
