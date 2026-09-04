package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReelInsightsData
import com.example.ui.components.FrameScrubVideoPreview
import com.example.ui.components.InteractiveWhenLikedChart
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.IgTextFaint
import com.example.ui.theme.IgTextMuted
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

        // Spacing-only separation without divider lines
        Spacer(modifier = Modifier.height(28.dp))

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

        // Spacing-only separation without divider lines
        Spacer(modifier = Modifier.height(28.dp))

        // 3. When people liked your reel (Comes THIRD with thumbnail and pink line chart)
        SectionHeader(title = "When people liked your reel")
        Spacer(modifier = Modifier.height(14.dp))

        var selectedLikeIndex by remember(data.whenLiked) { mutableIntStateOf(0) }
        var isLikeScrubbing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        var playIconDelayJob by remember { mutableStateOf<Job?>(null) }
        val likeTimeLabel = com.example.data.VideoMediaManager.formatTimeLabel(selectedLikeIndex)

        // Centered Video Thumbnail (9:16 Portrait Dimension) & Frame-Synced Scrubbing
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FrameScrubVideoPreview(
                videoUri = data.videoUri,
                thumbnailUrl = data.thumbnailUrl,
                scrubSecond = selectedLikeIndex,
                timeLabel = likeTimeLabel,
                showPlayIcon = !isLikeScrubbing
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            ShimmerBox(height = 150.dp, borderRadius = 10.dp)
        } else {
            InteractiveWhenLikedChart(
                likePoints = data.whenLiked,
                selectedIndex = selectedLikeIndex,
                onIndexChange = { newIndex ->
                    selectedLikeIndex = newIndex
                },
                onInteractingChange = { isInteracting ->
                    playIconDelayJob?.cancel()
                    if (isInteracting) {
                        isLikeScrubbing = true
                    } else {
                        // Keep play icon hidden for 2 seconds after user releases touch
                        playIconDelayJob = coroutineScope.launch {
                            delay(2000L)
                            isLikeScrubbing = false
                        }
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0:00", fontSize = 12.sp, color = IgTextFaint)
            Text(
                text = com.example.data.VideoMediaManager.formatTimeLabel((data.whenLiked.size - 1).coerceAtLeast(1)),
                fontSize = 12.sp,
                color = IgTextFaint
            )
        }

        // Spacing-only separation
        Spacer(modifier = Modifier.height(28.dp))

        // 4. Ad Section -> Boost this reel
        Text(
            text = "Ad",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = IgTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = IgTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Boost this reel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = IgTextPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = IgTextMuted,
                modifier = Modifier.size(22.dp)
            )
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
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Normal,
            color = IgTextPrimary
        )

        if (loading) {
            ShimmerBox(width = 28.dp, height = 18.dp)
        } else {
            Text(
                text = count.toString(),
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Normal,
                color = IgTextPrimary
            )
        }
    }
}
