package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.example.data.model.RetentionPoint
import com.example.ui.theme.IgChartGrid
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgTooltipBg

@Composable
fun InteractiveRetentionChart(
    retentionPoints: List<RetentionPoint>,
    selectedIndex: Int? = null,
    onIndexChange: (Int) -> Unit = {},
    onInteractingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isLineVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var hideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("retention_chart")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(retentionPoints) {
                    detectTapGestures(
                        onPress = { offset ->
                            hideJob?.cancel()
                            isLineVisible = true
                            onInteractingChange(true)
                            val leftPadding = 48.dp.toPx()
                            val rightPadding = 14.dp.toPx()
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                            onIndexChange(index)
                            tryAwaitRelease()
                            onInteractingChange(false)
                            hideJob = coroutineScope.launch {
                                delay(2500L)
                                isLineVisible = false
                            }
                        }
                    )
                }
                .pointerInput(retentionPoints) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            hideJob?.cancel()
                            isLineVisible = true
                            val leftPadding = 48.dp.toPx()
                            val rightPadding = 14.dp.toPx()
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                            onInteractingChange(true)
                            onIndexChange(index)
                        },
                        onDragEnd = {
                            onInteractingChange(false)
                            hideJob = coroutineScope.launch {
                                delay(2500L)
                                isLineVisible = false
                            }
                        },
                        onDragCancel = {
                            onInteractingChange(false)
                            hideJob = coroutineScope.launch {
                                delay(2500L)
                                isLineVisible = false
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            hideJob?.cancel()
                            isLineVisible = true
                            val leftPadding = 48.dp.toPx()
                            val rightPadding = 14.dp.toPx()
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (change.position.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                            onInteractingChange(true)
                            onIndexChange(index)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val leftPadding = 48.dp.toPx()
            val rightPadding = 14.dp.toPx()
            val topPadding = 34.dp.toPx()
            val bottomPadding = 30.dp.toPx()

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#9E9EA4")
                textSize = 12.sp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.LEFT
            }

            // 1. Horizontal grid lines & Y-axis percentage labels (100%, 50%, 0%)
            listOf(
                Triple(0f, "100%", topPadding),
                Triple(0.5f, "50%", topPadding + 0.5f * chartHeight),
                Triple(1f, "0%", topPadding + chartHeight)
            ).forEach { (_, label, yPos) ->
                // Draw Label on the left with clean spacing from gridline
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    4.dp.toPx(),
                    yPos + 4.dp.toPx(),
                    textPaint
                )

                // Draw Gridline
                drawLine(
                    color = IgChartGrid,
                    start = Offset(leftPadding, yPos),
                    end = Offset(width - rightPadding, yPos),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            if (retentionPoints.isEmpty()) return@Canvas

            // Bottom time labels (e.g. 0:00 on left, 0:08 on right)
            val startTimeLabel = retentionPoints.firstOrNull()?.timeLabel ?: "0:00"
            val endTimeLabel = retentionPoints.lastOrNull()?.timeLabel ?: "0:08"

            val timeLabelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#9E9EA4")
                textSize = 12.sp.toPx()
                isAntiAlias = true
            }

            timeLabelPaint.textAlign = android.graphics.Paint.Align.LEFT
            drawContext.canvas.nativeCanvas.drawText(
                startTimeLabel,
                leftPadding,
                topPadding + chartHeight + 20.dp.toPx(),
                timeLabelPaint
            )

            timeLabelPaint.textAlign = android.graphics.Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText(
                endTimeLabel,
                width - rightPadding,
                topPadding + chartHeight + 20.dp.toPx(),
                timeLabelPaint
            )

            val stepX = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)

            fun getCoords(index: Int, percent: Float): Offset {
                val x = leftPadding + index * stepX
                val y = topPadding + (1f - (percent / 100f)) * chartHeight
                return Offset(x, y)
            }

            // 2. Draw smooth Retention line (Instagram Magenta)
            val points = retentionPoints.mapIndexed { i, p -> getCoords(i, p.percent) }
            drawSmoothLine(
                points = points,
                color = IgMagenta,
                strokeWidth = 2.8.dp.toPx()
            )

            // 3. Draw active scrubber vertical line & tooltip when user is touching/scrubbing (and stays visible for a moment)
            val activeIdx = selectedIndex
            if (isLineVisible && activeIdx != null && activeIdx in retentionPoints.indices) {
                val activePoint = retentionPoints[activeIdx]
                val coords = getCoords(activeIdx, activePoint.percent)

                val bubbleWidth = 56.dp.toPx()
                val bubbleHeight = 38.dp.toPx()
                val bubbleX = (coords.x - bubbleWidth / 2).coerceIn(leftPadding, width - rightPadding - bubbleWidth)
                val bubbleY = 2.dp.toPx()

                // Simple vertical dashed guide line - NO pink point at intersection
                drawLine(
                    color = Color.White.copy(alpha = 0.55f),
                    start = Offset(coords.x, bubbleY + bubbleHeight),
                    end = Offset(coords.x, topPadding + chartHeight),
                    strokeWidth = 1.3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.5.dp.toPx()), 0f)
                )

                // Clean dark rounded rectangular pill tooltip matching Instagram
                drawRoundRect(
                    color = Color(0xFF1E232B),
                    topLeft = Offset(bubbleX, bubbleY),
                    size = Size(bubbleWidth, bubbleHeight),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )

                // Tooltip text: Percentage on top (bold white), Time on bottom (muted gray)
                val percentText = "${activePoint.percent.toInt()}%"
                val timeText = activePoint.timeLabel

                drawContext.canvas.nativeCanvas.apply {
                    val p1 = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 13.5.sp.toPx()
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val p2 = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#9AA0A6")
                        textSize = 11.sp.toPx()
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    drawText(percentText, bubbleX + bubbleWidth / 2, bubbleY + 16.5.dp.toPx(), p1)
                    drawText(timeText, bubbleX + bubbleWidth / 2, bubbleY + 31.5.dp.toPx(), p2)
                }
            }
        }
    }
}
