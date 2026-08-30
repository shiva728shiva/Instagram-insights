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
import com.example.ui.theme.IgChartGrid
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgTooltipBg

@Composable
fun InteractiveWhenLikedChart(
    likePoints: List<Float>,
    selectedIndex: Int? = null,
    onIndexChange: (Int) -> Unit = {},
    onInteractingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isTouching by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .testTag("when_liked_chart")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(likePoints) {
                    detectTapGestures(
                        onPress = { offset ->
                            isTouching = true
                            onInteractingChange(true)
                            val leftPadding = 40f
                            val rightPadding = 40f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (likePoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, likePoints.size - 1)
                            onIndexChange(index)
                            tryAwaitRelease()
                            isTouching = false
                            onInteractingChange(false)
                        }
                    )
                }
                .pointerInput(likePoints) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val leftPadding = 40f
                            val rightPadding = 40f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (likePoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, likePoints.size - 1)
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
                            val leftPadding = 40f
                            val rightPadding = 40f
                            val chartWidth = size.width - leftPadding - rightPadding
                            val relX = (change.position.x - leftPadding).coerceIn(0f, chartWidth)
                            val step = chartWidth / (likePoints.size - 1).coerceAtLeast(1)
                            val index = (relX / step).toInt().coerceIn(0, likePoints.size - 1)
                            isTouching = true
                            onInteractingChange(true)
                            onIndexChange(index)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val leftPadding = 40f
            val rightPadding = 40f
            val topPadding = 40f
            val bottomPadding = 30f

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            // 1. Horizontal grid lines
            listOf(0f, 0.5f, 1f).forEach { frac ->
                val yPos = topPadding + frac * chartHeight
                drawLine(
                    color = IgChartGrid,
                    start = Offset(leftPadding - 10f, yPos),
                    end = Offset(width - rightPadding + 10f, yPos),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            if (likePoints.isEmpty()) return@Canvas

            val stepX = chartWidth / (likePoints.size - 1).coerceAtLeast(1)

            fun getCoords(index: Int, raw: Float): Offset {
                val x = leftPadding + index * stepX
                val y = topPadding + (1f - (raw / 100f)) * chartHeight
                return Offset(x, y)
            }

            // 2. Draw line path with smooth Catmull-Rom/midpoint curve
            val points = likePoints.mapIndexed { i, raw -> getCoords(i, raw) }
            drawSmoothLine(
                points = points,
                color = IgMagenta,
                strokeWidth = 3.dp.toPx()
            )

            // 3. Draw active scrubber line & tooltip when user touches/scrubs
            val activeIdx = selectedIndex
            if (activeIdx != null && activeIdx in likePoints.indices && (isTouching || selectedIndex != null)) {
                val activeVal = likePoints[activeIdx]
                val coords = getCoords(activeIdx, activeVal)

                val bubbleWidth = 100f
                val bubbleHeight = 44f
                val bubbleX = (coords.x - bubbleWidth / 2).coerceIn(10f, width - bubbleWidth - 10f)
                val bubbleY = 4f

                // Vertical dashed guide line from tooltip down to bottom grid line
                drawLine(
                    color = Color(0xFF9E9EA4),
                    start = Offset(coords.x, bubbleY + bubbleHeight + 7f),
                    end = Offset(coords.x, topPadding + chartHeight),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )

                drawRoundRect(
                    color = IgTooltipBg,
                    topLeft = Offset(bubbleX, bubbleY),
                    size = Size(bubbleWidth, bubbleHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                val arrowPath = Path().apply {
                    val arrowCenterX = coords.x.coerceIn(bubbleX + 12f, bubbleX + bubbleWidth - 12f)
                    moveTo(arrowCenterX - 7f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX + 7f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX, bubbleY + bubbleHeight + 7f)
                    close()
                }
                drawPath(arrowPath, color = IgTooltipBg)

                val timeStr = "0:${activeIdx.toString().padStart(2, '0')}"
                drawContext.canvas.nativeCanvas.apply {
                    val p = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 26f
                        isFakeBoldText = true
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(timeStr, bubbleX + bubbleWidth / 2, bubbleY + 28f, p)
                }
            }
        }
    }
}
