package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MetricQualifier
import com.example.data.model.ReelInsightsData
import com.example.ui.theme.IgBorder
import com.example.ui.theme.IgCardBg
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgPillActive
import com.example.ui.theme.IgTextPrimary
import com.example.ui.theme.IgTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorBottomSheet(
    data: ReelInsightsData,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (
        caption: String,
        handle: String,
        views: Int,
        viewers: Int,
        avgWatch: String,
        follows: Int,
        likes: Int,
        comments: Int,
        reshares: Int,
        sends: Int,
        saves: Int,
        skipRate: Float,
        skipQualifier: MetricQualifier,
        likeRate: Float,
        likeQualifier: MetricQualifier,
        shareRate: Float,
        saveRate: Float,
        repostRate: Float,
        commentRate: Float,
        reelsTabPct: Float,
        explorePct: Float,
        profilePct: Float,
        feedPct: Float,
        followersPct: Float,
        nonFollowersPct: Float,
        countryDemographics: Map<String, Float>,
        chartStartDate: String,
        chartMidDate: String,
        chartEndDate: String
    ) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var caption by remember(data) { mutableStateOf(data.caption) }
    var handle by remember(data) { mutableStateOf(data.handle) }
    var views by remember(data) { mutableStateOf(data.views.toString()) }
    var viewers by remember(data) { mutableStateOf(data.viewers.toString()) }
    var avgWatch by remember(data) { mutableStateOf(data.avgWatchTime) }
    var follows by remember(data) { mutableStateOf(data.follows.toString()) }

    var chartStartDate by remember(data) { mutableStateOf(data.viewsOverTime.firstOrNull()?.dateLabel ?: "Aug 17") }
    var chartMidDate by remember(data) { mutableStateOf(data.viewsOverTime.getOrNull(6)?.dateLabel ?: "Aug 23") }
    var chartEndDate by remember(data) { mutableStateOf(data.viewsOverTime.lastOrNull()?.dateLabel ?: "Aug 28") }

    var likes by remember(data) { mutableStateOf(data.likes.toString()) }
    var comments by remember(data) { mutableStateOf(data.comments.toString()) }
    var reshares by remember(data) { mutableStateOf(data.reshares.toString()) }
    var sends by remember(data) { mutableStateOf(data.sends.toString()) }
    var saves by remember(data) { mutableStateOf(data.saves.toString()) }

    var skipRate by remember(data) { mutableStateOf(data.skipRate.toString()) }
    var skipQualifier by remember(data) { mutableStateOf(data.skipRateQualifier) }
    var likeRate by remember(data) { mutableStateOf(data.likeRate.toString()) }
    var likeQualifier by remember(data) { mutableStateOf(data.likeRateQualifier) }
    var shareRate by remember(data) { mutableStateOf(data.shareRate.toString()) }
    var saveRate by remember(data) { mutableStateOf(data.saveRate.toString()) }
    var repostRate by remember(data) { mutableStateOf(data.repostRate.toString()) }
    var commentRate by remember(data) { mutableStateOf(data.commentRate.toString()) }

    var reelsTabPct by remember(data) { mutableStateOf(data.reelsTabPct.toString()) }
    var explorePct by remember(data) { mutableStateOf(data.explorePct.toString()) }
    var profilePct by remember(data) { mutableStateOf(data.profilePct.toString()) }
    var feedPct by remember(data) { mutableStateOf(data.feedPct.toString()) }

    var followersPct by remember(data) { mutableStateOf(data.followersAudiencePct.toString()) }
    var nonFollowersPct by remember(data) { mutableStateOf(data.nonFollowersAudiencePct.toString()) }

    var country1Name by remember(data) { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(0) ?: "India") }
    var country1Pct by remember(data) { mutableStateOf((data.countryDemographics.values.elementAtOrNull(0) ?: 44.0f).toString()) }
    var country2Name by remember(data) { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(1) ?: "United States") }
    var country2Pct by remember(data) { mutableStateOf((data.countryDemographics.values.elementAtOrNull(1) ?: 4.0f).toString()) }
    var country3Name by remember(data) { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(2) ?: "Brazil") }
    var country3Pct by remember(data) { mutableStateOf((data.countryDemographics.values.elementAtOrNull(2) ?: 4.0f).toString()) }
    var country4Name by remember(data) { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(3) ?: "United Kingdom") }
    var country4Pct by remember(data) { mutableStateOf((data.countryDemographics.values.elementAtOrNull(3) ?: 2.5f).toString()) }
    var country5Name by remember(data) { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(4) ?: "Canada") }
    var country5Pct by remember(data) { mutableStateOf((data.countryDemographics.values.elementAtOrNull(4) ?: 1.8f).toString()) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141418),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Insights Data",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgTextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onReset) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Defaults",
                            tint = IgTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = IgTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(480.dp)
                    .verticalScroll(scrollState)
            ) {
                // Post info
                SectionLabel("POST DETAILS")
                CustomTextField(value = caption, onValueChange = { caption = it }, label = "Caption")
                CustomTextField(value = handle, onValueChange = { handle = it }, label = "Handle")

                // Summary Numbers
                SectionLabel("SUMMARY METRICS")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = views, onValueChange = { views = it }, label = "Views", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = viewers, onValueChange = { viewers = it }, label = "Viewers", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = avgWatch, onValueChange = { avgWatch = it }, label = "Avg Watch Time", modifier = Modifier.weight(1f))
                    CustomTextField(value = follows, onValueChange = { follows = it }, label = "Follows", isNumber = true, modifier = Modifier.weight(1f))
                }

                // Action Bar Stats
                SectionLabel("ENGAGEMENT COUNTS")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = likes, onValueChange = { likes = it }, label = "Likes", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = comments, onValueChange = { comments = it }, label = "Comments", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = reshares, onValueChange = { reshares = it }, label = "Reposts", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = sends, onValueChange = { sends = it }, label = "Shares", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = saves, onValueChange = { saves = it }, label = "Saves", isNumber = true, modifier = Modifier.weight(1f))
                }

                // What impacts your views
                SectionLabel("WHAT IMPACTS YOUR VIEWS (%)")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = skipRate, onValueChange = { skipRate = it }, label = "Skip Rate %", isNumber = true, modifier = Modifier.weight(1f))
                    QualifierSelector(selected = skipQualifier, onSelect = { skipQualifier = it }, modifier = Modifier.weight(1.2f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = likeRate, onValueChange = { likeRate = it }, label = "Like Rate %", isNumber = true, modifier = Modifier.weight(1f))
                    QualifierSelector(selected = likeQualifier, onSelect = { likeQualifier = it }, modifier = Modifier.weight(1.2f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = shareRate, onValueChange = { shareRate = it }, label = "Share Rate %", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = saveRate, onValueChange = { saveRate = it }, label = "Save Rate %", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = repostRate, onValueChange = { repostRate = it }, label = "Repost Rate %", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = commentRate, onValueChange = { commentRate = it }, label = "Comment Rate %", isNumber = true, modifier = Modifier.weight(1f))
                }

                // Top Sources of Views
                SectionLabel("TOP SOURCES OF VIEWS (%)")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = reelsTabPct, onValueChange = { reelsTabPct = it }, label = "Reels tab %", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = explorePct, onValueChange = { explorePct = it }, label = "Explore %", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = profilePct, onValueChange = { profilePct = it }, label = "Profile %", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = feedPct, onValueChange = { feedPct = it }, label = "Feed %", isNumber = true, modifier = Modifier.weight(1f))
                }

                // Audience: Who viewed your reel (Ratio)
                SectionLabel("AUDIENCE: WHO VIEWED YOUR REEL (RATIO %)")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = followersPct, onValueChange = { followersPct = it }, label = "Followers %", isNumber = true, modifier = Modifier.weight(1f))
                    CustomTextField(value = nonFollowersPct, onValueChange = { nonFollowersPct = it }, label = "Non-followers %", isNumber = true, modifier = Modifier.weight(1f))
                }

                // Views Over Time Dates (Graph 1)
                SectionLabel("VIEWS OVER TIME GRAPH DATES")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = chartStartDate, onValueChange = { chartStartDate = it }, label = "Start Date", modifier = Modifier.weight(1f))
                    CustomTextField(value = chartMidDate, onValueChange = { chartMidDate = it }, label = "Mid Date", modifier = Modifier.weight(1f))
                    CustomTextField(value = chartEndDate, onValueChange = { chartEndDate = it }, label = "End Date", modifier = Modifier.weight(1f))
                }

                // Audience: Country Demographics (5 countries)
                SectionLabel("AUDIENCE: TOP COUNTRIES (%)")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = country1Name, onValueChange = { country1Name = it }, label = "Country 1", modifier = Modifier.weight(1.5f))
                    CustomTextField(value = country1Pct, onValueChange = { country1Pct = it }, label = "%", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = country2Name, onValueChange = { country2Name = it }, label = "Country 2", modifier = Modifier.weight(1.5f))
                    CustomTextField(value = country2Pct, onValueChange = { country2Pct = it }, label = "%", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = country3Name, onValueChange = { country3Name = it }, label = "Country 3", modifier = Modifier.weight(1.5f))
                    CustomTextField(value = country3Pct, onValueChange = { country3Pct = it }, label = "%", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = country4Name, onValueChange = { country4Name = it }, label = "Country 4", modifier = Modifier.weight(1.5f))
                    CustomTextField(value = country4Pct, onValueChange = { country4Pct = it }, label = "%", isNumber = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomTextField(value = country5Name, onValueChange = { country5Name = it }, label = "Country 5", modifier = Modifier.weight(1.5f))
                    CustomTextField(value = country5Pct, onValueChange = { country5Pct = it }, label = "%", isNumber = true, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save and Apply Button
            Button(
                onClick = {
                    val countryMap = linkedMapOf<String, Float>()
                    if (country1Name.isNotBlank()) countryMap[country1Name.trim()] = country1Pct.toFloatOrNull() ?: 44f
                    if (country2Name.isNotBlank()) countryMap[country2Name.trim()] = country2Pct.toFloatOrNull() ?: 4f
                    if (country3Name.isNotBlank()) countryMap[country3Name.trim()] = country3Pct.toFloatOrNull() ?: 4f
                    if (country4Name.isNotBlank()) countryMap[country4Name.trim()] = country4Pct.toFloatOrNull() ?: 2.5f
                    if (country5Name.isNotBlank()) countryMap[country5Name.trim()] = country5Pct.toFloatOrNull() ?: 1.8f

                    onSave(
                        caption,
                        handle,
                        views.toIntOrNull() ?: data.views,
                        viewers.toIntOrNull() ?: data.viewers,
                        avgWatch,
                        follows.toIntOrNull() ?: data.follows,
                        likes.toIntOrNull() ?: data.likes,
                        comments.toIntOrNull() ?: data.comments,
                        reshares.toIntOrNull() ?: data.reshares,
                        sends.toIntOrNull() ?: data.sends,
                        saves.toIntOrNull() ?: data.saves,
                        skipRate.toFloatOrNull() ?: data.skipRate,
                        skipQualifier,
                        likeRate.toFloatOrNull() ?: data.likeRate,
                        likeQualifier,
                        shareRate.toFloatOrNull() ?: data.shareRate,
                        saveRate.toFloatOrNull() ?: data.saveRate,
                        repostRate.toFloatOrNull() ?: data.repostRate,
                        commentRate.toFloatOrNull() ?: data.commentRate,
                        reelsTabPct.toFloatOrNull() ?: data.reelsTabPct,
                        explorePct.toFloatOrNull() ?: data.explorePct,
                        profilePct.toFloatOrNull() ?: data.profilePct,
                        feedPct.toFloatOrNull() ?: data.feedPct,
                        followersPct.toFloatOrNull() ?: data.followersAudiencePct,
                        nonFollowersPct.toFloatOrNull() ?: data.nonFollowersAudiencePct,
                        countryMap,
                        chartStartDate,
                        chartMidDate,
                        chartEndDate
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = IgMagenta,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_changes_button")
            ) {
                Text(text = "Apply Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = IgTextSecondary,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isNumber: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IgCardBg,
            unfocusedContainerColor = IgCardBg,
            focusedBorderColor = IgMagenta,
            unfocusedBorderColor = IgBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = IgMagenta,
            unfocusedLabelColor = IgTextSecondary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun QualifierSelector(
    selected: MetricQualifier,
    onSelect: (MetricQualifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(text = "Status", fontSize = 11.sp, color = IgTextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(MetricQualifier.LOWER, MetricQualifier.HIGHER, MetricQualifier.TYPICAL).forEach { q ->
                val isSel = selected == q
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) IgPillActive else IgCardBg)
                        .clickable { onSelect(q) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = q.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) q.color else IgTextSecondary
                    )
                }
            }
        }
    }
}
