package com.example.madhuryaad

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class SongReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, SongPlayerService::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
        }

        intent.extras?.let {
            serviceIntent.putExtras(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        scheduleAgainForTomorrow(context, intent)
    }

    private fun scheduleAgainForTomorrow(context: Context, oldIntent: Intent) {
        val scheduleId = oldIntent.getIntExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, -1)

        if (scheduleId == -1) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val hour = oldIntent.getIntExtra(MadhurYaadConstants.EXTRA_HOUR, 7)
        val minute = oldIntent.getIntExtra(MadhurYaadConstants.EXTRA_MINUTE, 0)
        val songRawName = oldIntent.getStringExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME)
            ?: "gemini_man"
        val songTitle = oldIntent.getStringExtra(MadhurYaadConstants.EXTRA_SONG_TITLE)
            ?: "Gemini Man"

        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val newIntent = Intent(context, SongReceiver::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
            putExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(MadhurYaadConstants.EXTRA_HOUR, hour)
            putExtra(MadhurYaadConstants.EXTRA_MINUTE, minute)
            putExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME, songRawName)
            putExtra(MadhurYaadConstants.EXTRA_SONG_TITLE, songTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}