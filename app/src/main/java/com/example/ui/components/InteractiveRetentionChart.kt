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
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

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
                    detectTapGestures { offset ->
                        val leftPadding = 40f
                        val rightPadding = 40f
                        val chartWidth = size.width - leftPadding - rightPadding
                        val relX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                        val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                        val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                        selectedIndex = index
                    }
                }
                .pointerInput(retentionPoints) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val leftPadding = 40f
                        val rightPadding = 40f
                        val chartWidth = size.width - leftPadding - rightPadding
                        val relX = (change.position.x - leftPadding).coerceIn(0f, chartWidth)
                        val step = chartWidth / (retentionPoints.size - 1).coerceAtLeast(1)
                        val index = (relX / step).toInt().coerceIn(0, retentionPoints.size - 1)
                        selectedIndex = index
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val leftPadding = 40f
            val rightPadding = 40f
            val topPadding = 50f
            val bottomPadding = 30f

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            // 1. Horizontal grid lines (100%, 50%, 0%)
            listOf(0f, 0.5f, 1f).forEach { frac ->
                val yPos = topPadding + frac * chartHeight
                drawLine(
                    color = IgChartGrid,
                    start = Offset(leftPadding - 10f, yPos),
                    end = Offset(width - rightPadding + 10f, yPos),
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

            // 2. Draw smooth Retention line (Instagram Magenta)
            val path = Path()
            retentionPoints.forEachIndexed { i, p ->
                val point = getCoords(i, p.percent)
                if (i == 0) path.moveTo(point.x, point.y)
                else {
                    val prevPoint = getCoords(i - 1, retentionPoints[i - 1].percent)
                    val cx = (prevPoint.x + point.x) / 2
                    path.cubicTo(cx, prevPoint.y, cx, point.y, point.x, point.y)
                }
            }

            drawPath(
                path = path,
                color = IgMagenta,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 3. Draw active scrubber if selected
            if (selectedIndex in retentionPoints.indices) {
                val activePoint = retentionPoints[selectedIndex]
                val coords = getCoords(selectedIndex, activePoint.percent)

                // Vertical dashed guide line
                drawLine(
                    color = Color(0xFF6E6E73),
                    start = Offset(coords.x, topPadding - 10f),
                    end = Offset(coords.x, height - bottomPadding),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                // Glow and circle on point
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

                // Floating tooltip bubble: e.g. "92%\n0:02"
                val percentText = "${activePoint.percent.toInt()}%"
                val timeText = activePoint.timeLabel

                val bubbleWidth = 120f
                val bubbleHeight = 64f
                val bubbleX = (coords.x - bubbleWidth / 2).coerceIn(10f, width - bubbleWidth - 10f)
                val bubbleY = 4f

                drawRoundRect(
                    color = IgTooltipBg,
                    topLeft = Offset(bubbleX, bubbleY),
                    size = Size(bubbleWidth, bubbleHeight),
                    cornerRadius = CornerRadius(14f, 14f)
                )

                // Pointer triangle
                val arrowPath = Path().apply {
                    val arrowCenterX = coords.x.coerceIn(bubbleX + 14f, bubbleX + bubbleWidth - 14f)
                    moveTo(arrowCenterX - 8f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX + 8f, bubbleY + bubbleHeight)
                    lineTo(arrowCenterX, bubbleY + bubbleHeight + 8f)
                    close()
                }
                drawPath(arrowPath, color = IgTooltipBg)

                // Draw Text in bubble
                drawContext.canvas.nativeCanvas.apply {
                    val p1 = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f
                        isFakeBoldText = true
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val p2 = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#CCCCCC")
                        textSize = 22f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    drawText(percentText, bubbleX + bubbleWidth / 2, bubbleY + 28f, p1)
                    drawText(timeText, bubbleX + bubbleWidth / 2, bubbleY + 52f, p2)
                }
            }
        }
    }
}
