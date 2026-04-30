package com.example.madhuryaad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    schedules: List<ScheduleItem>,
    currentlyPlaying: CurrentlyPlaying?,
    onPlayToggle: (ScheduleItem) -> Unit,
    onMenuAction: (ScheduleItem, String) -> Unit
) {
    Text(
        text = "Schedules",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (schedules.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp
        ) {
            Text(
                text = "No schedules yet. Tap Add Song to create one.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(
                items = schedules,
                key = { it.id }
            ) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    isCurrentlyPlaying = currentlyPlaying?.scheduleId == schedule.id,
                    onPlayToggle = { onPlayToggle(schedule) },
                    onAction = { action -> onMenuAction(schedule, action) }
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: ScheduleItem,
    isCurrentlyPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onAction: (String) -> Unit,
    formatTime: (Int, Int) -> String = { h, m -> String.format("%02d:%02d", h, m) } // Default fallback
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val cardColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val playButtonColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val playButtonTextColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPlayToggle() }
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = formatTime(schedule.hour, schedule.minute),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = schedule.songTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )

                    if (!schedule.enabled) {
                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Alarm off",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 1.dp,
                    color = playButtonColor
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCurrentlyPlaying) "■" else "▶",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = playButtonTextColor
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true }
                ) {
                    Text(
                        text = "⋮",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(if (schedule.enabled) "Turn off alarm" else "Turn on alarm")
                        },
                        onClick = {
                            menuExpanded = false
                            onAction("toggle")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Edit time") },
                        onClick = {
                            menuExpanded = false
                            onAction("edit")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Change song") },
                        onClick = {
                            menuExpanded = false
                            onAction("change_song")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onAction("delete")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    use24HourFormat: Boolean,
    onUse24HourChange: (Boolean) -> Unit
) {
    Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Time format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (use24HourFormat) "Using 24-hour time" else "Using 12-hour time",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = use24HourFormat,
                onCheckedChange = onUse24HourChange
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "More settings coming later",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "We can add theme choices, default song, volume, or bigger text here later.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
