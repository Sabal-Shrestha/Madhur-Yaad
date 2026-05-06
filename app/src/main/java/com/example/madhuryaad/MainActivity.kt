package com.example.madhuryaad

import android.Manifest
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import com.example.madhuryaad.ui.theme.MadhurYaadTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MadhurYaadConstants.ACTION_PLAYBACK_STARTED -> {
                    viewModel.currentlyPlaying = CurrentlyPlaying(
                        scheduleId = intent.getIntExtra(
                            MadhurYaadConstants.EXTRA_SCHEDULE_ID,
                            -1,
                        ),
                        hour = intent.getIntExtra(
                            MadhurYaadConstants.EXTRA_HOUR,
                            7,
                        ),
                        minute = intent.getIntExtra(
                            MadhurYaadConstants.EXTRA_MINUTE,
                            0,
                        ),
                        songRawName = intent.getStringExtra(
                            MadhurYaadConstants.EXTRA_SONG_RAW_NAME,
                        ) ?: MusicLibrary.RANDOM_RAW_NAME,
                        songTitle = intent.getStringExtra(
                            MadhurYaadConstants.EXTRA_SONG_TITLE,
                        ) ?: MusicLibrary.RANDOM_TITLE,
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
            this,
            playbackStateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
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
        viewModel.loadData()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen(viewModel: MainViewModel) {
        var selectedTab by remember {
            mutableIntStateOf(0)
        }

        val showAddDialogState = remember {
            mutableStateOf(value = false)
        }

        var activeScheduleForDialog by remember {
            mutableStateOf<ScheduleItem?>(value = null)
        }

        var showDeleteDialog by remember {
            mutableStateOf(value = false)
        }

        var showChangeSongDialog by remember {
            mutableStateOf(value = false)
        }

        val appVersion = remember {
            getAppVersionName()
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Madhur Yaad",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            showAddDialogState.value = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Song")
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text("Home")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text("Settings")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        )
{ innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedTab == 0) {
                    HomeScreen(
                        schedules = viewModel.schedules,
                        currentlyPlaying = viewModel.currentlyPlaying,
                        formatTime = { hour, minute ->
                            formatTime(
                                hour = hour,
                                minute = minute
                            )
                        },
                        onPlayToggle = { schedule ->
                        if (viewModel.currentlyPlaying?.scheduleId == schedule.id) {
                            viewModel.stopPlaying()
                        } else {
                            viewModel.playSongNow(schedule)
                        }
                    }
                ) { schedule, action ->
                    activeScheduleForDialog = schedule

                    when (action) {
                        "toggle" -> {
                            viewModel.toggleSchedule(
                                schedule = schedule,
                                isEnabled = !schedule.enabled
                            )
                        }

                        "edit" -> {
                            showTimePicker(
                                hour = schedule.hour,
                                minute = schedule.minute
                            ) { newHour, newMinute ->
                                viewModel.editScheduleTime(
                                    schedule = schedule,
                                    newHour = newHour,
                                    newMinute = newMinute
                                )
                            }
                        }

                        "change_song" -> {
                            showChangeSongDialog = true
                        }

                        "delete" -> {
                            showDeleteDialog = true
                        }
                    }
                }
                } else {
                    SettingsScreen(
                        use24HourFormat = viewModel.use24HourFormat,
                        appVersion = appVersion,
                        onUse24HourChange = { enabled ->
                            viewModel.updateUse24HourFormat(enabled)
                        },
                        onDeleteAll = {
                            viewModel.deleteAllSchedules()
                        }
                    ) {
                        viewModel.restoreDefaultSchedules()
                    }
                }
            }

            if (showAddDialogState.value) {
                AddSongDialog(
                    songOptions = viewModel.songOptions,
                    currentHour = 7,
                    currentMinute = 0,
                    currentSong = viewModel.defaultSongOption,
                    formatTime = { hour, minute ->
                        formatTime(
                            hour = hour,
                            minute = minute,
                        )
                    },
                    onDismiss = {
                        showAddDialogState.value = false
                    },
                    onShowTimePicker = { hour, minute, onSelected ->
                        showTimePicker(
                            hour = hour,
                            minute = minute,
                            onTimeSelected = onSelected,
                        )
                    }
                ) { hour, minute, song ->
                    if (ensureExactAlarmPermission()) {
                        viewModel.addSchedule(
                            hour = hour,
                            minute = minute,
                            song = song,
                        )

                        showAddDialogState.value = false
                    }
                }
            }

            activeScheduleForDialog?.let { schedule ->
                if (showDeleteDialog) {
                    DeleteScheduleDialog(
                        schedule = schedule,
                        formatTime = { hour, minute ->
                            formatTime(
                                hour = hour,
                                minute = minute
                            )
                        },
                        onDismiss = {
                            showDeleteDialog = false
                        },
                    ) {
                        viewModel.deleteSchedule(schedule)
                        showDeleteDialog = false
                    }
                }

                if (showChangeSongDialog) {
                    ChangeSongDialog(
                        songOptions = viewModel.songOptions,
                        initialSong = viewModel.songOptions.find {
                            it.rawName == schedule.songRawName
                        } ?: viewModel.defaultSongOption,
                        onDismiss = {
                            showChangeSongDialog = false
                        },
                    ) { newSong ->
                        viewModel.changeScheduleSong(
                            schedule = schedule,
                            newSong = newSong
                        )

                        showChangeSongDialog = false
                    }
                }
            }
        }
    }

    private fun showTimePicker(
        hour: Int,
        minute: Int,
        onTimeSelected: (Int, Int) -> Unit,
    ) {
        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                onTimeSelected(
                    selectedHour,
                    selectedMinute,
                )
            },
            hour,
            minute,
            viewModel.use24HourFormat
        ).show()
    }

    private fun formatTime(
        hour: Int,
        minute: Int,
    ): String {
        return if (viewModel.use24HourFormat) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                hour,
                minute
            )
        } else {
            val amPm = if (hour < 12) {
                "AM"
            } else {
                "PM"
            }

            var hour12 = hour % 12

            if (hour12 == 0) {
                hour12 = 12
            }

            String.format(
                Locale.getDefault(),
                "%d:%02d %s",
                hour12,
                minute,
                amPm
            )
        }
    }

    private fun ensureExactAlarmPermission(): Boolean {
        if (AlarmScheduler.canScheduleExactAlarms(this)) {
            return true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    "package:$packageName".toUri()
                )
            )

            Toast.makeText(
                this,
                "Allow exact scheduling, then try again.",
                Toast.LENGTH_LONG
            ).show()
        }

        return false
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    2001,
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getAppVersionName(): String {
        return try {
            packageManager.getPackageInfo(
                packageName,
                0,
            ).versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }
}