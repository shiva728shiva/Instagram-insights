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
     * Correlates directly with [totalViews] and [viewersCount].
     * Follows Instagram's healthy viral distribution curve:
     * - Day 0: 0 views (release moment)
     * - Day 1: 18%-24% initial audience velocity
     * - Day 2: 52%-65% algorithmic push (Explore / Reels tab breakout)
     * - Day 3-4: 75%-86% steady cumulative climb
     * - Day 5-7: 92%-98.5% asymptotic approach to total
     * - Day 8-9: Exactly 100% of total views
     * - Day 10-11: -1f (pending future reporting)
     *
     * Typical curve follows account baseline (~30%-38% of total peak)
     */
    fun generateViewsOverTime(
        totalViews: Int,
        viewersCount: Int = (totalViews * 0.74).toInt(),
        typicalRatio: Float = 0.34f,
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

        // Percentage milestones along the 12-day lifecycle for a healthy viral reel
        val cumulativePct = listOf(
            0.0f,     // Day 0: 0 views
            0.20f,    // Day 1: 20%
            0.58f,    // Day 2: 58% (breakout)
            0.75f,    // Day 3: 75%
            0.85f,    // Day 4: 85%
            0.92f,    // Day 5: 92%
            0.96f,    // Day 6: 96%
            0.985f,   // Day 7: 98.5%
            1.0f,     // Day 8: Exactly 100% of total views
            1.0f,     // Day 9: Exactly 100% of total views
            -1.0f,    // Day 10: -1 (future / unreported)
            -1.0f     // Day 11: -1 (future / unreported)
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
     * Accurately reflects real Instagram healthy metrics:
     * - Strictly starts at 100.0% at 0:00
     * - First 1-2 seconds (Hook): High retention (94%-97%), gentle shoulder
     * - Mid-duration: Smooth monotonic decline shaped by average watch time and replay ratio
     * - Tail: Healthy completion/loop rate (25%-42%)
     */
    fun generateRetentionCurve(
        durationSeconds: Int = 8,
        totalViews: Int = 1379,
        viewersCount: Int = 1020,
        avgWatchTimeSec: Float = 6.8f,
        skipRatePercent: Float = 11.2f
    ): List<RetentionPoint> {
        val totalSec = durationSeconds.coerceIn(4, 90)
        val safeViews = totalViews.coerceAtLeast(1)
        val safeViewers = viewersCount.coerceAtLeast(1)
        val replayRatio = (safeViews.toFloat() / safeViewers.toFloat()).coerceIn(1.0f, 2.5f)

        // End completion floor (healthy reels maintain 25%-42% through the loop)
        val endFloor = (22f + (replayRatio - 1.0f) * 22f + (if (safeViews > 3000) 5f else 0f)).coerceIn(24f, 42f)
        val normalizedWatch = (avgWatchTimeSec / totalSec.toFloat()).coerceIn(0.40f, 0.90f)

        val points = ArrayList<Float>(totalSec + 1)
        points.add(100.0f) // 0:00 is strictly 100%

        if (totalSec >= 1) {
            val hook1 = (100.0f - (2.6f + (skipRatePercent * 0.10f))).coerceIn(93.5f, 98.0f)
            points.add(hook1)
        }
        if (totalSec >= 2) {
            val prev = points.last()
            val hook2 = (prev - (4.0f + (skipRatePercent * 0.12f))).coerceIn(86.0f, prev - 1.0f)
            points.add(hook2)
        }

        // For remaining seconds: smooth monotonic decay towards endFloor
        val startDecayVal = points.last()
        val remainingSecs = totalSec - (points.size - 1)
        if (remainingSecs > 0) {
            for (step in 1..remainingSecs) {
                val t = step.toFloat() / remainingSecs.toFloat()
                // Logistic sigmoid decay curve centered around the relative watch milestone
                val decay = 1f / (1f + exp(5.5f * (t - normalizedWatch)))
                val calc = endFloor + (startDecayVal - endFloor) * decay
                val prev = points.last()
                val finalVal = calc.coerceIn(endFloor, prev - 0.2f)
                points.add(finalVal)
            }
        }

        return (0..totalSec).map { sec ->
            val label = formatTimeLabel(sec)
            val pct = points.getOrElse(sec) { endFloor }.roundToSingleDecimal()
            RetentionPoint(label, pct)
        }
    }

    /**
     * Generates a "When people liked your reel" activity distribution curve.
     * Real Instagram pattern:
     * - Low starting activity (10%-16%) as viewers start watching
     * - Rises to a pronounced peak at 35%-50% of the video duration (punchline/hook payoff)
     * - Sustains strong plateau (72%-88%) through the second half
     */
    fun generateWhenLiked(
        durationSeconds: Int = 8,
        likesCount: Int = 28,
        totalViews: Int = 1379
    ): List<Float> {
        val count = (durationSeconds + 1).coerceIn(8, 30)
        val peakIndex = (count * 0.40f).toInt().coerceIn(2, count - 3)
        val likeRateRatio = (likesCount.toFloat() / totalViews.coerceAtLeast(1).toFloat()).coerceIn(0.01f, 0.15f)
        val tailFloor = (70f + likeRateRatio * 120f).coerceIn(68f, 88f)

        return (0 until count).map { i ->
            when {
                i == 0 -> 12.0f
                i == 1 -> 28.0f
                i < peakIndex -> {
                    val progress = (i - 1).toFloat() / (peakIndex - 1).toFloat()
                    (28.0f + (100.0f - 28.0f) * kotlin.math.sin(progress * (Math.PI / 2)).toFloat()).coerceIn(28f, 100f)
                }
                i == peakIndex -> 100.0f
                else -> {
                    val postProgress = (i - peakIndex).toFloat() / (count - 1 - peakIndex).toFloat()
                    val decay = kotlin.math.cos(postProgress * (Math.PI / 2)).toFloat()
                    (tailFloor + (100.0f - tailFloor) * decay).coerceIn(tailFloor, 100f)
                }
            }
        }
    }

    /**
     * Computes healthy Instagram metrics (viewers, average watch time, skip rate, like rate)
     * correlated realistically to total views and video duration.
     */
    fun computeHealthyMetrics(
        views: Int,
        likes: Int? = null,
        viewers: Int? = null,
        durationSeconds: Int = 8
    ): HealthyMetricsResult {
        val safeViews = views.coerceAtLeast(1)
        val durationSec = durationSeconds.coerceIn(3, 90)

        // Viewers (Unique accounts reached): typically 68% - 82% of total views due to loops/re-watches
        val resultViewers = viewers ?: (safeViews * 0.74).toInt().coerceAtLeast(1)

        // Average watch time: healthy reels achieve 70% to 110% of video length
        val avgWatchSec = ((durationSec * 0.85).toInt()).coerceAtLeast(3)
        val avgWatchTime = "${avgWatchSec}s"

        // Skip rate: healthy reels have lower skip rate (< 14%)
        val skipRate = (10.8f + (600f / (safeViews + 300f))).coerceIn(8.5f, 13.8f).roundToSingleDecimal()

        // Like rate: healthy reels have high like rate (2.0% - 6.5%)
        val resultLikes = likes ?: (safeViews * 0.042f).roundToInt().coerceAtLeast(1)
        val likeRate = ((resultLikes.toFloat() / safeViews.toFloat()) * 100f).coerceIn(1.2f, 8.5f).roundToSingleDecimal()

        return HealthyMetricsResult(
            viewers = resultViewers,
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
