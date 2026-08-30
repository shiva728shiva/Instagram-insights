package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.exp
import kotlin.math.min

object GraphSplineUtils {

    /**
     * Professional Instagram Views Over Time S-Curve generator
     */
    fun generateViewsOverTimeCurve(
        totalViews: Int,
        totalLikes: Int,
        totalComments: Int,
        daysSpan: Int = 12,
        pointsPerDay: Int = 4
    ): List<Float> {
        val engagementRate = (totalLikes + totalComments * 2f) / totalViews.coerceAtLeast(1)
        val steepness = (4f + engagementRate * 40f).coerceIn(3f, 9f)
        val midpoint = daysSpan * 0.18f

        val totalPoints = daysSpan * pointsPerDay
        val rawPoints = (0 until totalPoints).map { i ->
            val t = i.toFloat() / pointsPerDay
            val logistic = 1f / (1f + exp(-steepness * (t - midpoint) / daysSpan))
            logistic * totalViews
        }

        return smoothCatmullRom(rawPoints, samplesPerSegment = 6)
    }

    /**
     * Professional Instagram Retention Decay Curve generator
     */
    fun generateRetentionCurve(
        videoDurationSec: Int,
        avgWatchTimeSec: Float,
        skipRatePercent: Float,
        points: Int = 40
    ): List<Float> {
        val dropPoint = (avgWatchTimeSec / videoDurationSec.coerceAtLeast(1)).coerceIn(0.1f, 0.9f)
        val dropSteepness = 8f + (skipRatePercent / 100f) * 20f

        val raw = (0 until points).map { i ->
            val t = i.toFloat() / points
            val decay = 1f / (1f + exp(dropSteepness * (t - dropPoint)))
            (0.85f + 0.15f * decay).coerceIn(0.05f, 1f) * 100f
        }
        return smoothCatmullRom(raw, samplesPerSegment = 4)
    }

    /**
     * Catmull-Rom Spline Interpolation for organic, ultra-smooth chart lines
     */
    fun smoothCatmullRom(points: List<Float>, samplesPerSegment: Int = 6): List<Float> {
        if (points.size < 4) return points
        val result = mutableListOf<Float>()
        for (i in 0 until points.size - 1) {
            val p0 = points[(i - 1).coerceAtLeast(0)]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = points[min(i + 2, points.size - 1)]

            for (s in 0 until samplesPerSegment) {
                val t = s / samplesPerSegment.toFloat()
                val t2 = t * t
                val t3 = t2 * t
                val value = 0.5f * (
                    (2f * p1) +
                    (-p0 + p2) * t +
                    (2f * p0 - 5f * p1 + 4f * p2 - p3) * t2 +
                    (-p0 + 3f * p1 - 3f * p2 + p3) * t3
                )
                result.add(value)
            }
        }
        result.add(points.last())
        return result
    }
}

/**
 * Mid-point quadratic smoothing for drawing paths on Canvas without sharp vertices
 */
fun DrawScope.drawSmoothLine(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float = 3.5f,
    pathEffect: PathEffect? = null
) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size - 1) {
            val mid = Offset(
                (points[i].x + points[i + 1].x) / 2f,
                (points[i].y + points[i + 1].y) / 2f
            )
            quadraticTo(points[i].x, points[i].y, mid.x, mid.y)
        }
        lineTo(points.last().x, points.last().y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = pathEffect
        )
    )
}
