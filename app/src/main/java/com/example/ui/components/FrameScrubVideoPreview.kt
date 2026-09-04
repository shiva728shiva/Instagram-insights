package com.example.ui.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FrameScrubVideoPreview(
    videoUri: String?,
    thumbnailUrl: String,
    scrubSecond: Int,
    timeLabel: String = "",
    showPlayIcon: Boolean = true,
    width: Dp = 72.dp,
    height: Dp = 126.dp,
    cornerRadius: Dp = 6.dp,
    onVideoSelected: ((Uri) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    val frameCache = remember(videoUri) { mutableMapOf<Int, Bitmap>() }

    val videoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && onVideoSelected != null) {
            onVideoSelected(uri)
        }
    }

    // When scrubSecond changes and videoView is active, seek immediately
    LaunchedEffect(scrubSecond) {
        if (videoViewRef != null) {
            try {
                videoViewRef?.seekTo(scrubSecond * 1000)
            } catch (_: Exception) {}
        }
    }

    // When scrubSecond changes and not actively playing, extract or lookup the exact frame
    LaunchedEffect(videoUri, scrubSecond) {
        if (!videoUri.isNullOrBlank() && !isPlaying) {
            if (frameCache.containsKey(scrubSecond)) {
                currentFrameBitmap = frameCache[scrubSecond]
            } else {
                try {
                    withContext(Dispatchers.IO) {
                        val retriever = MediaMetadataRetriever()
                        try {
                            if (videoUri.startsWith("content://")) {
                                retriever.setDataSource(context, Uri.parse(videoUri))
                            } else if (videoUri.startsWith("file://")) {
                                retriever.setDataSource(Uri.parse(videoUri).path)
                            } else if (videoUri.startsWith("/")) {
                                retriever.setDataSource(videoUri)
                            } else {
                                retriever.setDataSource(videoUri, HashMap())
                            }
                            val frameUs = scrubSecond * 1_000_000L
                            val frame = retriever.getFrameAtTime(frameUs, MediaMetadataRetriever.OPTION_CLOSEST)
                            if (frame != null) {
                                frameCache[scrubSecond] = frame
                                withContext(Dispatchers.Main) {
                                    currentFrameBitmap = frame
                                }
                            }
                        } catch (_: Exception) {
                        } finally {
                            try {
                                retriever.release()
                            } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2E1B4E), Color(0xFF1E1E24), Color(0xFF0F0F12))
                )
            )
            .clickable {
                if (videoUri.isNullOrBlank() && onVideoSelected != null) {
                    try {
                        videoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
                            )
                        )
                    } catch (_: Exception) {}
                } else {
                    isPlaying = !isPlaying
                }
            }
            .testTag("video_scrub_preview"),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying && !videoUri.isNullOrBlank()) {
            // Live Video Playback Mode
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val uri = if (videoUri.startsWith("/")) Uri.fromFile(java.io.File(videoUri)) else Uri.parse(videoUri)
                        setVideoURI(uri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            mp.setVolume(0.8f, 0.8f)
                            seekTo(scrubSecond * 1000)
                            start()
                        }
                        videoViewRef = this
                    }
                },
                update = { view ->
                    view.seekTo(scrubSecond * 1000)
                    if (!view.isPlaying) {
                        view.start()
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
            // Exact Scrubbed Frame or Thumbnail with graceful dark fallback
            if (currentFrameBitmap != null) {
                Image(
                    bitmap = currentFrameBitmap!!.asImageBitmap(),
                    contentDescription = "Video Frame at $scrubSecond s",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Video Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Scrub timestamp overlay when scrubbing or active
        if (timeLabel.isNotBlank() && timeLabel != "0:00") {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = timeLabel,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Instagram Hollow Outlined Play Icon - small, delicate, and rounded matching real Instagram
        if (showPlayIcon && !isPlaying) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(16.dp)
            ) {
                val cx = size.width / 2
                val cy = size.height / 2

                // Visual center offset so triangle center-of-mass aligns with thumbnail center
                val offsetVisual = 0.8.dp.toPx()
                val v1 = Offset(cx - 4.8.dp.toPx() + offsetVisual, cy - 5.8.dp.toPx()) // top-left
                val v2 = Offset(cx + 5.8.dp.toPx() + offsetVisual, cy)                 // right tip
                val v3 = Offset(cx - 4.8.dp.toPx() + offsetVisual, cy + 5.8.dp.toPx()) // bottom-left

                // Distinct rounded corner radius (tip has soft roundness as in real IG)
                val crTip = 2.4.dp.toPx()
                val crBase = 1.8.dp.toPx()

                val d12 = kotlin.math.hypot((v2.x - v1.x).toDouble(), (v2.y - v1.y).toDouble()).toFloat()
                val u12 = Offset((v2.x - v1.x) / d12, (v2.y - v1.y) / d12)

                val d23 = kotlin.math.hypot((v3.x - v2.x).toDouble(), (v3.y - v2.y).toDouble()).toFloat()
                val u23 = Offset((v3.x - v2.x) / d23, (v3.y - v2.y) / d23)

                val d31 = kotlin.math.hypot((v1.x - v3.x).toDouble(), (v1.y - v3.y).toDouble()).toFloat()
                val u31 = Offset((v1.x - v3.x) / d31, (v1.y - v3.y) / d31)

                val p1In = v1 - u31 * crBase
                val p1Out = v1 + u12 * crBase

                val p2In = v2 - u12 * crTip
                val p2Out = v2 + u23 * crTip

                val p3In = v3 - u23 * crBase
                val p3Out = v3 + u31 * crBase

                val playPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(p1Out.x, p1Out.y)
                    lineTo(p2In.x, p2In.y)
                    quadraticTo(v2.x, v2.y, p2Out.x, p2Out.y)
                    lineTo(p3In.x, p3In.y)
                    quadraticTo(v3.x, v3.y, p3Out.x, p3Out.y)
                    lineTo(p1In.x, p1In.y)
                    quadraticTo(v1.x, v1.y, p1Out.x, p1Out.y)
                    close()
                }

                // Pure crisp hollow white outline with smooth rounded vertices
                drawPath(
                    path = playPath,
                    color = Color.White.copy(alpha = 0.95f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}
