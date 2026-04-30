package com.example.madhuryaad

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddSongDialog(
    songOptions: List<SongOption>,
    currentHour: Int,
    currentMinute: Int,
    currentSong: SongOption,
    formatTime: (Int, Int) -> String,
    onDismiss: () -> Unit,
    onShowTimePicker: (Int, Int, (Int, Int) -> Unit) -> Unit,
    onAdd: (Int, Int, SongOption) -> Unit
) {
    var tempHour by remember { mutableStateOf(currentHour) }
    var tempMinute by remember { mutableStateOf(currentMinute) }
    var tempSong by remember { mutableStateOf(currentSong) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Song") },
        text = {
            Column {
                Text(
                    text = "Time: ${formatTime(tempHour, tempMinute)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        onShowTimePicker(tempHour, tempMinute) { h, m ->
                            tempHour = h
                            tempMinute = m
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose Time")
                }

                Spacer(modifier = Modifier.height(12.dp))

                SongDropdown(
                    songOptions = songOptions,
                    selected = tempSong,
                    onSelected = { tempSong = it }
                )
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
    onSelected: (SongOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected.title)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            songOptions.forEach { song ->
                DropdownMenuItem(
                    text = { Text(song.title) },
                    onClick = {
                        onSelected(song)
                        expanded = false
                    }
                )
            }
        }
    }
}
