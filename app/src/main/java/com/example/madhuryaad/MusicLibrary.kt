package com.example.madhuryaad

import android.content.Context
import android.media.MediaMetadataRetriever
import kotlin.random.Random

object MusicLibrary {

    const val RANDOM_RAW_NAME = "__random__"
    const val RANDOM_TITLE = "Random"

    private data class SongResource(
        val rawName: String,
        val resourceId: Int,
    )

    private val songResources = listOf(
        SongResource("gemini_man", R.raw.gemini_man),
        SongResource("hard_man", R.raw.hard_man),
        SongResource("magnet_man", R.raw.magnet_man),
        SongResource("needle_man", R.raw.needle_man),
        SongResource("shadow_man", R.raw.shadow_man),
        SongResource("snake_man", R.raw.snake_man),
        SongResource("spark_man", R.raw.spark_man),
        SongResource("stage_chosen", R.raw.stage_chosen),
        SongResource("title_screen", R.raw.title_screen),
        SongResource("top_man", R.raw.top_man),
    )

    private var cachedSongOptions: List<SongOption>? = null

    fun getSongOptions(context: Context): List<SongOption> {
        cachedSongOptions?.let { return it }

        val options = buildList {
            add(
                SongOption(
                    title = RANDOM_TITLE,
                    rawName = RANDOM_RAW_NAME,
                )
            )

            songResources.forEach { song ->
                add(
                    SongOption(
                        title = getTitleFromTagOrFileName(
                            context = context,
                            rawName = song.rawName,
                            resourceId = song.resourceId
                        ),
                        rawName = song.rawName
                    )
                )
            }
        }

        cachedSongOptions = options
        return options
    }

    fun getDefaultSongOption(context: Context): SongOption {
        return getSongOptions(context).first()
    }

    fun isRandom(rawName: String): Boolean {
        return rawName == RANDOM_RAW_NAME
    }

    fun findSongOption(context: Context, rawName: String): SongOption {
        return getSongOptions(context).firstOrNull { it.rawName == rawName }
            ?: SongOption(
                title = getFallbackFileName(rawName),
                rawName = rawName
            )
    }

    fun getDisplayTitle(context: Context, rawName: String): String {
        return findSongOption(context, rawName).title
    }

    fun getResourceId(rawName: String): Int? {
        return songResources.firstOrNull { it.rawName == rawName }?.resourceId
    }

    fun getRandomPlayableSong(context: Context): SongOption {
        val playableSongs = getSongOptions(context)
            .filterNot { isRandom(it.rawName) }

        return playableSongs.random(Random(System.currentTimeMillis()))
    }

    fun resolvePlayableSong(context: Context, requestedRawName: String): SongOption {
        if (isRandom(requestedRawName)) {
            return getRandomPlayableSong(context)
        }

        val requestedSong = getSongOptions(context)
            .firstOrNull { it.rawName == requestedRawName }

        if ((requestedSong != null) && (getResourceId(requestedSong.rawName) != null)) {
            return requestedSong
        }

        return getSongOptions(context)
            .firstOrNull { (!isRandom(it.rawName)) && (getResourceId(it.rawName) != null) }
            ?: SongOption(
                title = "gemini_man",
                rawName = "gemini_man"
            )
    }

    private fun getTitleFromTagOrFileName(
        context: Context,
        rawName: String,
        resourceId: Int
    ): String {
        val retriever = MediaMetadataRetriever()

        return try {
            context.resources.openRawResourceFd(resourceId).use { afd ->
                retriever.setDataSource(
                    afd.fileDescriptor,
                    afd.startOffset,
                    afd.length
                )

                val title = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_TITLE
                )?.trim().orEmpty()

                title.ifBlank {
                    getFallbackFileName(rawName)
                }
            }
        } catch (_: Exception) {
            getFallbackFileName(rawName)
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun getFallbackFileName(rawName: String): String {
        return rawName
    }
}