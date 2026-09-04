package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ViewDataPoint
import com.example.ui.theme.IgChartGrid
import com.example.ui.theme.IgChartTypicalLine
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgTextFaint
import com.example.ui.theme.IgTextPrimary
import com.example.ui.theme.IgTooltipBg

@Composable
fun InteractiveViewsOverTimeChart(
    dataPoints: List<ViewDataPoint>,
    selectedFilter: String = "All",
    modifier: Modifier = Modifier
) {
    // By default, tooltip is hidden until user taps/drags on chart
    val maxValidIndex = remember(dataPoints) {
        val lastValid = dataPoints.indexOfLast { it.viewsThisReel >= 0f }
        if (lastValid >= 0) lastValid else (dataPoints.size - 1)
    }
    var selectedIndex by remember(dataPoints) {
        mutableIntStateOf(-1)
    }
    var isTouching by remember { mutableStateOf(false) }

    // Multiplier for filter (All: 1.0, Followers: 0.08, Non-followers: 0.92)
    val factor = when (selectedFilter) {
        "Followers" -> 0.08f
        "Non-followers" -> 0.92f
        else -> 1.0f
    }

    val animatedFactor by animateFloatAsState(
        targetValue = factor,
        animationSpec = tween(400),
        label = "filterAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("views_over_time_chart")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dataPoints, maxValidIndex) {
                    detectTapGestures { offset ->
                        val leftPadding = 110f
                        val rightPadding = 50f
                        val chartWidth = size.width - leftPadding - rightPadding
                        val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                        val step = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)
                        val index = (relX / step).toInt().coerceIn(0, maxValidIndex)
                        selectedIndex = index
                        isTouching = true
                    }
                }
                .pointerInput(dataPoints, maxValidIndex) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val leftPadding = 110f
                            val rightPadding = 50f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, maxValidIndex)
                            selectedIndex = index
                            isTouching = true
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val leftPadding = 110f
                            val rightPadding = 50f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (change.position.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, maxValidIndex)
                            selectedIndex = index
                            isTouching = true
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val leftPadding = 110f
            val rightPadding = 50f
            val topPadding = 50f
            val bottomPadding = 45f

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            // Dynamically calculate maxY and levels from the data points
            val maxDataVal = (dataPoints.maxOfOrNull { maxOf(it.viewsThisReel, it.viewsTypical) } ?: 400f).coerceAtLeast(10f)
            val maxY = calculateNiceMax(maxDataVal)
            val midY = maxY / 2f
            val yLevels = listOf(maxY, midY, 0f)

            // 1. Draw horizontal grid lines and dynamic Y-axis numbers
            yLevels.forEach { yVal ->
                val yPos = topPadding + (1f - (yVal / maxY)) * chartHeight

                // Grid line
                drawLine(
                    color = IgChartGrid,
                    start = Offset(leftPadding - 15f, yPos),
                    end = Offset(width - rightPadding, yPos),
                    strokeWidth = 1.2.dp.toPx()
                )

                // Dynamic Y-axis label text
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#8E8E93")
                        textSize = 28f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    val formattedLabel = formatAxisNumber(yVal)
                    drawText(formattedLabel, leftPadding - 20f, yPos + 9f, paint)
                }
            }

            if (dataPoints.isEmpty()) return@Canvas

            val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

            // Function to map data point to Canvas Coordinates
            fun getCoordinates(index: Int, rawValue: Float): Offset {
                val clamped = if (rawValue < 0f) 0f else rawValue
                val value = (clamped * animatedFactor).coerceIn(0f, maxY)
                val x = leftPadding + index * stepX
                val y = topPadding + (1f - (value / maxY)) * chartHeight
                return Offset(x, y)
            }

            // 2. Draw "Your typical reel" line (dashed gray) with smooth curves
            val typicalPoints = dataPoints.mapIndexed { i, dp ->
                getCoordinates(i, dp.viewsTypical)
            }
            drawSmoothLine(
                points = typicalPoints,
                color = IgChartTypicalLine,
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )

            // 3. Draw "This reel" line (vibrant magenta/pink) - only up to valid data points
            val validThisReelPoints = dataPoints.mapIndexedNotNull { i, dp ->
                if (dp.viewsThisReel >= 0f) getCoordinates(i, dp.viewsThisReel) else null
            }
            drawSmoothLine(
                points = validThisReelPoints,
                color = IgMagenta,
                strokeWidth = 3.2.dp.toPx()
            )

            // 4. Draw Interactive Scrubber / Pointer Tooltip if user touched/scrubbed
            if (isTouching && selectedIndex in 0..maxValidIndex && dataPoints[selectedIndex].viewsThisReel >= 0f) {
                val activePoint = dataPoints[selectedIndex]
                val coords = getCoordinates(selectedIndex, activePoint.viewsThisReel)

                val rawVal = (activePoint.viewsThisReel * animatedFactor).toInt()
                val displayViews = String.format(java.util.Locale.US, "%,d", rawVal)
                val dateLabel = activePoint.dateLabel

                val pViews = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    isFakeBoldText = true
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val pDate = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#CCCCCC")
                    textSize = 22f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val textWidth = pViews.measureText(displayViews)
                val bubbleWidth = maxOf(110f, textWidth + 36f)
                val bubbleHeight = 38.dp.toPx()
                val bubbleX = (coords.x - bubbleWidth / 2).coerceIn(leftPadding - 10.dp.toPx(), width - bubbleWidth - 10.dp.toPx())
                val bubbleY = 4.dp.toPx()

                // Vertical dashed guide line connecting from bottom of tooltip down to 0 baseline
                drawLine(
                    color = Color.White.copy(alpha = 0.55f),
                    start = Offset(coords.x, bubbleY + bubbleHeight),
                    end = Offset(coords.x, topPadding + chartHeight),
                    strokeWidth = 1.3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.5.dp.toPx()), 0f)
                )

                // Tooltip background pill
                drawRoundRect(
                    color = Color(0xFF1E232B),
                    topLeft = Offset(bubbleX, bubbleY),
                    size = Size(bubbleWidth, bubbleHeight),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )

                // Draw 2 lines of text
                drawContext.canvas.nativeCanvas.apply {
                    drawText(displayViews, bubbleX + bubbleWidth / 2, bubbleY + 16.5.dp.toPx(), pViews)
                    drawText(dateLabel, bubbleX + bubbleWidth / 2, bubbleY + 31.5.dp.toPx(), pDate)
                }
            }
        }
    }
}

