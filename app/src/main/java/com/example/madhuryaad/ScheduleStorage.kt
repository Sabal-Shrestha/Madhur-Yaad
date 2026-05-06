package com.example.madhuryaad

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

object ScheduleStorage {

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(
            MadhurYaadConstants.PREF_NAME,
            Context.MODE_PRIVATE,
        )

    fun loadSchedules(context: Context): List<ScheduleItem> {
        val prefs = getPrefs(context)
        val hasInitialized = prefs.getBoolean(MadhurYaadConstants.PREF_HAS_INITIALIZED, false)

        if (!hasInitialized) {
            val defaults = getDefaultSchedules(context)
            saveSchedules(context, defaults)
            prefs.edit { putBoolean(MadhurYaadConstants.PREF_HAS_INITIALIZED, true) }
            return defaults
        }

        val savedText = prefs.getString(MadhurYaadConstants.PREF_SCHEDULES, "[]") ?: "[]"

        return try {
            val array = JSONArray(savedText)
            val loadedSchedules = mutableListOf<ScheduleItem>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val rawName = obj.getString("songRawName")

                loadedSchedules.add(
                    ScheduleItem(
                        id = obj.getInt("id"),
                        hour = obj.getInt("hour"),
                        minute = obj.getInt("minute"),
                        songRawName = rawName,
                        songTitle = obj.optString("songTitle", rawName),
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }

            sortSchedules(loadedSchedules)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSchedules(context: Context, schedules: List<ScheduleItem>) {
        val sorted = sortSchedules(schedules)
        val array = JSONArray()

        sorted.forEach { schedule ->
            val obj = JSONObject()

            obj.put("id", schedule.id)
            obj.put("hour", schedule.hour)
            obj.put("minute", schedule.minute)
            obj.put("songRawName", schedule.songRawName)
            obj.put("songTitle", schedule.songTitle)
            obj.put("enabled", schedule.enabled)

            array.put(obj)
        }

        getPrefs(context).edit {
            putString(
                MadhurYaadConstants.PREF_SCHEDULES,
                array.toString()
            )
        }
    }

    private fun sortSchedules(schedules: List<ScheduleItem>): List<ScheduleItem> {
        return schedules.sortedWith(
            compareBy<ScheduleItem> { (it.hour * 60) + it.minute }
                .thenBy { it.songTitle.lowercase() }
                .thenBy { it.id }
        )
    }

    fun findScheduleById(context: Context, scheduleId: Int): ScheduleItem? {
        return loadSchedules(context).firstOrNull { it.id == scheduleId }
    }

    fun getNextScheduleId(context: Context): Int {
        val prefs = getPrefs(context)

        val nextId = prefs.getInt(
            MadhurYaadConstants.PREF_NEXT_ID,
            1
        )

        prefs.edit {
            putInt(
                MadhurYaadConstants.PREF_NEXT_ID,
                nextId + 1
            )
        }

        return nextId
    }

    fun saveUse24HourFormat(context: Context, use24Hour: Boolean) {
        getPrefs(context).edit {
            putBoolean(
                MadhurYaadConstants.PREF_USE_24_HOUR,
                use24Hour
            )
        }
    }

    fun loadUse24HourFormat(context: Context): Boolean {
        return getPrefs(context).getBoolean(
            MadhurYaadConstants.PREF_USE_24_HOUR,
            false
        )
    }

    fun loadCurrentlyPlaying(context: Context): CurrentlyPlaying? {
        val prefs = getPrefs(context)

        val isPlaying = prefs.getBoolean(
            MadhurYaadConstants.PREF_CURRENT_IS_PLAYING,
            false
        )

        if (!isPlaying) return null

        return CurrentlyPlaying(
            scheduleId = prefs.getInt(
                MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID,
                -1
            ),
            hour = prefs.getInt(
                MadhurYaadConstants.PREF_CURRENT_HOUR,
                7
            ),
            minute = prefs.getInt(
                MadhurYaadConstants.PREF_CURRENT_MINUTE,
                0
            ),
            songRawName = prefs.getString(
                MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME,
                MusicLibrary.RANDOM_RAW_NAME
            ) ?: MusicLibrary.RANDOM_RAW_NAME,
            songTitle = prefs.getString(
                MadhurYaadConstants.PREF_CURRENT_SONG_TITLE,
                MusicLibrary.RANDOM_TITLE
            ) ?: MusicLibrary.RANDOM_TITLE
        )
    }

    fun saveCurrentlyPlaying(
        context: Context,
        scheduleId: Int,
        hour: Int,
        minute: Int,
        songRawName: String,
        songTitle: String
    ) {
        getPrefs(context).edit {
            putBoolean(
                MadhurYaadConstants.PREF_CURRENT_IS_PLAYING,
                true
            )
            putInt(
                MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID,
                scheduleId
            )
            putInt(
                MadhurYaadConstants.PREF_CURRENT_HOUR,
                hour
            )
            putInt(
                MadhurYaadConstants.PREF_CURRENT_MINUTE,
                minute
            )
            putString(
                MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME,
                songRawName
            )
            putString(
                MadhurYaadConstants.PREF_CURRENT_SONG_TITLE,
                songTitle
            )
        }
    }

    fun clearCurrentPlaying(context: Context) {
        getPrefs(context).edit {
            putBoolean(
                MadhurYaadConstants.PREF_CURRENT_IS_PLAYING,
                false
            )
            remove(MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID)
            remove(MadhurYaadConstants.PREF_CURRENT_HOUR)
            remove(MadhurYaadConstants.PREF_CURRENT_MINUTE)
            remove(MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME)
            remove(MadhurYaadConstants.PREF_CURRENT_SONG_TITLE)
        }
    }

    fun getDefaultSchedules(context: Context): List<ScheduleItem> {
        val songOptions = MusicLibrary.getSongOptions(context)
            .filterNot { it.rawName == MusicLibrary.RANDOM_RAW_NAME }

        val defaultTimes = listOf(
            4 to 0,   // Amritvela
            7 to 0,   // Morning Murli
            10 to 30, // Mid-morning
            12 to 0,  // Noon Remembrance
            18 to 30, // Evening Meditation
            20 to 0,  // Night Meditation
            21 to 30  // Before Sleep
        )

        return defaultTimes.mapIndexed { index, (hour, minute) ->
            val song = songOptions.getOrElse(index % songOptions.size) { songOptions.first() }
            ScheduleItem(
                id = index + 1000,
                hour = hour,
                minute = minute,
                songRawName = song.rawName,
                songTitle = song.title,
                enabled = true
            )
        }
    }
}
