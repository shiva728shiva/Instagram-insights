package com.example.data

import com.example.data.model.MetricQualifier
import com.example.data.model.RetentionPoint
import com.example.data.model.ViewDataPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.exp
import kotlin.math.roundToInt

object HealthyGraphGenerator {

    /**
     * Generates a realistic, algorithmically sound 12-day views over time chart.
     * Follows Instagram's healthy viral distribution curve:
     * - Day 0: 0 views (release moment)
     * - Day 1: 18%-25% initial audience velocity
     * - Day 2: 50%-68% algorithmic push (Explore / Reels tab breakout)
     * - Day 3-4: 78%-88% steady cumulative climb
     * - Day 5-7: 94%-99% asymptotic approach to total
     * - Day 8-9: 100% plateau at total views
     * - Day 10-11: -1f (pending future reporting)
     *
     * Typical curve follows account baseline (~25%-35% of total peak)
     */
    fun generateViewsOverTime(
        totalViews: Int,
        typicalRatio: Float = 0.32f,
        daysCount: Int = 12,
        customStartLabel: String? = null,
        customMidLabel: String? = null,
        customEndLabel: String? = null
    ): List<ViewDataPoint> {
        val safeTotal = totalViews.coerceAtLeast(10).toFloat()
        val typicalPeak = (safeTotal * typicalRatio).coerceAtLeast(4f)

        val dateFormat = SimpleDateFormat("MMM d", Locale.US)
        val calendar = Calendar.getInstance()

        if (!customStartLabel.isNullOrBlank()) {
            try {
                val parsed = dateFormat.parse(customStartLabel.trim())
                if (parsed != null) {
                    val calTemp = Calendar.getInstance()
                    val curYear = calTemp.get(Calendar.YEAR)
                    calTemp.time = parsed
                    calTemp.set(Calendar.YEAR, curYear)
                    calendar.time = calTemp.time
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, -(daysCount - 3))
                }
            } catch (e: Exception) {
                calendar.add(Calendar.DAY_OF_MONTH, -(daysCount - 3))
            }
        } else {
            // Offset so that day 9 is roughly today, 10-11 are next days
            calendar.add(Calendar.DAY_OF_MONTH, -(daysCount - 3))
        }

        val dates = (0 until daysCount).map { i ->
            if (i == 0 && !customStartLabel.isNullOrBlank()) {
                customStartLabel.trim()
            } else if (i == 6 && !customMidLabel.isNullOrBlank()) {
                customMidLabel.trim()
            } else if (i == daysCount - 1 && !customEndLabel.isNullOrBlank()) {
                customEndLabel.trim()
            } else {
                val dateStr = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                dateStr
            }
        }

        // Percentage milestones along the 12-day lifecycle
        val cumulativePct = listOf(
            0.0f,     // Day 0
            0.22f,    // Day 1
            0.58f,    // Day 2 (breakout)
            0.76f,    // Day 3
            0.86f,    // Day 4
            0.92f,    // Day 5
            0.96f,    // Day 6
            0.985f,   // Day 7
            1.0f,     // Day 8
            1.0f,     // Day 9
            -1.0f,    // Day 10 (unreported)
            -1.0f     // Day 11 (unreported)
        )

        val typicalPct = listOf(
            0.0f,
            0.35f,
            0.70f,
            0.90f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            0.0f
        )

