package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.ReelItem
import com.example.ui.theme.IgPinkAccent
import com.example.ui.theme.IgPurple

@Composable
fun ReelPlayerScreen(
    reel: ReelItem,
    onBackToProfile: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenEditor: () -> Unit,
    onSelectVideoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember(reel) { mutableIntStateOf(reel.likesCount) }
    var isSaved by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    val isLocalVideo = !reel.insightsData.videoUri.isNullOrBlank() && reel.insightsData.videoUri?.startsWith("content://") == true

    // Subtle gentle zoom animation to give live dynamic video effect
    val infiniteTransition = rememberInfiniteTransition(label = "videoMotion")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoom"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Navigation Bar matching Screenshot 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToProfile,
                modifier = Modifier.testTag("back_to_profile_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Your reels",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSelectVideoClick,
                    modifier = Modifier.testTag("select_video_in_player_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Select Video",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onOpenEditor) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera / Edit",
                        tint = Color.White
                    )
                }
            }
        }

        // Full Screen Video Player Area (Interactive tap to play/pause)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures {
                        isPlaying = !isPlaying
                    }
                }
        ) {
            if (isLocalVideo) {
                // Real Video Playback for selected local video
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.parse(reel.insightsData.videoUri))
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.setVolume(1f, 1f)
                                if (isPlaying) start()
                            }
                            videoViewRef = this
                        }
                    },
                    update = { view ->
                        if (isPlaying) {
                            if (!view.isPlaying) view.start()
                        } else {
                            if (view.isPlaying) view.pause()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                DisposableEffect(Unit) {
                    onDispose {
                        videoViewRef?.stopPlayback()
                    }
                }
            } else {
                // Video Cover / Dynamic Player Canvas
                AsyncImage(
                    model = reel.thumbnailUrl,
                    contentDescription = "Reel video playback",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (isPlaying) {
                                scaleX = scale
                                scaleY = scale
                            }
                        }
                )
            }

            // Top overlay text (Matching "Bellamy and Camilla talked for almost 2 HOURS?! 😳")
            if (reel.topOverlayText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = reel.topOverlayText,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }
            }

            // Play/Pause Floating Icon Indicator
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // Right-Side Floating Action Column (Heart, Comment, Repost, Share, Bookmark, More)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        isLiked = !isLiked
                        likesCount += if (isLiked) 1 else -1
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = likesCount.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                // Comment Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onOpenEditor() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    if (reel.commentsCount > 0) {
                        Text(
                            text = reel.commentsCount.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }

                // Repost Button
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )

                // Share Button (Paper airplane)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )

                // Bookmark / Save Button
                Icon(
                    imageVector = if (isSaved) Icons.Default.BookmarkBorder else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) Color(0xFFF9CE34) else Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { isSaved = !isSaved }
                )

                // More (Three dots)
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onOpenEditor() }
                )
            }

            // Bottom-Left User Details & Caption Overlay (Clickable to edit username, avatar, or caption)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.78f)
                    .padding(start = 14.dp, bottom = 14.dp)
            ) {
                // Avatar + Handle (Clickable to edit profile)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenEditor() }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFF9CE34),
                                        IgPinkAccent,
                                        IgPurple,
                                        Color(0xFFF9CE34)
                                    )
                                )
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = reel.insightsData.thumbnailUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = reel.insightsData.handle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Caption (Clickable to edit caption)
                Text(
                    text = reel.caption,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onOpenEditor() }
                )
            }
        }

        // Bottom CTA Bar matching Screenshot 3: [ ✨ Get inspired on Edits ] and [ 👁 1.3K views  ↗ Boost ]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Get inspired on Edits" pill button
                Button(
                    onClick = onOpenInsights,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    ),
                    modifier = Modifier.testTag("get_inspired_on_edits_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE1306C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Get inspired on Edits",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Views Counter & Boost Button (Clicking here opens Reel Insights!)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clickable Views Counter with Eye Icon
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenInsights() }
                            .padding(4.dp)
                            .testTag("reel_views_insights_trigger"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Views",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatViewsShort(reel.viewsCount)} views",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Boost Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenInsights() }
                            .padding(4.dp)
                            .testTag("reel_boost_insights_trigger"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Boost",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatViewsShort(views: Int): String {
    return when {
        views >= 1_000_000 -> String.format("%.1fM", views / 1_000_000.0)
        views >= 1000 -> String.format("%.1fK", views / 1000.0)
        else -> views.toString()
    }
}
