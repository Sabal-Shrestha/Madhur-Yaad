package com.example.madhuryaad

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
