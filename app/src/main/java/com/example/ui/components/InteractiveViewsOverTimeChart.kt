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
    // Default selected index pointing to Aug 24 (index 7)
    val maxValidIndex = remember(dataPoints) {
        val lastValid = dataPoints.indexOfLast { it.viewsThisReel >= 0f }
        if (lastValid >= 0) lastValid else (dataPoints.size - 1)
    }
    var selectedIndex by remember(dataPoints) {
        val aug18Index = dataPoints.indexOfFirst { it.dateLabel == "Aug 18" }
        mutableIntStateOf(if (aug18Index >= 0) aug18Index else 1.coerceAtMost(maxValidIndex))
    }
    var isTouching by remember { mutableStateOf(true) }

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

            val maxY = 400f
            val yLevels = listOf(400, 200, 0)

            // 1. Draw horizontal grid lines and exact raw Y-axis numbers (400, 200, 0)
            yLevels.forEach { yVal ->
                val yPos = topPadding + (1f - (yVal / maxY)) * chartHeight

                // Grid line
                drawLine(
                    color = IgChartGrid,
                    start = Offset(leftPadding - 15f, yPos),
                    end = Offset(width - rightPadding, yPos),
                    strokeWidth = 1.2.dp.toPx()
                )

                // Raw Y-axis label text
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#8E8E93")
                        textSize = 30f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    drawText(yVal.toString(), leftPadding - 25f, yPos + 10f, paint)
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

            // 2. Draw "Your typical reel" line (dashed gray)
            val typicalPath = Path()
            dataPoints.forEachIndexed { i, dp ->
                val point = getCoordinates(i, dp.viewsTypical)
                if (i == 0) typicalPath.moveTo(point.x, point.y)
                else typicalPath.lineTo(point.x, point.y)
            }

            drawPath(
                path = typicalPath,
                color = IgChartTypicalLine,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // 3. Draw "This reel" line (vibrant magenta/pink) - only up to valid data points
            val thisReelPath = Path()
            var firstPoint = true
            dataPoints.forEachIndexed { i, dp ->
                if (dp.viewsThisReel >= 0f) {
                    val point = getCoordinates(i, dp.viewsThisReel)
                    if (firstPoint) {
                        thisReelPath.moveTo(point.x, point.y)
                        firstPoint = false
                    } else {
                        thisReelPath.lineTo(point.x, point.y)
                    }
                }
            }

            drawPath(
                path = thisReelPath,
                color = IgMagenta,
                style = Stroke(
                    width = 3.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 4. Draw Interactive Scrubber / Pointer Tooltip if active
            if (selectedIndex in 0..maxValidIndex && dataPoints[selectedIndex].viewsThisReel >= 0f) {
                val activePoint = dataPoints[selectedIndex]
                val coords = getCoordinates(selectedIndex, activePoint.viewsThisReel)

                // Vertical dashed guide line connecting from top/bottom to data point
                drawLine(
                    color = Color(0xFF6E6E73),
                    start = Offset(coords.x, topPadding - 10f),
                    end = Offset(coords.x, height - bottomPadding),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                // White glowing dot on the pink line
                drawCircle(
                    color = IgMagenta,
                    radius = 7.dp.toPx(),
                    center = coords
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = coords
                )

                // Tooltip bubble at the top: e.g. "359" (top) and "Aug 20" (bottom)
                val displayViews = (activePoint.viewsThisReel * animatedFactor).toInt().toString()
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

                val bubbleWidth = 110f
                val bubbleHeight = 62f
                val bubbleX = (coords.x - bubbleWidth / 2).coerceIn(10f, width - bubbleWidth - 10f)
                val bubbleY = 4f

                // Tooltip background pill
                drawRoundRect(
                    color = IgTooltipBg,
                    topLeft = Offset(bubbleX, bubbleY),
                    size = Size(bubbleWidth, bubbleHeight),
                    cornerRadius = CornerRadius(14f, 14f)
                )

                // Small arrow pointer triangle under the tooltip
                val arrowPath = Path().apply {
                    val arrowCenterX = coords.x.coerceIn(bubbleX + 14f, bubbleX + bubbleWidth - 14f)
                    moveTo(arrowCenterX - 8f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX + 8f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX, bubbleY + bubbleHeight + 8f)
                    close()
                }
                drawPath(arrowPath, color = IgTooltipBg)

                // Draw 2 lines of text
                drawContext.canvas.nativeCanvas.apply {
                    drawText(displayViews, bubbleX + bubbleWidth / 2, bubbleY + 26f, pViews)
                    drawText(dateLabel, bubbleX + bubbleWidth / 2, bubbleY + 50f, pDate)
                }
            }
        }
    }
}
