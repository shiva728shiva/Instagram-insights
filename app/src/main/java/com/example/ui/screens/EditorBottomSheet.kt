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
        commentRate: Float
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

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save and Apply Button
            Button(
                onClick = {
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
                        commentRate.toFloatOrNull() ?: data.commentRate
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
