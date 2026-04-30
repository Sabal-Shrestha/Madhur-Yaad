package com.example.madhuryaad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class SongReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, -1)
        val schedule = ScheduleStorage.findScheduleById(context, scheduleId)

        if (schedule == null || !schedule.enabled) {
            if (scheduleId != -1) {
                AlarmScheduler.cancel(context, scheduleId)
            }
            return
        }

        val serviceIntent = Intent(context, SongPlayerService::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
            putExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(MadhurYaadConstants.EXTRA_HOUR, schedule.hour)
            putExtra(MadhurYaadConstants.EXTRA_MINUTE, schedule.minute)
            putExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME, schedule.songRawName)
            putExtra(MadhurYaadConstants.EXTRA_SONG_TITLE, schedule.songTitle)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Exception) {
            return
        }

        AlarmScheduler.schedule(context, schedule)
    }
}
