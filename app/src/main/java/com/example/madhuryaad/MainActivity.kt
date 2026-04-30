package com.example.madhuryaad

import android.Manifest
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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.madhuryaad.ui.theme.MadhurYaadTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MadhurYaadConstants.ACTION_PLAYBACK_STARTED -> {
                    viewModel.currentlyPlaying = CurrentlyPlaying(
                        scheduleId = intent.getIntExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, -1),
                        hour = intent.getIntExtra(MadhurYaadConstants.EXTRA_HOUR, 7),
                        minute = intent.getIntExtra(MadhurYaadConstants.EXTRA_MINUTE, 0),
                        songRawName = intent.getStringExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME) ?: "gemini_man",
                        songTitle = intent.getStringExtra(MadhurYaadConstants.EXTRA_SONG_TITLE) ?: "Gemini Man"
                    )
                }
                MadhurYaadConstants.ACTION_PLAYBACK_STOPPED -> {
                    viewModel.currentlyPlaying = null
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()

        setContent {
            MadhurYaadTheme {
                MainScreen(viewModel)
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
            this, playbackStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(playbackStateReceiver)
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    @Composable
    private fun MainScreen(viewModel: MainViewModel) {
        var selectedTab by remember { mutableStateOf(0) }
        var showAddDialog by remember { mutableStateOf(false) }
        
        var activeScheduleForDialog by remember { mutableStateOf<ScheduleItem?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showChangeSongDialog by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                if (selectedTab == 0) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Text("Add Song")
                    }
                }
            },
            bottomBar = {
                NavigationBar(modifier = Modifier.navigationBarsPadding()) {
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
                    HomeScreen(
                        schedules = viewModel.schedules,
                        currentlyPlaying = viewModel.currentlyPlaying,
                        onPlayToggle = { schedule ->
                            if (viewModel.currentlyPlaying?.scheduleId == schedule.id) {
                                viewModel.stopPlaying()
                            } else {
                                viewModel.playSongNow(schedule)
                            }
                        },
                        onMenuAction = { schedule, action ->
                            activeScheduleForDialog = schedule
                            when (action) {
                                "toggle" -> viewModel.toggleSchedule(schedule, !schedule.enabled)
                                "edit" -> showTimePicker(schedule.hour, schedule.minute) { h, m ->
                                    viewModel.editScheduleTime(schedule, h, m)
                                }
                                "change_song" -> showChangeSongDialog = true
                                "delete" -> showDeleteDialog = true
                            }
                        }
                    )
                } else {
                    SettingsScreen(
                        use24HourFormat = viewModel.use24HourFormat,
                        onUse24HourChange = { viewModel.updateUse24HourFormat(it) }
                    )
                }
            }

            if (showAddDialog) {
                AddSongDialog(
                    songOptions = viewModel.songOptions,
                    currentHour = 7,
                    currentMinute = 0,
                    currentSong = viewModel.songOptions[0],
                    formatTime = { h, m -> formatTime(h, m) },
                    onDismiss = { showAddDialog = false },
                    onShowTimePicker = { h, m, onSelected -> showTimePicker(h, m, onSelected) },
                    onAdd = { h, m, song ->
                        if (ensureExactAlarmPermission()) {
                            viewModel.addSchedule(h, m, song)
                            showAddDialog = false
                        }
                    }
                )
            }

            activeScheduleForDialog?.let { schedule ->
                if (showDeleteDialog) {
                    DeleteScheduleDialog(
                        schedule = schedule,
                        formatTime = { h, m -> formatTime(h, m) },
                        onDismiss = { showDeleteDialog = false },
                        onConfirm = {
                            viewModel.deleteSchedule(schedule)
                            showDeleteDialog = false
                        }
                    )
                }

                if (showChangeSongDialog) {
                    ChangeSongDialog(
                        songOptions = viewModel.songOptions,
                        initialSong = viewModel.songOptions.find { it.rawName == schedule.songRawName } ?: viewModel.songOptions[0],
                        onDismiss = { showChangeSongDialog = false },
                        onConfirm = { newSong ->
                            viewModel.changeScheduleSong(schedule, newSong)
                            showChangeSongDialog = false
                        }
                    )
                }
            }
        }
    }

    private fun showTimePicker(hour: Int, minute: Int, onTimeSelected: (Int, Int) -> Unit) {
        TimePickerDialog(this, { _, h, m -> onTimeSelected(h, m) }, hour, minute, viewModel.use24HourFormat).show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return if (viewModel.use24HourFormat) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val amPm = if (hour < 12) "AM" else "PM"
            var h12 = hour % 12
            if (h12 == 0) h12 = 12
            String.format("%d:%02d %s", h12, minute, amPm)
        }
    }

    private fun ensureExactAlarmPermission(): Boolean {
        if (AlarmScheduler.canScheduleExactAlarms(this)) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            Toast.makeText(this, "Allow exact scheduling, then try again.", Toast.LENGTH_LONG).show()
        }
        return false
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
            }
        }
    }
}
