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
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Reel insights",
                fontSize = 17.5.sp,
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
                        modifier = Modifier.size(22.dp)
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
                        modifier = Modifier.size(22.dp)
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
            // Mini Reel Thumbnail Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(118.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Column {
                        Text(
                            text = data.caption,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111111),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 11.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF321A3B), Color(0xFF1F1A2A), Color(0xFF121214))
                                    )
                                )
                        )
                    }
                }
            }

            // Quick Stats Icon Row (Likes, Comments, Reshares, Sends, Saves)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickStatItem(icon = Icons.Outlined.FavoriteBorder, count = data.likes.toString())
                QuickStatItem(icon = Icons.Outlined.ChatBubbleOutline, count = data.comments.toString())
                QuickStatItem(icon = Icons.Default.Repeat, count = data.reshares.toString())
                QuickStatItem(icon = Icons.AutoMirrored.Filled.Send, count = data.sends.toString())
                QuickStatItem(icon = Icons.Outlined.BookmarkBorder, count = data.saves.toString())
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Bar Row (Overview, Engagement, Audience)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                InsightsTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelect(tab) }
                            )
                            .padding(vertical = 12.dp)
                            .testTag("tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else IgTextMuted
                            )
                        }
                    }
                }
            }

            // Active Tab Indicator Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(IgBorder)
            ) {
                val tabIndex = currentTab.ordinal
                val totalTabs = InsightsTab.entries.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f / totalTabs)
                        .padding(start = (tabIndex * (360 / totalTabs)).dp) // visual indicator alignment
                        .height(2.dp)
                        .background(Color.White)
                )
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
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = IgTextPrimary
        )
    }
}