        return (0 until daysCount).map { i ->
            val date = dates.getOrElse(i) { "Day ${i + 1}" }
            val thisPct = cumulativePct.getOrElse(i) { 1.0f }
            val typPct = typicalPct.getOrElse(i) { 1.0f }

            val thisViews = if (thisPct < 0f) -1f else (safeTotal * thisPct).roundToInt().toFloat()
            val typViews = if (typPct <= 0f && i == daysCount - 1) 0f else (typicalPeak * typPct).roundToInt().toFloat()

            ViewDataPoint(
                dateLabel = date,
                viewsThisReel = thisViews,
                viewsTypical = typViews
            )
        }
    }

    /**
     * Generates a realistic retention curve from 0:00 up to the video's duration.
     * Healthy reach characteristics:
     * - First 3 seconds (Hook): High retention (>90%), gentle drop.
     * - Middle section: smooth exponential decay.
     * - Tail (Call to action / loop): healthy retention between 20% and 38%.
     */
    fun generateRetentionCurve(
        durationSeconds: Int = 8,
        totalViews: Int = 1379,
        avgWatchTimeSec: Float = 6.8f,
        skipRatePercent: Float = 11.2f
    ): List<RetentionPoint> {
        val totalSec = durationSeconds.coerceIn(4, 90)
        val dropPoint = (avgWatchTimeSec / totalSec.toFloat()).coerceIn(0.15f, 0.85f)
        val dropSteepness = 7f + (skipRatePercent / 100f) * 18f

        val raw = (0..totalSec).map { sec ->
            val t = sec.toFloat() / totalSec.toFloat()
            val decay = 1f / (1f + exp(dropSteepness * (t - dropPoint)))
            val floor = if (totalViews > 3000) 24f else 18f
            (floor + (100f - floor) * decay).coerceIn(12f, 100f)
        }

        return (0..totalSec).map { sec ->
            val label = formatTimeLabel(sec)
            val pct = raw[sec]
            RetentionPoint(label, pct.roundToSingleDecimal())
        }
    }

    /**
     * Generates a "When people liked your reel" activity distribution curve.
     * Naturally peaks at 25%-45% of the video duration where the punchline or climax occurs.
     */
    fun generateWhenLiked(
        durationSeconds: Int = 8,
        likesCount: Int = 28
    ): List<Float> {
        val count = (durationSeconds + 1).coerceIn(8, 24)
        val peakIndex = (count * 0.35f).toInt().coerceIn(1, count - 2)

        val raw = (0 until count).map { i ->
            val distFromPeak = kotlin.math.abs(i - peakIndex).toFloat()
            val base = 100f - (distFromPeak * 18f)
            when {
                i == 0 -> 15f
                i == peakIndex -> 100f
                i == peakIndex - 1 -> 90f
                i == peakIndex + 1 -> 95f
                else -> base.coerceIn(10f, 85f)
            }
        }
        return raw
    }

    /**
     * Computes healthy Instagram metrics (viewers, average watch time, skip rate, like rate)
     * correlated realistically to total views and video duration.
     */
    fun computeHealthyMetrics(
        views: Int,
        durationSeconds: Int = 8
    ): HealthyMetricsResult {
        val safeViews = views.coerceAtLeast(1)
        val durationSec = durationSeconds.coerceIn(3, 90)

        // Viewers (Unique accounts reached): typically 65% - 82% of total views due to loops/re-watches
        val viewers = (safeViews * 0.74).toInt().coerceAtLeast(1)

        // Average watch time: healthy reels achieve 70% to 120% of video length
        val avgWatchSec = ((durationSec * 0.85).toInt()).coerceAtLeast(3)
        val avgWatchTime = "${avgWatchSec}s"

        // Skip rate: healthy reels have lower skip rate (< 16%)
        val skipRate = (11.5f + (800f / (safeViews + 200f)) * 2f).coerceIn(8.5f, 14.8f).roundToSingleDecimal()

        // Like rate: healthy reels have high like rate (2.0% - 6.5%)
        val likeRate = ((28f / safeViews.coerceAtLeast(20)) * 100f).coerceIn(1.2f, 7.5f).roundToSingleDecimal()

        return HealthyMetricsResult(
            viewers = viewers,
            avgWatchTime = avgWatchTime,
            skipRate = skipRate,
            likeRate = likeRate
        )
    }

    data class HealthyMetricsResult(
        val viewers: Int,
        val avgWatchTime: String,
        val skipRate: Float,
        val likeRate: Float
    )

    private fun formatTimeLabel(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }

    private fun Float.roundToSingleDecimal(): Float {
        return (this * 10f).roundToInt() / 10f
    }
}
