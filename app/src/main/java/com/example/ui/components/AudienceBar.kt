package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IgBorder
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgTextPrimary
import com.example.ui.theme.IgTextSecondary

@Composable
fun MetricProgressBar(
    label: String,
    percent: Float,
    barColor: Color = IgMagenta,
    loading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "barProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Normal,
            color = IgTextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Track & Fill bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(IgBorder)
            ) {
                if (!loading && animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(barColor)
                    )
                }
            }

            // Percentage Text
            if (loading) {
                ShimmerBox(width = 42.dp, height = 14.dp)
            } else {
                Text(
                    text = String.format("%.1f%%", percent),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = IgTextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(52.dp)
                )
            }
        }
    }
}
