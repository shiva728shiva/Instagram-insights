package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReelInsightsData
import com.example.ui.components.InteractiveWhenLikedChart
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.IgCardBg
import com.example.ui.theme.IgDivider
import com.example.ui.theme.IgTextFaint
import com.example.ui.theme.IgTextPrimary

@Composable
fun EngagementTab(
    data: ReelInsightsData,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 40.dp)
    ) {
        // 1. Actions after viewing (Comes FIRST)
        SectionHeader(title = "Actions after viewing")
        Spacer(modifier = Modifier.height(14.dp))

        EngagementTextRow(
            label = "Follows",
            count = data.follows,
            loading = loading
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = IgDivider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))

        // 2. Interactions section (Comes SECOND)
        SectionHeader(title = "Interactions")
        Spacer(modifier = Modifier.height(14.dp))

        EngagementTextRow(
            label = "Likes",
            count = data.likes,
            loading = loading
        )
        EngagementTextRow(
            label = "Comments",
            count = data.comments,
            loading = loading
        )
        EngagementTextRow(
            label = "Reposts",
            count = data.reshares,
            loading = loading
        )
        EngagementTextRow(
            label = "Shares",
            count = data.sends,
            loading = loading
        )
        EngagementTextRow(
            label = "Saves",
            count = data.saves,
            loading = loading
        )

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider(color = IgDivider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // 3. When people liked your reel (Comes THIRD with thumbnail and pink line chart)
        SectionHeader(title = "When people liked your reel")
        Spacer(modifier = Modifier.height(14.dp))

        // Centered Video Thumbnail
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF38153A), Color(0xFF20162A), Color(0xFF0C0A10))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            ShimmerBox(height = 150.dp, borderRadius = 10.dp)
        } else {
            InteractiveWhenLikedChart(likePoints = data.whenLiked)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0:00", fontSize = 11.sp, color = IgTextFaint)
            Text(text = "0:11", fontSize = 11.sp, color = IgTextFaint)
        }
    }
}

@Composable
fun EngagementTextRow(
    label: String,
    count: Int,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Normal,
            color = IgTextPrimary
        )

        if (loading) {
            ShimmerBox(width = 24.dp, height = 16.dp)
        } else {
            Text(
                text = count.toString(),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Normal,
                color = IgTextPrimary
            )
        }
    }
}
