package com.example.madhuryaad

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

object MadhurYaadConstants {
    const val ACTION_PLAY_SCHEDULED_SONG = "com.example.madhuryaad.PLAY_SCHEDULED_SONG"
    const val ACTION_PLAYBACK_STARTED = "com.example.madhuryaad.PLAYBACK_STARTED"
    const val ACTION_PLAYBACK_STOPPED = "com.example.madhuryaad.PLAYBACK_STOPPED"

    const val EXTRA_SCHEDULE_ID = "schedule_id"
    const val EXTRA_HOUR = "hour"
    const val EXTRA_MINUTE = "minute"
    const val EXTRA_SONG_RAW_NAME = "song_raw_name"
    const val EXTRA_SONG_TITLE = "song_title"

    const val PREF_NAME = "madhur_yaad_prefs"
    const val PREF_SCHEDULES = "schedules"
    const val PREF_NEXT_ID = "next_id"
    const val PREF_USE_24_HOUR = "use_24_hour"

    const val PREF_CURRENT_IS_PLAYING = "current_is_playing"
    const val PREF_CURRENT_SCHEDULE_ID = "current_schedule_id"
    const val PREF_CURRENT_HOUR = "current_hour"
    const val PREF_CURRENT_MINUTE = "current_minute"
    const val PREF_CURRENT_SONG_RAW_NAME = "current_song_raw_name"
    const val PREF_CURRENT_SONG_TITLE = "current_song_title"
}

data class SongOption(
    val title: String,
    val rawName: String
)

data class ScheduleItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val songRawName: String,
    val songTitle: String,
    val enabled: Boolean = true
)

data class CurrentlyPlaying(
    val scheduleId: Int,
    val hour: Int,
    val minute: Int,
    val songRawName: String,
    val songTitle: String
)

class MainActivity : ComponentActivity() {

    private val madhurYaadColorScheme = lightColorScheme(
        primary = Color(0xFF7B4FC6),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE9DDFF),
        onPrimaryContainer = Color(0xFF24113F),

