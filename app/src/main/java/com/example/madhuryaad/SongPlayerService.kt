package com.example.madhuryaad

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.os.IBinder
import android.widget.Toast

class SongPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentStartId: Int = 0

    companion object {
        private const val CHANNEL_ID = "song_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentStartId = startId

        val songRawName = intent?.getStringExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME)
            ?: "gemini_man"

        val songTitle = intent?.getStringExtra(MadhurYaadConstants.EXTRA_SONG_TITLE)
            ?: "Gemini Man"

        val scheduleId = intent?.getIntExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, -1) ?: -1
        val hour = intent?.getIntExtra(MadhurYaadConstants.EXTRA_HOUR, 7) ?: 7
        val minute = intent?.getIntExtra(MadhurYaadConstants.EXTRA_MINUTE, 0) ?: 0

        startAsForeground(songTitle)

        ScheduleStorage.saveCurrentlyPlaying(
            this, scheduleId, hour, minute, songRawName, songTitle
        )

        broadcastPlaybackStarted(
            scheduleId = scheduleId,
            hour = hour,
            minute = minute,
            songRawName = songRawName,
            songTitle = songTitle
        )

        playSong(songRawName, songTitle)

        return START_NOT_STICKY
    }

    private fun getSongResourceId(songRawName: String): Int {
        return when (songRawName) {
            "gemini_man" -> R.raw.gemini_man
            "hard_man" -> R.raw.hard_man
            "magnet_man" -> R.raw.magnet_man
            "needle_man" -> R.raw.needle_man
            "shadow_man" -> R.raw.shadow_man
            "snake_man" -> R.raw.snake_man
            "spark_man" -> R.raw.spark_man
            "stage_chosen" -> R.raw.stage_chosen
            "title_screen" -> R.raw.title_screen
            "top_man" -> R.raw.top_man
            else -> R.raw.gemini_man
        }
    }

    private fun playSong(songRawName: String, songTitle: String) {
        stopCurrentMediaOnly()

        val songResourceId = getSongResourceId(songRawName)

        mediaPlayer = MediaPlayer.create(this, songResourceId)

        if (mediaPlayer == null) {
            Toast.makeText(this, "Could not play song", Toast.LENGTH_LONG).show()
            stopPlayback()
            return
        }

        mediaPlayer?.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer?.isLooping = false
        mediaPlayer?.setVolume(1.0f, 1.0f)

        mediaPlayer?.setOnCompletionListener {
            stopPlayback()
        }

        mediaPlayer?.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Song playback error", Toast.LENGTH_LONG).show()
            stopPlayback()
            true
        }

        mediaPlayer?.start()

        Toast.makeText(this, "Playing: $songTitle", Toast.LENGTH_SHORT).show()
    }

    private fun stopCurrentMediaOnly() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        } catch (_: Exception) {
        }

        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun stopPlayback() {
        stopCurrentMediaOnly()

        ScheduleStorage.clearCurrentPlaying(this)
        broadcastPlaybackStopped()

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }

        stopSelf(currentStartId)
    }

    override fun onDestroy() {
        stopCurrentMediaOnly()
        ScheduleStorage.clearCurrentPlaying(this)
        broadcastPlaybackStopped()

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startAsForeground(songTitle: String) {
        val notification = createNotification(songTitle)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(songTitle: String): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Madhur Yaad")
                .setContentText("Playing: $songTitle")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Madhur Yaad")
                .setContentText("Playing: $songTitle")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Madhur Yaad Playback",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun broadcastPlaybackStarted(
        scheduleId: Int,
        hour: Int,
        minute: Int,
        songRawName: String,
        songTitle: String
    ) {
        val intent = Intent(MadhurYaadConstants.ACTION_PLAYBACK_STARTED).apply {
            setPackage(packageName)
            putExtra(MadhurYaadConstants.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(MadhurYaadConstants.EXTRA_HOUR, hour)
            putExtra(MadhurYaadConstants.EXTRA_MINUTE, minute)
            putExtra(MadhurYaadConstants.EXTRA_SONG_RAW_NAME, songRawName)
            putExtra(MadhurYaadConstants.EXTRA_SONG_TITLE, songTitle)
        }

        sendBroadcast(intent)
    }

    private fun broadcastPlaybackStopped() {
        val intent = Intent(MadhurYaadConstants.ACTION_PLAYBACK_STOPPED).apply {
            setPackage(packageName)
        }

        sendBroadcast(intent)
    }
}
