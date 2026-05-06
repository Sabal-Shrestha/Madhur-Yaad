package com.example.madhuryaad

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

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

        if (intent == null) {
            stopPlayback()
            return START_NOT_STICKY
        }

        val requestedRawName = intent.getStringExtra(
            MadhurYaadConstants.EXTRA_SONG_RAW_NAME,
        ) ?: MusicLibrary.RANDOM_RAW_NAME

        val selectedSong = MusicLibrary.resolvePlayableSong(
            context = this,
            requestedRawName = requestedRawName,
        )

        val songRawName = selectedSong.rawName
        val songTitle = selectedSong.title

        val scheduleId = intent.getIntExtra(
            MadhurYaadConstants.EXTRA_SCHEDULE_ID,
            -1,
        )

        val hour = intent.getIntExtra(
            MadhurYaadConstants.EXTRA_HOUR,
            7,
        )

        val minute = intent.getIntExtra(
            MadhurYaadConstants.EXTRA_MINUTE,
            0,
        )

        startAsForeground(songTitle)

        ScheduleStorage.saveCurrentlyPlaying(
            context = this,
            scheduleId = scheduleId,
            hour = hour,
            minute = minute,
            songRawName = songRawName,
            songTitle = songTitle,
        )

        broadcastPlaybackStarted(
            scheduleId = scheduleId,
            hour = hour,
            minute = minute,
            songRawName = songRawName,
            songTitle = songTitle,
        )

        playSong(
            songRawName = songRawName,
            songTitle = songTitle,
        )

        return START_NOT_STICKY
    }

    private fun playSong(songRawName: String, songTitle: String) {
        stopCurrentMediaOnly()

        val songResourceId = MusicLibrary.getResourceId(songRawName)

        if (songResourceId == null) {
            Toast.makeText(
                this,
                "Song file not found: $songRawName",
                Toast.LENGTH_LONG,
            ).show()

            stopPlayback()
            return
        }

        try {
            val player = MediaPlayer()
            mediaPlayer = player

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )

            player.setWakeMode(
                applicationContext,
                PowerManager.PARTIAL_WAKE_LOCK,
            )

            resources.openRawResourceFd(songResourceId).use { afd ->
                player.setDataSource(
                    afd.fileDescriptor,
                    afd.startOffset,
                    afd.length,
                )
                player.prepare()
            }

            player.isLooping = false
            player.setVolume(1.0f, 1.0f)

            player.setOnCompletionListener {
                stopPlayback()
            }

            player.setOnErrorListener { _, _, _ ->
                stopPlayback()
                true
            }

            player.start()

            Toast.makeText(
                this,
                "Playing: $songTitle",
                Toast.LENGTH_SHORT,
            ).show()
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Could not play song: $songTitle",
                Toast.LENGTH_LONG,
            ).show()

            stopPlayback()
        }
    }

    private fun stopCurrentMediaOnly() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

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
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notification,
            )
        }
    }

    private fun createNotification(songTitle: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Madhur Yaad")
            .setContentText("Playing: $songTitle")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Madhur Yaad Playback",
                NotificationManager.IMPORTANCE_LOW,
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
        songTitle: String,
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