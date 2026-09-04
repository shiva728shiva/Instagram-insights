package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ReelItem
import com.example.ui.components.IgIcons
import com.example.ui.theme.IgPinkAccent
import com.example.ui.theme.IgPurple
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelPlayerScreen(
    reel: ReelItem,
    onBackToProfile: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenEditor: () -> Unit,
    onSelectVideoClick: () -> Unit = {},
    onUpdateAvatar: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember(reel) { mutableIntStateOf(reel.likesCount) }
    var isSaved by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var showTempActionIndicator by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var hideIndicatorJob by remember { mutableStateOf<Job?>(null) }

    // Launcher for selecting profile photo from phone gallery directly
    val galleryAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onUpdateAvatar(it.toString())
        }
    }

    val playableVideoUri = remember(reel) {
        reel.insightsData.videoUri?.ifBlank { null }
            ?: reel.videoUrl.ifBlank { null }
    }

    // Subtle gentle zoom animation when using image fallback
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
        // 1. Full Screen Video Player / Cover Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(playableVideoUri) {
                    detectTapGestures {
                        if (playableVideoUri != null) {
                            isPlaying = !isPlaying
                            showTempActionIndicator = true
                            hideIndicatorJob?.cancel()
                            hideIndicatorJob = coroutineScope.launch {
                                delay(650L)
                                showTempActionIndicator = false
                            }
                        } else {
                            onSelectVideoClick()
                        }
                    }
                }
        ) {
            if (playableVideoUri != null) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val uri = if (playableVideoUri.startsWith("/")) {
                                Uri.fromFile(java.io.File(playableVideoUri))
                            } else {
                                Uri.parse(playableVideoUri)
                            }
                            setVideoURI(uri)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.setVolume(1f, 1f)
                                if (isPlaying) start()
                            }
                            setOnErrorListener { _, _, _ -> true }
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

                DisposableEffect(playableVideoUri) {
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

            // Top gradient overlay for header readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Bottom gradient scrim overlay for captions & actions readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Temporary Floating Play/Pause Indicator that cleanly fades out
            AnimatedVisibility(
                visible = showTempActionIndicator,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPlaying) "Play" else "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        // 2. Top Header Navigation: "Reels" + Back Arrow + Camera Icon (Matching Real IG Screenshot 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBackToProfile() }
            ) {
                IconButton(
                    onClick = onBackToProfile,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("back_to_profile_button")
                ) {
                    Icon(
                        painter = painterResource(id = IgIcons.back),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                Text(
                    text = "Reels",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(
                onClick = onSelectVideoClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("camera_video_picker_btn")
            ) {
                Icon(
                    painter = painterResource(id = IgIcons.camera),
                    contentDescription = "Camera / Select Video",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 3. Right-Side Action Icons Column (Heart, Comment, Repost, Share, Bookmark, More, Audio Artwork)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 74.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Like Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    isLiked = !isLiked
                    likesCount += if (isLiked) 1 else -1
                }
            ) {
                Icon(
                    painter = painterResource(id = IgIcons.likeRate),
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFFF2D55) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = formatViewsShort(likesCount),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            // 2. Comment Button (In real IG, no number is displayed if 0)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onOpenEditor() }
            ) {
                Icon(
                    painter = painterResource(id = IgIcons.commentRate),
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                if (reel.commentsCount > 0) {
                    Text(
                        text = formatViewsShort(reel.commentsCount),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            // 3. Repost Button (In real IG, no number is displayed if 0)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onOpenEditor() }
            ) {
                Icon(
                    painter = painterResource(id = IgIcons.repostRate),
                    contentDescription = "Repost",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                if (reel.insightsData.reshares > 0) {
                    Text(
                        text = formatViewsShort(reel.insightsData.reshares),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            // 4. Send / Share Button (Paper Plane)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onOpenEditor() }
            ) {
                Icon(
                    painter = painterResource(id = IgIcons.shareRate),
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                if (reel.insightsData.sends > 0) {
                    Text(
                        text = formatViewsShort(reel.insightsData.sends),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            // 5. Bookmark / Save Button
            Icon(
                painter = painterResource(id = IgIcons.saveRate),
                contentDescription = "Save",
                tint = if (isSaved) Color(0xFFF9CE34) else Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isSaved = !isSaved }
            )

            // 6. More (Two horizontal bars =)
            Icon(
                painter = painterResource(id = IgIcons.more),
                contentDescription = "More",
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onOpenEditor() }
            )

            // 7. Small Audio Album Art Square (at bottom of action column matching real IG)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .border(1.2.dp, Color.White, RoundedCornerShape(5.dp))
                    .clickable { onOpenEditor() }
            ) {
                AsyncImage(
                    model = reel.insightsData.thumbnailUrl,
                    contentDescription = "Audio Artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 4. Bottom-Left Username + Audio + Caption (Matching Real IG Screenshot 2)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.72f)
                .padding(start = 14.dp, bottom = 74.dp)
        ) {
            // Avatar + Username + Audio Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // Clean circular profile avatar matching real IG
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable {
                            galleryAvatarLauncher.launch("image/*")
                        }
                ) {
                    AsyncImage(
                        model = reel.insightsData.thumbnailUrl,
                        contentDescription = "Avatar (Tap to change)",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = reel.insightsData.handle.ifBlank { "costflorarprim1974" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clickable { onOpenEditor() }
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Audio track title row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenEditor() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = reel.insightsData.musicTitle.ifBlank { "Original audio" },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Caption Text
            Text(
                text = reel.caption.ifBlank { "@higgsfield.ai — every new universe can change t ..." },
                fontSize = 13.5.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onOpenEditor() }
            )
        }

        // 5. Bottom Bar (Get inspired on Edits pill + Vertically Stacked Views & Boost)
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
                // "Get inspired on Edits" pill button with authentic badge icon
                Button(
                    onClick = onOpenInsights,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 7.dp
                    ),
                    modifier = Modifier.testTag("get_inspired_on_edits_button")
                ) {
                    Icon(
                        painter = painterResource(id = IgIcons.editsBadge),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get inspired on Edits",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                // Views Counter & Boost Button (Vertically Stacked Icon + Text matching Real IG)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clickable Views Counter with Eye Icon on top
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenInsights() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("reel_views_insights_trigger")
                    ) {
                        Icon(
                            painter = painterResource(id = IgIcons.eye),
                            contentDescription = "Views",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${formatViewsShort(reel.viewsCount)} views",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }

                    // Boost Button with Trending Arrow Icon on top
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenInsights() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("reel_boost_insights_trigger")
                    ) {
                        Icon(
                            painter = painterResource(id = IgIcons.boost),
                            contentDescription = "Boost",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Boost",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
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
