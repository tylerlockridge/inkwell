package com.obsidiancapture.ui.capture

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.obsidiancapture.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        try { focusRequester.requestFocus() } catch (_: IllegalStateException) { /* not yet attached */ }
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
        contentWindowInsets = WindowInsets(0),
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
