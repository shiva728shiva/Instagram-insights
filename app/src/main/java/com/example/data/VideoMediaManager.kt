package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.data.model.MetricQualifier
import com.example.data.model.ReelInsightsData
import com.example.data.model.ReelItem
import com.example.data.model.RetentionPoint
import com.example.data.model.ViewDataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object VideoMediaManager {

    private const val TAG = "VideoMediaManager"

    data class VideoInfo(
        val uri: Uri,
        val durationMs: Long,
        val durationSeconds: Int,
        val width: Int,
        val height: Int,
        val title: String,
        val thumbnailBitmap: Bitmap?,
        val frameBitmaps: Map<Int, Bitmap> // Key: second -> Bitmap frame
    )

    suspend fun extractVideoInfo(context: Context, uri: Uri): VideoInfo = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        var durationMs = 8000L
        var width = 1080
        var height = 1920
        var title = "Selected Video"
        var thumb: Bitmap? = null
        val frameMap = mutableMapOf<Int, Bitmap>()

        try {
            retriever.setDataSource(context, uri)

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durationStr?.toLongOrNull() ?: 8000L
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val titleStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)

            width = widthStr?.toIntOrNull() ?: 1080
            height = heightStr?.toIntOrNull() ?: 1920
            if (!titleStr.isNullOrBlank()) {
                title = titleStr
            }

            val totalSec = ((durationMs / 1000L).toInt()).coerceIn(1, 120)

            // Extract primary thumbnail at 0s
            thumb = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            // Extract frames for each second for instant lag-free scrubbing
            for (sec in 0..totalSec) {
                try {
                    val frameTimeUs = sec * 1_000_000L
                    val frame = retriever.getFrameAtTime(frameTimeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                    if (frame != null) {
                        frameMap[sec] = frame
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not extract frame at $sec s: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading video metadata: ${e.message}", e)
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        val durationSec = (durationMs / 1000L).toInt().coerceAtLeast(1)

        VideoInfo(
            uri = uri,
            durationMs = durationMs,
            durationSeconds = durationSec,
            width = width,
            height = height,
            title = title,
            thumbnailBitmap = thumb,
            frameBitmaps = frameMap
        )
    }

    fun buildReelItemFromVideo(
        videoInfo: VideoInfo,
        username: String = "alishaasassy"
    ): ReelItem {
        val durationSec = videoInfo.durationSeconds.coerceAtLeast(4)

        // Build dynamic retention points from 0:00 to 0:XX
        val retentionList = mutableListOf<RetentionPoint>()
        for (i in 0..durationSec) {
            val label = formatTimeLabel(i)
            // Realistic retention curve drop off
            val percent = when {
                i == 0 -> 100f
                i == 1 -> 98f
                i == 2 -> 96f
                i == 3 -> 93f
                i <= durationSec / 2 -> (90f - (i * 8f)).coerceAtLeast(30f)
                else -> (30f - ((i - durationSec / 2) * 2.5f)).coerceAtLeast(12f)
            }
            retentionList.add(RetentionPoint(label, percent))
        }

        // When liked points across the video duration
        val likePoints = List((durationSec + 1).coerceAtLeast(8)) { index ->
            when {
                index == 0 -> 15f
                index == 1 -> 85f
                index == 2 -> 100f
                index == 3 -> 95f
                index == durationSec / 2 -> 100f
                else -> (40f + (index * 7f) % 60f)
            }
        }

        val insights = ReelInsightsData(
            caption = "My uploaded reel video 🎬🔥 #viral #reels",
            handle = username,
            thumbnailUrl = videoInfo.uri.toString(),
            videoUri = videoInfo.uri.toString(),
            videoDurationMs = videoInfo.durationMs,
            likes = 34,
            comments = 4,
            reshares = 2,
            sends = 3,
            saves = 1,
            views = 1420,
            viewers = 390,
            avgWatchTime = "${(durationSec * 0.65).toInt()}s",
            follows = 2,
            skipRate = 10.4f,
            skipRateQualifier = MetricQualifier.LOWER,
            shareRate = 0.8f,
            shareRateQualifier = MetricQualifier.TYPICAL,
            likeRate = 2.4f,
            likeRateQualifier = MetricQualifier.HIGHER,
            saveRate = 0.5f,
            saveRateQualifier = MetricQualifier.TYPICAL,
            repostRate = 0.3f,
            repostRateQualifier = MetricQualifier.TYPICAL,
            commentRate = 0.6f,
            commentRateQualifier = MetricQualifier.TYPICAL,
            retention = retentionList,
            whenLiked = likePoints,
            reelsTabPct = 68.4f,
            explorePct = 18.2f,
            profilePct = 9.4f,
            feedPct = 4.0f
        )

        return ReelItem(
            id = "custom_video_${System.currentTimeMillis()}",
            thumbnailUrl = videoInfo.uri.toString(),
            videoUrl = videoInfo.uri.toString(),
            viewsCount = 1420,
            likesCount = 34,
            commentsCount = 4,
            resharesCount = 2,
            sendsCount = 3,
            savesCount = 1,
            caption = "My uploaded reel video 🎬🔥 #viral #reels",
            topOverlayText = "Original video uploaded ⚡",
            watermarks = listOf("TIKTOK @$username", "INSTAGRAM @$username"),
            insightsData = insights
        )
    }

    fun formatTimeLabel(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format(Locale.US, "%d:%02d", min, sec)
    }
}
