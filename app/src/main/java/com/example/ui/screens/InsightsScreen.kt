package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReelInsightsData
import com.example.ui.components.IgIcons
import com.example.ui.theme.IgBackground
import com.example.ui.theme.IgDivider
import com.example.ui.theme.IgTextMuted
import com.example.ui.theme.IgTextPrimary
import com.example.viewmodel.InsightsTab

@OptIn(ExperimentalFoundationApi::class)
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IgBackground)
            .statusBarsPadding()
    ) {
        // Top App Bar - Fixed (Title left-aligned next to back button matching Real Instagram)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
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

            Spacer(modifier = Modifier.width(6.dp))

            // Left-aligned title
            Text(
                text = "Reel insights",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = IgTextPrimary,
                modifier = Modifier.weight(1f)
            )

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

        // Scrollable Insights Content with Sticky Tabs Header
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "thumbnail") {
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
                    ) {
                        if (data.thumbnailUrl.isNotBlank()) {
                            AsyncImage(
                                model = data.thumbnailUrl,
                                contentDescription = "Reel Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            item(key = "quick_stats") {
                // Quick Stats Icon Row (Likes, Comments, Reshares, Sends, Saves)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickStatItem(iconRes = IgIcons.likeRate, count = data.likes.toString())
                    QuickStatItem(iconRes = IgIcons.commentRate, count = data.comments.toString())
                    QuickStatItem(iconRes = IgIcons.repostRate, count = data.reshares.toString())
                    QuickStatItem(iconRes = IgIcons.shareRate, count = data.sends.toString())
                    QuickStatItem(iconRes = IgIcons.saveRate, count = data.saves.toString())
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Sticky Header: Tabs (Overview, Engagement, Audience) stay pinned under top bar on scroll
            stickyHeader(key = "tabs_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IgBackground) // Opaque so underlying scrolled content is covered
                ) {
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
                                    color = if (isSelected) IgTextPrimary else IgTextMuted,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                                // White active tab underline (thin and crisp matching Real Instagram)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.5.dp)
                                        .background(if (isSelected) IgTextPrimary else Color.Transparent)
                                )
                            }
                        }
                    }
                    // Divider below tabs
                    HorizontalDivider(color = IgDivider, thickness = 1.dp)
                }
            }

            // Tab Content
            item(key = "tab_content_${currentTab.name}") {
                Spacer(modifier = Modifier.height(18.dp))
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
}

@Composable
fun QuickStatItem(
    @DrawableRes iconRes: Int,
    count: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconRes),
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
