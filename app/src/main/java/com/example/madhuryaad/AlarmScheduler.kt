package com.example.madhuryaad

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun schedule(context: Context, schedule: ScheduleItem): Boolean {
        if (!schedule.enabled) {
            cancel(context, schedule.id)
            return false
        }

        if (!canScheduleExactAlarms(context)) {
            return false
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = getNextTriggerTime(schedule.hour, schedule.minute)
        val pendingIntent = createPendingIntent(context, schedule, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        return true
    }

    fun cancel(context: Context, scheduleId: Int) {
        val pendingIntent = createPendingIntentForCancel(context, scheduleId) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun rescheduleAllEnabled(context: Context): Int {
        if (!canScheduleExactAlarms(context)) {
            return 0
        }

        val schedules = ScheduleStorage.loadSchedules(context)
            .filter { it.enabled }

        schedules.forEach { item ->
            schedule(context, item)
        }

        return schedules.size
    }

    private fun getNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    private fun createPendingIntent(
        context: Context,
        schedule: ScheduleItem,
        baseFlag: Int
    ): PendingIntent {
        val intent = Intent(context, SongReceiver::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
            putExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(MadhurYaadConstants.EXTRA_HOUR, schedule.hour)
            putExtra(MadhurYaadConstants.EXTRA_MINUTE, schedule.minute)
            putExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME, schedule.songRawName)
            putExtra(MadhurYaadConstants.EXTRA_SONG_TITLE, schedule.songTitle)
        }

        return PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            baseFlag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createPendingIntentForCancel(context: Context, scheduleId: Int): PendingIntent? {
        val intent = Intent(context, SongReceiver::class.java).apply {
            action = MadhurYaadConstants.ACTION_PLAY_SCHEDULED_SONG
        }

        return PendingIntent.getBroadcast(
            context,
            scheduleId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
