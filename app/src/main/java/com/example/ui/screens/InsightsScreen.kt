package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReelInsightsData
import com.example.ui.theme.IgBackground
import com.example.ui.theme.IgBorder
import com.example.ui.theme.IgDivider
import com.example.ui.theme.IgTextMuted
import com.example.ui.theme.IgTextPrimary
import com.example.ui.theme.IgTextSecondary
import com.example.viewmodel.InsightsTab

@Composable
fun InsightsScreen(
    data: ReelInsightsData,
    currentTab: InsightsTab,
    loading: Boolean,
    selectedViewsFilter: String,
    selectedAudienceSubTab: String,
    onTabSelect: (InsightsTab) -> Unit,
    onViewsFilterSelect: (String) -> Unit,
    onAudienceSubTabSelect: (String) -> Unit,
    onBackClick: () -> Unit,
    onOpenEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IgBackground)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = IgTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = "Reel insights",
                fontSize = 18.5.sp,
                fontWeight = FontWeight.Bold,
                color = IgTextPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenEditor,
                    modifier = Modifier.testTag("open_editor_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = "Customize Data",
                        tint = IgTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onOpenEditor,
                    modifier = Modifier.testTag("more_options_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = IgTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Scrollable Insights Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Reel Thumbnail Preview Box (Matching Real Instagram 9:16 Portrait Dimension)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .height(155.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF381A40), Color(0xFF22162E), Color(0xFF14111C))
                            )
                        )
                )
            }

            // Quick Stats Icon Row (Likes, Comments, Reshares, Sends, Saves)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickStatItem(icon = Icons.Outlined.FavoriteBorder, count = data.likes.toString())
                QuickStatItem(icon = Icons.Outlined.ChatBubbleOutline, count = data.comments.toString())
                QuickStatItem(icon = Icons.Default.Repeat, count = data.reshares.toString())
                QuickStatItem(icon = Icons.AutoMirrored.Filled.Send, count = data.sends.toString())
                QuickStatItem(icon = Icons.Outlined.BookmarkBorder, count = data.saves.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Bar Row (Overview, Engagement, Audience)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InsightsTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTabSelect(tab) }
                                )
                                .testTag("tab_${tab.name.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else IgTextMuted,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            // White active tab underline
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(if (isSelected) Color.White else Color.Transparent)
                            )
                        }
                    }
                }
                // Subtle gray divider line below tabs
                HorizontalDivider(color = IgDivider, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tab Content with Animated Transition
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabContentAnimation"
            ) { targetTab ->
                when (targetTab) {
                    InsightsTab.OVERVIEW -> {
                        OverviewTab(
                            data = data,
                            loading = loading,
                            selectedFilter = selectedViewsFilter,
                            onFilterSelect = onViewsFilterSelect
                        )
                    }
                    InsightsTab.ENGAGEMENT -> {
                        EngagementTab(
                            data = data,
                            loading = loading
                        )
                    }
                    InsightsTab.AUDIENCE -> {
                        AudienceTab(
                            data = data,
                            loading = loading,
                            subTab = selectedAudienceSubTab,
                            onSubTabSelect = onAudienceSubTabSelect
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatItem(
    icon: ImageVector,
    count: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IgTextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = count,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = IgTextPrimary
        )
    }
}
