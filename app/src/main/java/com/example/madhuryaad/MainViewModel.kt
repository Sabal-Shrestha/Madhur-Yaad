package com.example.madhuryaad

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    val songOptions = listOf(
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

    val schedules = mutableStateListOf<ScheduleItem>()

    var use24HourFormat by mutableStateOf(false)
        private set

    var currentlyPlaying by mutableStateOf<CurrentlyPlaying?>(null)

    init {
        loadData()
    }

    fun loadData() {
        use24HourFormat = ScheduleStorage.loadUse24HourFormat(context)
        schedules.clear()
        schedules.addAll(ScheduleStorage.loadSchedules(context))
        currentlyPlaying = ScheduleStorage.loadCurrentlyPlaying(context)
    }

    fun updateUse24HourFormat(enabled: Boolean) {
        use24HourFormat = enabled
        ScheduleStorage.saveUse24HourFormat(context, enabled)
    }

    fun addSchedule(hour: Int, minute: Int, song: SongOption) {
        val schedule = ScheduleItem(
            id = ScheduleStorage.getNextScheduleId(context),
            hour = hour,
            minute = minute,
            songRawName = song.rawName,
            songTitle = song.title,
            enabled = true
        )

        schedules.add(schedule)
        saveAndReschedule(schedule)
    }

    fun toggleSchedule(schedule: ScheduleItem, isEnabled: Boolean) {
        val updated = schedule.copy(enabled = isEnabled)
        replaceSchedule(updated)
        
        if (isEnabled) {
            AlarmScheduler.schedule(context, updated)
        } else {
            AlarmScheduler.cancel(context, updated.id)
            if (currentlyPlaying?.scheduleId == updated.id) {
                stopPlaying()
            }
        }
        
        ScheduleStorage.saveSchedules(context, schedules)
    }

    fun editScheduleTime(schedule: ScheduleItem, newHour: Int, newMinute: Int) {
        val updated = schedule.copy(hour = newHour, minute = newMinute)
        replaceSchedule(updated)
        saveAndReschedule(updated)
    }

    fun changeScheduleSong(schedule: ScheduleItem, newSong: SongOption) {
        val updated = schedule.copy(
            songRawName = newSong.rawName,
            songTitle = newSong.title
        )
        replaceSchedule(updated)
        saveAndReschedule(updated)
    }

    fun deleteSchedule(schedule: ScheduleItem) {
        if (currentlyPlaying?.scheduleId == schedule.id) {
            stopPlaying()
        }

        AlarmScheduler.cancel(context, schedule.id)
        schedules.removeAll { it.id == schedule.id }
        ScheduleStorage.saveSchedules(context, schedules)
    }

    private fun replaceSchedule(updated: ScheduleItem) {
        val index = schedules.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            schedules[index] = updated
            // Sort by time
            val sorted = schedules.sortedWith(
                compareBy<ScheduleItem> { it.hour * 60 + it.minute }
                    .thenBy { it.songTitle.lowercase() }
                    .thenBy { it.id }
            )
            schedules.clear()
            schedules.addAll(sorted)
        }
    }

    private fun saveAndReschedule(schedule: ScheduleItem) {
        ScheduleStorage.saveSchedules(context, schedules)
        if (schedule.enabled) {
            AlarmScheduler.schedule(context, schedule)
        }
    }

    fun playSongNow(schedule: ScheduleItem) {
        val intent = Intent(context, SongPlayerService::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
            putExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(MadhurYaadConstants.EXTRA_HOUR, schedule.hour)
            putExtra(MadhurYaadConstants.EXTRA_MINUTE, schedule.minute)
            putExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME, schedule.songRawName)
            putExtra(MadhurYaadConstants.EXTRA_SONG_TITLE, schedule.songTitle)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopPlaying() {
        val intent = Intent(context, SongPlayerService::class.java)
        context.stopService(intent)
        
        ScheduleStorage.clearCurrentPlaying(context)
        currentlyPlaying = null
    }
}
