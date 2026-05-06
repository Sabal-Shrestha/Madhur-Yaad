package com.example.madhuryaad

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.madhuryaad.ui.theme.BKDialogBackground

@Composable
fun HomeScreen(
    schedules: List<ScheduleItem>,
    currentlyPlaying: CurrentlyPlaying?,
    formatTime: (Int, Int) -> String,
    onPlayToggle: (ScheduleItem) -> Unit,
    onMenuAction: (ScheduleItem, String) -> Unit
) {
    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Text(
        text = "Schedules",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(
        modifier = Modifier.height(10.dp)
    )

    if (schedules.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your library is quiet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap Add Song to schedule a reminder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 96.dp
            )
        ) {
            items(
                items = schedules,
                key = { it.id }
            ) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    isCurrentlyPlaying = currentlyPlaying?.scheduleId == schedule.id,
                    formatTime = formatTime,
                    onPlayToggle = {
                        onPlayToggle(schedule)
                    },
                    onAction = { action ->
                        onMenuAction(schedule, action)
                    }
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: ScheduleItem,
    isCurrentlyPlaying: Boolean,
    formatTime: (Int, Int) -> String,
    onPlayToggle: () -> Unit,
    onAction: (String) -> Unit
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val cardColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primaryContainer
    } else if (!schedule.enabled) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else if (!schedule.enabled) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val playButtonColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primary
    } else if (!schedule.enabled) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val playButtonTextColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.onPrimary
    } else if (!schedule.enabled) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onPlayToggle()
                        }
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatTime(
                                schedule.hour,
                                schedule.minute
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = schedule.songTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )

                        if (!schedule.enabled) {
                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

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
                            Icon(
                                imageVector = if (isCurrentlyPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isCurrentlyPlaying) "Stop" else "Play",
                                tint = playButtonTextColor
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        menuExpanded = !menuExpanded
                    }
                ) {
                    Icon(
                        imageVector = if (menuExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (menuExpanded) "Show less" else "Show more",
                        tint = textColor
                    )
                }
            }

            if (menuExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = textColor.copy(alpha = 0.1f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onAction("toggle")
                        }
                    ) {
                        Icon(
                            imageVector = if (schedule.enabled) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                            contentDescription = if (schedule.enabled) "Turn off" else "Turn on",
                            tint = textColor
                        )
                    }

                    IconButton(
                        onClick = {
                            onAction("edit")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit time",
                            tint = textColor
                        )
                    }

                    IconButton(
                        onClick = {
                            onAction("change_song")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Change song",
                            tint = textColor
                        )
                    }

                    IconButton(
                        onClick = {
                            onAction("delete")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    use24HourFormat: Boolean,
    appVersion: String,
    onUse24HourChange: (Boolean) -> Unit,
    onDeleteAll: () -> Unit,
    onRestoreDefaults: () -> Unit,
) {
    val showAboutDialogState = remember {
        mutableStateOf(value = false)
    }

    val showClearConfirmState = remember {
        mutableStateOf(value = false)
    }

    val showRestoreConfirmState = remember {
        mutableStateOf(value = false)
    }

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "24-hour time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = if (use24HourFormat) {
                        "Times will show like 18:30"
                    } else {
                        "Times will show like 6:30 PM"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = use24HourFormat,
                onCheckedChange = onUse24HourChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                    uncheckedBorderColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showClearConfirmState.value = true
            },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Clear all schedules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = "Remove every scheduled song",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
    }

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showRestoreConfirmState.value = true
            },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Restore default schedules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = "Reset to standard meditation times",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showAboutDialogState.value = true
            },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Version",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = appVersion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showAboutDialogState.value) {
        AlertDialog(
            onDismissRequest = {
                showAboutDialogState.value = false
            },
            containerColor = BKDialogBackground,
            title = {
                Text("About Madhur Yaad")
            },
            text = {
                Column {
                    Text(
                        text = "Madhur Yaad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp),
                    )

                    Text(
                        text = "Version: $appVersion",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp),
                    )

                    Text(
                        text = "A simple music reminder app for setting peaceful song schedules.",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp),
                    )

                    Text(
                        text = "Default music: Random",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp),
                    )

                    Text(
                        text = "Song names are read from the music title tag when available. If no title tag is found, the app uses the file name.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAboutDialogState.value = false
                    },
                ) {
                    Text("OK")
                }
            },
        )
    }

    if (showClearConfirmState.value) {
        AlertDialog(
            onDismissRequest = {
                showClearConfirmState.value = false
            },
            containerColor = BKDialogBackground,
            title = {
                Text("Clear all schedules?")
            },
            text = {
                Text("This will permanently remove all your scheduled songs. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAll()
                        showClearConfirmState.value = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearConfirmState.value = false
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showRestoreConfirmState.value) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmState.value = false
            },
            containerColor = BKDialogBackground,
            title = {
                Text("Restore defaults?")
            },
            text = {
                Text("This will replace your current schedules with the standard meditation times.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreDefaults()
                        showRestoreConfirmState.value = false
                    },
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmState.value = false
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}
