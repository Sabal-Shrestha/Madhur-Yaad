package com.example.madhuryaad

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ScheduleStorage {

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(MadhurYaadConstants.PREF_NAME, Context.MODE_PRIVATE)

    fun loadSchedules(context: Context): List<ScheduleItem> {
        val savedText = getPrefs(context)
            .getString(MadhurYaadConstants.PREF_SCHEDULES, "[]") ?: "[]"

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

        getPrefs(context).edit()
            .putString(MadhurYaadConstants.PREF_SCHEDULES, array.toString())
            .apply()
    }

    private fun sortSchedules(schedules: List<ScheduleItem>): List<ScheduleItem> {
        return schedules.sortedWith(
            compareBy<ScheduleItem> { it.hour * 60 + it.minute }
                .thenBy { it.songTitle.lowercase() }
                .thenBy { it.id }
        )
    }

    fun findScheduleById(context: Context, scheduleId: Int): ScheduleItem? {
        return loadSchedules(context).firstOrNull { it.id == scheduleId }
    }

    fun getNextScheduleId(context: Context): Int {
        val prefs = getPrefs(context)
        val nextId = prefs.getInt(MadhurYaadConstants.PREF_NEXT_ID, 1)

        prefs.edit()
            .putInt(MadhurYaadConstants.PREF_NEXT_ID, nextId + 1)
            .apply()

        return nextId
    }

    fun saveUse24HourFormat(context: Context, use24Hour: Boolean) {
        getPrefs(context).edit()
            .putBoolean(MadhurYaadConstants.PREF_USE_24_HOUR, use24Hour)
            .apply()
    }

    fun loadUse24HourFormat(context: Context): Boolean {
        return getPrefs(context).getBoolean(MadhurYaadConstants.PREF_USE_24_HOUR, false)
    }

    fun loadCurrentlyPlaying(context: Context): CurrentlyPlaying? {
        val prefs = getPrefs(context)

        val isPlaying = prefs.getBoolean(
            MadhurYaadConstants.PREF_CURRENT_IS_PLAYING,
            false
        )

        if (!isPlaying) return null

        return CurrentlyPlaying(
            scheduleId = prefs.getInt(MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID, -1),
            hour = prefs.getInt(MadhurYaadConstants.PREF_CURRENT_HOUR, 7),
            minute = prefs.getInt(MadhurYaadConstants.PREF_CURRENT_MINUTE, 0),
            songRawName = prefs.getString(
                MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME,
                "gemini_man"
            ) ?: "gemini_man",
            songTitle = prefs.getString(
                MadhurYaadConstants.PREF_CURRENT_SONG_TITLE,
                "Gemini Man"
            ) ?: "Gemini Man"
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
        getPrefs(context).edit()
            .putBoolean(MadhurYaadConstants.PREF_CURRENT_IS_PLAYING, true)
            .putInt(MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID, scheduleId)
            .putInt(MadhurYaadConstants.PREF_CURRENT_HOUR, hour)
            .putInt(MadhurYaadConstants.PREF_CURRENT_MINUTE, minute)
            .putString(MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME, songRawName)
            .putString(MadhurYaadConstants.PREF_CURRENT_SONG_TITLE, songTitle)
            .apply()
    }

    fun clearCurrentPlaying(context: Context) {
        getPrefs(context).edit()
            .putBoolean(MadhurYaadConstants.PREF_CURRENT_IS_PLAYING, false)
            .remove(MadhurYaadConstants.PREF_CURRENT_SCHEDULE_ID)
            .remove(MadhurYaadConstants.PREF_CURRENT_HOUR)
            .remove(MadhurYaadConstants.PREF_CURRENT_MINUTE)
            .remove(MadhurYaadConstants.PREF_CURRENT_SONG_RAW_NAME)
            .remove(MadhurYaadConstants.PREF_CURRENT_SONG_TITLE)
            .apply()
    }
}