private fun calculateNiceMax(maxValue: Float): Float {
    if (maxValue <= 0f) return 100f
    val exp = kotlin.math.floor(kotlin.math.log10(maxValue.toDouble()))
    val magnitude = Math.pow(10.0, exp).toFloat()
    val normalized = maxValue / magnitude
    val niceNormalized = when {
        normalized <= 1.0f -> 1.0f
        normalized <= 1.5f -> 1.5f
        normalized <= 2.0f -> 2.0f
        normalized <= 2.5f -> 2.5f
        normalized <= 3.0f -> 3.0f
        normalized <= 4.0f -> 4.0f
        normalized <= 5.0f -> 5.0f
        normalized <= 6.0f -> 6.0f
        normalized <= 8.0f -> 8.0f
        else -> 10.0f
    }
    return (niceNormalized * magnitude).coerceAtLeast(10f)
}

private fun formatAxisNumber(value: Float): String {
    val intVal = value.toInt()
    return when {
        value >= 1_000_000f -> {
            if (value % 1_000_000f == 0f) "${(value / 1_000_000f).toInt()}M"
            else String.format(java.util.Locale.US, "%.1fM", value / 1_000_000f)
        }
        value >= 10_000f -> {
            if (value % 1000f == 0f) "${(value / 1000f).toInt()}K"
            else String.format(java.util.Locale.US, "%.1fK", value / 1000f)
        }
        value >= 1_000f -> {
            if (value % 1000f == 0f) "${(value / 1000f).toInt()}K"
            else if ((value % 100f) == 0f) String.format(java.util.Locale.US, "%.1fK", value / 1000f)
            else String.format(java.util.Locale.US, "%,d", intVal)
        }
        else -> intVal.toString()
    }
}

