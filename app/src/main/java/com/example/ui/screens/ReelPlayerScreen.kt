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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

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
    var showUploadedToast by remember { mutableStateOf(true) }

    // "Original video uploaded ⚡" banner fades out cleanly after initial 0.5s
    LaunchedEffect(Unit) {
        delay(600)
        showUploadedToast = false
    }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Full Screen Video Player / Cover Area (Click to play/pause)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        isPlaying = !isPlaying
                    }
                }
        ) {
            if (isLocalVideo) {
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

            // Top gradient overlay (smooth white/dark to transparent gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Bottom gradient scrim overlay (smooth transparent to rich dark gradient for captions & actions)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Play/Pause Floating Indicator
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // 2. Top Header Navigation (Gradient background, faded semi-transparent back arrow + faded icons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
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
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }

                Text(
                    text = "Your reels",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.92f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSelectVideoClick,
                        modifier = Modifier.testTag("select_video_in_player_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Select Video",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    IconButton(onClick = onOpenEditor) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera / Edit",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // "Original video uploaded ⚡" temporary indicator (smooth fade out in 0.5s)
            AnimatedVisibility(
                visible = showUploadedToast,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(400)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Original video uploaded ⚡",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Top custom overlay text if present and not the generic upload string
            if (reel.topOverlayText.isNotBlank() && !reel.topOverlayText.contains("uploaded", ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = reel.topOverlayText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // 3. Right-Side Action Icons Column (Heart, Comment, Repost, Share, Bookmark, More)
        // Positioned safely with end = 16.dp padding so NO icon is cut off!
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Like Button (Heart)
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
                    tint = if (isLiked) Color(0xFFFF2D55) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = likesCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 2. Comment Button (Chat bubble)
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
                Text(
                    text = if (reel.commentsCount > 0) reel.commentsCount.toString() else "0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 3. Repost Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onOpenEditor() }
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                if (reel.insightsData.reshares > 0) {
                    Text(
                        text = reel.insightsData.reshares.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // 4. Send / Share Button (Paper airplane)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Share",
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onOpenEditor() }
            )

            // 5. Bookmark / Save Button
            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Save",
                tint = if (isSaved) Color(0xFFF9CE34) else Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .size(26.dp)
                    .clickable { isSaved = !isSaved }
            )

            // 6. More (Three dots)
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More",
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onOpenEditor() }
            )
        }

        // 4. Bottom-Left Username + Avatar (Gradient Ring) + Caption Row
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.74f)
                .padding(start = 16.dp, bottom = 80.dp)
        ) {
            // Avatar with Instagram Story gradient ring + Username
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
                                    Color(0xFFEE2A7B),
                                    Color(0xFF6228D7),
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
                    text = reel.insightsData.handle.ifBlank { "alishaasassy" },
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption Text
            Text(
                text = reel.caption.ifBlank { "IG model @piperrockelle recently sparked attentio ..." },
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 2,
                lineHeight = 17.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onOpenEditor() }
            )
        }

        // 5. Bottom Bar (Get inspired on Edits pill + 👁 1.3K views + 📈 Boost)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
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
                        horizontal = 13.dp,
                        vertical = 7.dp
                    ),
                    modifier = Modifier.testTag("get_inspired_on_edits_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE1306C),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Get inspired on Edits",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Views Counter & Boost Button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clickable Views Counter with EYE ICON (👁 1.3K views)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenInsights() }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("reel_views_insights_trigger"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = "Views",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${formatViewsShort(reel.viewsCount)} views",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Boost Button with Trend Icon
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenInsights() }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("reel_boost_insights_trigger"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                            contentDescription = "Boost",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
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
        views >= 1_000_000 -> {
            val v = views / 1_000_000.0
            if (views % 1_000_000 == 0) "${views / 1_000_000}M"
            else String.format(java.util.Locale.US, "%.1fM", v)
        }
        views >= 1000 -> {
            val v = views / 1000.0
            if (views % 1000 == 0) "${views / 1000}K"
            else String.format(java.util.Locale.US, "%.1fK", v)
        }
        else -> views.toString()
    }
}
