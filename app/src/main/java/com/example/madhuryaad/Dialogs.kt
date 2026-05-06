package com.example.madhuryaad

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.madhuryaad.ui.theme.BKDialogBackground

@Composable
fun AddSongDialog(
    songOptions: List<SongOption>,
    currentHour: Int,
    currentMinute: Int,
    currentSong: SongOption,
    formatTime: (Int, Int) -> String,
    onDismiss: () -> Unit,
    onShowTimePicker: (Int, Int, (Int, Int) -> Unit) -> Unit,
    onAdd: (Int, Int, SongOption) -> Unit,
) {
    var tempHour by remember { mutableIntStateOf(currentHour) }
    var tempMinute by remember { mutableIntStateOf(currentMinute) }
    var tempSong by remember { mutableStateOf(currentSong) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BKDialogBackground,
        title = { Text("Add Song") },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Time: ${formatTime(tempHour, tempMinute)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        onShowTimePicker(tempHour, tempMinute) { h, m ->
                            tempHour = h
                            tempMinute = m
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Time")
                }

                Spacer(modifier = Modifier.height(16.dp))

                SongDropdown(
                    songOptions = songOptions,
                    selected = tempSong,
                ) { tempSong = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(tempHour, tempMinute, tempSong) }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangeSongDialog(
    songOptions: List<SongOption>,
    initialSong: SongOption,
    onDismiss: () -> Unit,
    onConfirm: (SongOption) -> Unit
) {
    var tempSong by remember { mutableStateOf(initialSong) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BKDialogBackground,
        title = { Text("Change Song") },
        text = {
            SongDropdown(
                songOptions = songOptions,
                selected = tempSong,
                onSelected = { tempSong = it }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSong) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteScheduleDialog(
    schedule: ScheduleItem,
    formatTime: (Int, Int) -> String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BKDialogBackground,
        title = {
            Text("Delete schedule?")
        },
        text = {
            Text(
                "This will remove ${schedule.songTitle} scheduled for ${formatTime(schedule.hour, schedule.minute)}."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SongDropdown(
    songOptions: List<SongOption>,
    selected: SongOption,
    onSelected: (SongOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(value = false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(selected.title)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            songOptions.forEach { song ->
                DropdownMenuItem(
                    text = { Text(song.title) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        onSelected(song)
                        expanded = false
                    }
                )
            }
        }
    }
}
