package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MetricQualifier
import com.example.ui.theme.IgCardBg
import com.example.ui.theme.IgTextPrimary

@Composable
fun ImpactMetricRow(
    @DrawableRes iconRes: Int,
    label: String,
    value: Float,
    qualifier: MetricQualifier,
    loading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("impact_row_${label.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular icon container (Matching Real Instagram 48dp)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(IgCardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = IgTextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Metric Label
        Text(
            text = label,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Medium,
            color = IgTextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Percentage and Qualifier column on the right
        if (loading) {
            ShimmerBox(width = 54.dp, height = 20.dp)
        } else {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.1f%%", value),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgTextPrimary
                )
                Text(
                    text = qualifier.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = qualifier.color,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
