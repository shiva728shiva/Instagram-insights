package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReelInsightsData
import com.example.ui.components.FrameScrubVideoPreview
import com.example.ui.components.IgIcons
import com.example.ui.components.ImpactMetricRow
import com.example.ui.components.InteractiveRetentionChart
import com.example.ui.components.InteractiveViewsOverTimeChart
import com.example.ui.components.MetricProgressBar
import com.example.ui.components.RollingNumberCounter
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShimmerBox
import com.example.ui.components.rememberShimmerBrush
import com.example.ui.theme.IgBorder
import com.example.ui.theme.IgCardBg
import com.example.ui.theme.IgChartTypicalLine
import com.example.ui.theme.IgDivider
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgPillActive
import com.example.ui.theme.IgTextFaint
import com.example.ui.theme.IgTextMuted
import com.example.ui.theme.IgTextPrimary
import com.example.ui.theme.IgTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OverviewTab(
    data: ReelInsightsData,
    loading: Boolean,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 40.dp)
    ) {
        // 1. Summary Section
        Text(
            text = "Summary",
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold,
            color = IgTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Summary 2x2 cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                label = "Views",
                value = numberFormat.format(data.views),
                loading = loading,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Viewers",
                value = numberFormat.format(data.viewers),
                loading = loading,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                label = "Average watch time",
                value = data.avgWatchTime,
                loading = loading,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Follows",
                value = data.follows.toString(),
                loading = loading,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Views over time Section
        SectionHeader(
            title = "Views over time",
            rightContent = {
                if (loading) {
                    ShimmerBox(width = 54.dp, height = 18.dp)
                } else {
                    RollingNumberCounter(
                        targetValue = data.views,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = IgTextPrimary
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills (All, Followers, Non-followers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Followers", "Non-followers").forEach { filter ->
                FilterPill(
                    label = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterSelect(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Views Chart with raw Y-axis values & tooltip
        if (loading) {
            ShimmerBox(height = 180.dp, borderRadius = 10.dp)
        } else {
            InteractiveViewsOverTimeChart(
                dataPoints = data.viewsOverTime,
                selectedFilter = selectedFilter
            )
        }

        // Bottom Date Labels: dynamically sourced from viewsOverTime data
        val firstDate = data.viewsOverTime.firstOrNull()?.dateLabel ?: "Aug 17"
        val midDate = data.viewsOverTime.getOrNull(data.viewsOverTime.size / 2)?.dateLabel ?: "Aug 23"
        val lastDate = data.viewsOverTime.lastOrNull()?.dateLabel ?: "Aug 28"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 38.dp, end = 16.dp, top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = firstDate, fontSize = 11.sp, color = IgTextFaint)
            Text(text = midDate, fontSize = 11.sp, color = IgTextFaint)
            Text(text = lastDate, fontSize = 11.sp, color = IgTextFaint)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chart Legend (This reel vs Your typical reel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(IgMagenta)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "This reel", fontSize = 12.sp, color = IgTextSecondary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(IgChartTypicalLine)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Your typical reel", fontSize = 12.sp, color = IgTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3. What impacts your views Section
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "What impacts your views",
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                color = IgTextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Info",
                tint = IgTextMuted,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = "Rates are listed in order of importance to reach.",
            fontSize = 13.sp,
            color = IgTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        ImpactMetricRow(
            iconRes = IgIcons.skipRate,
            label = "Skip rate",
            value = data.skipRate,
            qualifier = data.skipRateQualifier,
            loading = loading
        )
        ImpactMetricRow(
            iconRes = IgIcons.shareRate,
            label = "Share rate",
            value = data.shareRate,
            qualifier = data.shareRateQualifier,
            loading = loading
        )
        ImpactMetricRow(
            iconRes = IgIcons.likeRate,
            label = "Like rate",
            value = data.likeRate,
            qualifier = data.likeRateQualifier,
            loading = loading
        )
        ImpactMetricRow(
            iconRes = IgIcons.saveRate,
            label = "Save rate",
            value = data.saveRate,
            qualifier = data.saveRateQualifier,
            loading = loading
        )
        ImpactMetricRow(
            iconRes = IgIcons.repostRate,
            label = "Repost rate",
            value = data.repostRate,
            qualifier = data.repostRateQualifier,
            loading = loading
        )
        ImpactMetricRow(
            iconRes = IgIcons.commentRate,
            label = "Comment rate",
            value = data.commentRate,
            qualifier = data.commentRateQualifier,
            loading = loading
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4. How long people watched your reel
        SectionHeader(title = "How long people watched your reel")

        Spacer(modifier = Modifier.height(14.dp))

        var selectedRetentionIndex by remember(data.retention) { mutableStateOf<Int?>(null) }
        var isRetentionScrubbing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        var playIconDelayJob by remember { mutableStateOf<Job?>(null) }
        val activeRetentionPoint = selectedRetentionIndex?.let { data.retention.getOrNull(it) }
        val scrubTimeLabel = activeRetentionPoint?.timeLabel ?: "0:00"

        // Video Thumbnail Preview with Play Icon (9:16 Portrait Dimension) & Frame-Synced Scrubbing
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FrameScrubVideoPreview(
                videoUri = data.videoUri,
                thumbnailUrl = data.thumbnailUrl,
                scrubSecond = selectedRetentionIndex ?: 0,
                timeLabel = scrubTimeLabel,
                showPlayIcon = !isRetentionScrubbing
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (loading) {
            ShimmerBox(height = 160.dp, borderRadius = 10.dp)
        } else {
            InteractiveRetentionChart(
                retentionPoints = data.retention,
                selectedIndex = selectedRetentionIndex,
                onIndexChange = { newIndex ->
                    selectedRetentionIndex = newIndex
                },
                onInteractingChange = { isInteracting ->
                    playIconDelayJob?.cancel()
                    if (isInteracting) {
                        isRetentionScrubbing = true
                    } else {
                        // Keep play icon hidden for 2 seconds after user releases touch
                        playIconDelayJob = coroutineScope.launch {
                            delay(2000L)
                            isRetentionScrubbing = false
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Top sources of views
        SectionHeader(title = "Top sources of views")
        Spacer(modifier = Modifier.height(8.dp))

        MetricProgressBar(label = "Reels tab", percent = data.reelsTabPct, loading = loading)
        MetricProgressBar(label = "Explore", percent = data.explorePct, loading = loading)
        MetricProgressBar(label = "Profile", percent = data.profilePct, loading = loading)
        MetricProgressBar(label = "Feed", percent = data.feedPct, loading = loading)

        Spacer(modifier = Modifier.height(28.dp))

        // 6. Ad Section
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
fun SummaryCard(
    label: String,
    value: String,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    if (loading) {
        val shimmerBrush = rememberShimmerBrush()
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(shimmerBrush)
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .width(65.dp)
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2E2E32).copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF3A3A40).copy(alpha = 0.6f))
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(IgCardBg)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = IgTextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgTextPrimary
                )
            }
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) Color(0xFF2E323A) else Color(0xFF1F2228))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF454B57) else Color(0xFF282B32),
                shape = RoundedCornerShape(50)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
