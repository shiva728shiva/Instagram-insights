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
    var isTouching by remember { mutableStateOf(false) }

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
                            isTouching = true
                            onInteractingChange(true)
                            val leftPadding = 120f
                            val rightPadding = 24f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                            onIndexChange(index)
                            tryAwaitRelease()
                            isTouching = false
                            onInteractingChange(false)
                        }
                    )
                }
                .pointerInput(retentionPoints) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val leftPadding = 120f
                            val rightPadding = 24f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                            isTouching = true
                            onInteractingChange(true)
                            onIndexChange(index)
                        },
                        onDragEnd = {
                            isTouching = false
                            onInteractingChange(false)
                        },
                        onDragCancel = {
                            isTouching = false
                            onInteractingChange(false)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val leftPadding = 120f
                            val rightPadding = 24f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (change.position.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                            isTouching = true
                            onInteractingChange(true)
                            onIndexChange(index)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val leftPadding = 120f // Generous space so line starts comfortably away from 100%
            val rightPadding = 24f
            val topPadding = 45f
            val bottomPadding = 40f

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#9E9EA4")
                textSize = 28f
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
                    8f,
                    yPos + 8f,
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

            val stepX = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)

            fun getCoords(index: Int, percent: Float): Offset {
                val x = leftPadding + index * stepX
                val y = topPadding + (1f - (percent / 100f)) * chartHeight
                return Offset(x, y)
            }

            // 2. Draw smooth Retention line (Instagram Magenta) using drawSmoothLine
            val points = retentionPoints.mapIndexed { i, p -> getCoords(i, p.percent) }
            drawSmoothLine(
                points = points,
                color = IgMagenta,
                strokeWidth = 3.dp.toPx()
            )

            // 3. Draw active scrubber line & tooltip when user touches/scrubs
            val activeIdx = selectedIndex
            if (activeIdx != null && activeIdx in retentionPoints.indices && (isTouching || selectedIndex != null)) {
                val activePoint = retentionPoints[activeIdx]
                val coords = getCoords(activeIdx, activePoint.percent)

                val bubbleWidth = 114f
                val bubbleHeight = 60f
                val bubbleX = (coords.x - bubbleWidth / 2).coerceIn(10f, width - bubbleWidth - 10f)
                val bubbleY = 4f

                // Vertical dashed guide line from tooltip pointer down to 0% line
                drawLine(
                    color = Color(0xFF9E9EA4),
                    start = Offset(coords.x, bubbleY + bubbleHeight + 8f),
                    end = Offset(coords.x, topPadding + chartHeight),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )

                // Tooltip background rounded rectangle
                drawRoundRect(
                    color = IgTooltipBg,
                    topLeft = Offset(bubbleX, bubbleY),
                    size = Size(bubbleWidth, bubbleHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Pointer triangle pointing straight down to dashed line
                val arrowPath = Path().apply {
                    val arrowCenterX = coords.x.coerceIn(bubbleX + 14f, bubbleX + bubbleWidth - 14f)
                    moveTo(arrowCenterX - 7f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX + 7f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX, bubbleY + bubbleHeight + 8f)
                    close()
                }
                drawPath(arrowPath, color = IgTooltipBg)

                // Draw text in tooltip (Percentage on top, Time on bottom)
                val percentText = "${activePoint.percent.toInt()}%"
                val timeText = activePoint.timeLabel

                drawContext.canvas.nativeCanvas.apply {
                    val p1 = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f
                        isFakeBoldText = true
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val p2 = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#B0B0B4")
                        textSize = 22f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    drawText(percentText, bubbleX + bubbleWidth / 2, bubbleY + 26f, p1)
                    drawText(timeText, bubbleX + bubbleWidth / 2, bubbleY + 50f, p2)
                }
            }
        }
    }
}