        secondary = Color(0xFF9C6B3F),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFE1C2),
        onSecondaryContainer = Color(0xFF331B00),

        background = Color(0xFFFFF8FF),
        onBackground = Color(0xFF1D1A22),

        surface = Color(0xFFFFF8FF),
        onSurface = Color(0xFF1D1A22),

        surfaceVariant = Color(0xFFEDE3F2),
        onSurfaceVariant = Color(0xFF4B4453),

        outline = Color(0xFF7C7284)
    )

    private val songOptions = listOf(
        SongOption("Gemini Man", "gemini_man"),
        SongOption("Hard Man", "hard_man"),
        SongOption("Magnet Man", "magnet_man"),
        SongOption("Needle Man", "needle_man"),
        SongOption("Shadow Man", "shadow_man"),
        SongOption("Snake Man", "snake_man"),
        SongOption("Spark Man", "spark_man"),
        SongOption("Stage Chosen", "stage_chosen"),
        SongOption("Title Screen", "title_screen"),
        SongOption("Top Man", "top_man")
    )

    private val schedules = mutableStateListOf<ScheduleItem>()

    private var selectedHour by mutableStateOf(7)
    private var selectedMinute by mutableStateOf(0)
    private var selectedSong by mutableStateOf(songOptions[0])
    private var use24HourFormat by mutableStateOf(false)

    private var currentlyPlaying by mutableStateOf<CurrentlyPlaying?>(null)

    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MadhurYaadConstants.ACTION_PLAYBACK_STARTED -> {
                    currentlyPlaying = CurrentlyPlaying(
                        scheduleId = intent.getIntExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, -1),
                        hour = intent.getIntExtra(MadhurYaadConstants.EXTRA_HOUR, 7),
                        minute = intent.getIntExtra(MadhurYaadConstants.EXTRA_MINUTE, 0),
                        songRawName = intent.getStringExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME)
                            ?: "gemini_man",
                        songTitle = intent.getStringExtra(MadhurYaadConstants.EXTRA_SONG_TITLE)
                            ?: "Gemini Man"
                    )
                }

                MadhurYaadConstants.ACTION_PLAYBACK_STOPPED -> {
                    currentlyPlaying = null
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        use24HourFormat = getPrefs().getBoolean(
            MadhurYaadConstants.PREF_USE_24_HOUR,
            false
        )

        schedules.clear()
        schedules.addAll(loadSchedules())
        sortSchedulesByTime()

        currentlyPlaying = loadCurrentlyPlaying()

        if (hasExactAlarmPermission()) {
            schedules
                .filter { it.enabled }
                .forEach { schedule ->
                    scheduleOne(schedule)
                }
        }

        setContent {
            MaterialTheme(
                colorScheme = madhurYaadColorScheme
            ) {
                MadhurYaadScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val filter = IntentFilter().apply {
            addAction(MadhurYaadConstants.ACTION_PLAYBACK_STARTED)
            addAction(MadhurYaadConstants.ACTION_PLAYBACK_STOPPED)
        }

        ContextCompat.registerReceiver(
            this,
            playbackStateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()

        try {
            unregisterReceiver(playbackStateReceiver)
        } catch (_: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        currentlyPlaying = loadCurrentlyPlaying()
    }

    @Composable
    private fun MadhurYaadScreen() {
        var selectedTab by remember { mutableStateOf(0) }
        var showAddSongDialog by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                if (selectedTab == 0) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddSongDialog = true }
                    ) {
                        Text("Add Song")
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Text("⌂") },
                        label = { Text("Home") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Text("⚙") },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Madhur Yaad",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    HomeScreen()
                } else {
                    SettingsScreen()
                }
            }

            if (showAddSongDialog) {
                AddSongDialog(
                    onDismiss = { showAddSongDialog = false },
                    onAdd = { hour, minute, song ->
                        val added = addSchedule(hour, minute, song)
                        if (added) {
                            showAddSongDialog = false
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun HomeScreen() {
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
                    ScheduleCard(schedule)
                }
            }
        }
    }

    @Composable
    private fun SettingsScreen() {
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
                    onCheckedChange = {
                        use24HourFormat = it
                        saveSettings()
                    }
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

    @Composable
    private fun ScheduleCard(schedule: ScheduleItem) {
        var menuExpanded by remember(schedule.id) { mutableStateOf(false) }
        var showSongDialog by remember(schedule.id) { mutableStateOf(false) }
        var showDeleteDialog by remember(schedule.id) { mutableStateOf(false) }

        val isCurrentlyPlaying = currentlyPlaying?.scheduleId == schedule.id

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
                        .clickable {
                            if (isCurrentlyPlaying) {
                                stopPlaying()
                            } else {
                                playSongNow(schedule)
                            }
                        }
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
                                toggleSchedule(schedule, !schedule.enabled)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Edit time") },
                            onClick = {
                                menuExpanded = false
                                editScheduleTime(schedule)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Change song") },
                            onClick = {
                                menuExpanded = false
                                showSongDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showSongDialog) {
            ChangeSongDialog(
                initialSong = songOptions.find { it.rawName == schedule.songRawName }
                    ?: songOptions[0],
                onDismiss = { showSongDialog = false },
                onConfirm = { newSong ->
                    changeScheduleSong(schedule, newSong)
                    showSongDialog = false
                }
            )
        }

        if (showDeleteDialog) {
            DeleteScheduleDialog(
                schedule = schedule,
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    deleteSchedule(schedule)
                    showDeleteDialog = false
                }
            )
        }
    }

    @Composable
    private fun DeleteScheduleDialog(
        schedule: ScheduleItem,
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
    private fun AddSongDialog(
        onDismiss: () -> Unit,
        onAdd: (Int, Int, SongOption) -> Unit
    ) {
        var tempHour by remember { mutableStateOf(selectedHour) }
        var tempMinute by remember { mutableStateOf(selectedMinute) }
        var tempSong by remember { mutableStateOf(selectedSong) }

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
                            showTimePicker(tempHour, tempMinute) { hour, minute ->
                                tempHour = hour
                                tempMinute = minute
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose Time")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SongDropdown(
                        selected = tempSong,
                        onSelected = { tempSong = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedHour = tempHour
                        selectedMinute = tempMinute
                        selectedSong = tempSong
                        onAdd(tempHour, tempMinute, tempSong)
                    }
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
    private fun ChangeSongDialog(
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
    private fun SongDropdown(
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

    private fun addSchedule(
        hour: Int,
        minute: Int,
        song: SongOption
    ): Boolean {
        if (!ensureExactAlarmPermission()) {
            return false
        }

        val schedule = ScheduleItem(
            id = getNextScheduleId(),
            hour = hour,
            minute = minute,
            songRawName = song.rawName,
            songTitle = song.title,
            enabled = true
        )

        schedules.add(schedule)
        sortSchedulesByTime()
        saveSchedules()
        scheduleOne(schedule)

        Toast.makeText(this, "Song added to schedule", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun toggleSchedule(schedule: ScheduleItem, isEnabled: Boolean) {
        if (isEnabled) {
            if (!ensureExactAlarmPermission()) {
                return
            }

            val updatedSchedule = schedule.copy(enabled = true)
            replaceSchedule(updatedSchedule)
            saveSchedules()
            scheduleOne(updatedSchedule)

            Toast.makeText(this, "Alarm turned on", Toast.LENGTH_SHORT).show()
        } else {
            cancelSchedule(schedule)

            if (currentlyPlaying?.scheduleId == schedule.id) {
                stopPlaying()
            }

            val updatedSchedule = schedule.copy(enabled = false)
            replaceSchedule(updatedSchedule)
            saveSchedules()

            Toast.makeText(this, "Alarm turned off", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editScheduleTime(schedule: ScheduleItem) {
        showTimePicker(schedule.hour, schedule.minute) { newHour, newMinute ->
            if (schedule.enabled && !ensureExactAlarmPermission()) {
                return@showTimePicker
            }

            cancelSchedule(schedule)

            val updatedSchedule = schedule.copy(
                hour = newHour,
                minute = newMinute
            )

            replaceSchedule(updatedSchedule)
            saveSchedules()

            if (updatedSchedule.enabled) {
                scheduleOne(updatedSchedule)
            }

            Toast.makeText(this, "Time updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeScheduleSong(schedule: ScheduleItem, newSong: SongOption) {
        if (schedule.enabled && !ensureExactAlarmPermission()) {
            return
        }

        cancelSchedule(schedule)

        val updatedSchedule = schedule.copy(
            songRawName = newSong.rawName,
            songTitle = newSong.title
        )

        replaceSchedule(updatedSchedule)
        saveSchedules()

        if (updatedSchedule.enabled) {
            scheduleOne(updatedSchedule)
        }

        Toast.makeText(this, "Song changed", Toast.LENGTH_SHORT).show()
    }

    private fun deleteSchedule(schedule: ScheduleItem) {
        if (currentlyPlaying?.scheduleId == schedule.id) {
            stopPlaying()
        }

        cancelSchedule(schedule)
        schedules.removeAll { it.id == schedule.id }
        sortSchedulesByTime()
        saveSchedules()

        Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show()
    }

    private fun replaceSchedule(updatedSchedule: ScheduleItem) {
        val index = schedules.indexOfFirst { it.id == updatedSchedule.id }
        if (index != -1) {
            schedules[index] = updatedSchedule
            sortSchedulesByTime()
        }
    }

    private fun sortSchedulesByTime() {
        val sortedSchedules = schedules.sortedWith(
            compareBy<ScheduleItem> { it.hour * 60 + it.minute }
                .thenBy { it.songTitle.lowercase() }
                .thenBy { it.id }
        )

        schedules.clear()
        schedules.addAll(sortedSchedules)
    }

    private fun scheduleOne(schedule: ScheduleItem) {
        if (!schedule.enabled) return

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, schedule.hour)
        calendar.set(Calendar.MINUTE, schedule.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(this, SongReceiver::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
            putExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(MadhurYaadConstants.EXTRA_HOUR, schedule.hour)
            putExtra(MadhurYaadConstants.EXTRA_MINUTE, schedule.minute)
            putExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME, schedule.songRawName)
            putExtra(MadhurYaadConstants.EXTRA_SONG_TITLE, schedule.songTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelSchedule(schedule: ScheduleItem) {
        val intent = Intent(this, SongReceiver::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            schedule.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun playSongNow(schedule: ScheduleItem) {
        val intent = Intent(this, SongPlayerService::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
            putExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(MadhurYaadConstants.EXTRA_HOUR, schedule.hour)
            putExtra(MadhurYaadConstants.EXTRA_MINUTE, schedule.minute)
            putExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME, schedule.songRawName)
            putExtra(MadhurYaadConstants.EXTRA_SONG_TITLE, schedule.songTitle)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopPlaying() {
        val intent = Intent(this, SongPlayerService::class.java)
        stopService(intent)

        clearCurrentPlaying()
        currentlyPlaying = null
    }

    private fun showTimePicker(
        startHour: Int,
        startMinute: Int,
        onTimeSelected: (Int, Int) -> Unit
    ) {
        val dialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            startHour,
            startMinute,
            use24HourFormat
        )
        dialog.show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return if (use24HourFormat) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val amPm = if (hour < 12) "AM" else "PM"
            var hour12 = hour % 12
            if (hour12 == 0) hour12 = 12
            String.format("%d:%02d %s", hour12, minute, amPm)
        }
    }

    private fun ensureExactAlarmPermission(): Boolean {
        if (hasExactAlarmPermission()) {
            return true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)

            Toast.makeText(
                this,
                "Allow exact scheduling, then come back and try again.",
                Toast.LENGTH_LONG
            ).show()
        }

        return false
    }

    private fun hasExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    2001
                )
            }
        }
    }

    private fun getNextScheduleId(): Int {
        val prefs = getPrefs()
        val nextId = prefs.getInt(MadhurYaadConstants.PREF_NEXT_ID, 1)

        prefs.edit()
            .putInt(MadhurYaadConstants.PREF_NEXT_ID, nextId + 1)
            .apply()

        return nextId
    }

    private fun saveSettings() {
        getPrefs().edit()
            .putBoolean(MadhurYaadConstants.PREF_USE_24_HOUR, use24HourFormat)
            .apply()
    }

    private fun saveSchedules() {
        sortSchedulesByTime()

        val array = JSONArray()

        schedules.forEach { schedule ->
            val obj = JSONObject()
            obj.put("id", schedule.id)
            obj.put("hour", schedule.hour)
            obj.put("minute", schedule.minute)
            obj.put("songRawName", schedule.songRawName)
            obj.put("songTitle", schedule.songTitle)
            obj.put("enabled", schedule.enabled)
            array.put(obj)
        }

        getPrefs().edit()
            .putString(MadhurYaadConstants.PREF_SCHEDULES, array.toString())
            .apply()
    }

    private fun loadSchedules(): List<ScheduleItem> {
        val savedText = getPrefs()
            .getString(MadhurYaadConstants.PREF_SCHEDULES, "[]") ?: "[]"

        return try {
            val array = JSONArray(savedText)
            val loadedSchedules = mutableListOf<ScheduleItem>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                val rawName = obj.getString("songRawName")
                val matchingSong = songOptions.find { it.rawName == rawName }

                loadedSchedules.add(
                    ScheduleItem(
                        id = obj.getInt("id"),
                        hour = obj.getInt("hour"),
                        minute = obj.getInt("minute"),
                        songRawName = rawName,
                        songTitle = matchingSong?.title ?: obj.optString("songTitle", rawName),
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }

            loadedSchedules.sortedWith(
                compareBy<ScheduleItem> { it.hour * 60 + it.minute }
                    .thenBy { it.songTitle.lowercase() }
                    .thenBy { it.id }
            )
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadCurrentlyPlaying(): CurrentlyPlaying? {
        val prefs = getPrefs()

        val isPlaying = prefs.getBoolean(
            MadhurYaadConstants.PREF_CURRENT_IS_PLAYING,
            false
        )

        if (!isPlaying) return null

        return CurrentlyPlaying(
            scheduleId = prefs.getInt(MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID, -1),
            hour = prefs.getInt(MadhurYaadConstants.PREF_CURRENT_HOUR, 7),
            minute = prefs.getInt(MadhurYaadConstants.PREF_CURRENT_MINUTE, 0),
            songRawName = prefs.getString(
                MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME,
                "gemini_man"
            ) ?: "gemini_man",
            songTitle = prefs.getString(
                MadhurYaadConstants.PREF_CURRENT_SONG_TITLE,
                "Gemini Man"
            ) ?: "Gemini Man"
        )
    }

    private fun clearCurrentPlaying() {
        getPrefs().edit()
            .putBoolean(MadhurYaadConstants.PREF_CURRENT_IS_PLAYING, false)
            .remove(MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID)
            .remove(MadhurYaadConstants.PREF_CURRENT_HOUR)
            .remove(MadhurYaadConstants.PREF_CURRENT_MINUTE)
            .remove(MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME)
            .remove(MadhurYaadConstants.PREF_CURRENT_SONG_TITLE)
            .apply()
    }

    private fun getPrefs() =
        getSharedPreferences(MadhurYaadConstants.PREF_NAME, Context.MODE_PRIVATE)
}