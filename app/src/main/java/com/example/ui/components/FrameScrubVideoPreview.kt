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
    width: Dp = 136.dp,
    height: Dp = 240.dp,
    cornerRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    // When scrubSecond changes and not actively playing, extract the exact frame
    LaunchedEffect(videoUri, scrubSecond) {
        if (!videoUri.isNullOrBlank() && !isPlaying) {
            try {
                withContext(Dispatchers.IO) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        val uri = Uri.parse(videoUri)
                        retriever.setDataSource(context, uri)
                        val frameUs = scrubSecond * 1_000_000L
                        val frame = retriever.getFrameAtTime(frameUs, MediaMetadataRetriever.OPTION_CLOSEST)
                        withContext(Dispatchers.Main) {
                            currentFrameBitmap = frame
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
                isPlaying = !isPlaying
            }
            .testTag("video_scrub_preview"),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying && !videoUri.isNullOrBlank()) {
            // Live Video Playback Mode
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(videoUri))
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
                    if (!view.isPlaying) {
                        view.seekTo(scrubSecond * 1000)
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

        // Instagram Hollow Outlined Play Icon with smooth rounded corners (compact & refined)
        if (showPlayIcon && !isPlaying) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(34.dp)
            ) {
                val cx = size.width / 2
                val cy = size.height / 2

                val v1 = Offset(cx - 10.dp.toPx(), cy - 13.dp.toPx()) // top-left
                val v2 = Offset(cx + 13.5.dp.toPx(), cy)               // right tip
                val v3 = Offset(cx - 10.dp.toPx(), cy + 13.dp.toPx()) // bottom-left

                val cr = 3.6.dp.toPx()

                val d12 = kotlin.math.hypot((v2.x - v1.x).toDouble(), (v2.y - v1.y).toDouble()).toFloat()
                val u12 = Offset((v2.x - v1.x) / d12, (v2.y - v1.y) / d12)
                val u21 = Offset(-u12.x, -u12.y)

                val d23 = kotlin.math.hypot((v3.x - v2.x).toDouble(), (v3.y - v2.y).toDouble()).toFloat()
                val u23 = Offset((v3.x - v2.x) / d23, (v3.y - v2.y) / d23)
                val u32 = Offset(-u23.x, -u23.y)

                val p1In = Offset(v1.x, v1.y + cr)
                val p1Out = v1 + u12 * cr

                val p2In = v2 + u21 * (cr * 1.3f)
                val p2Out = v2 + u23 * (cr * 1.3f)

                val p3In = v3 + u32 * cr
                val p3Out = Offset(v3.x, v3.y - cr)

                val playPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(p1In.x, p1In.y)
                    quadraticTo(v1.x, v1.y, p1Out.x, p1Out.y)
                    lineTo(p2In.x, p2In.y)
                    quadraticTo(v2.x, v2.y, p2Out.x, p2Out.y)
                    lineTo(p3In.x, p3In.y)
                    quadraticTo(v3.x, v3.y, p3Out.x, p3Out.y)
                    lineTo(p1In.x, p1In.y)
                    close()
                }

                // Pure crisp hollow white outline with smooth rounded vertices
                drawPath(
                    path = playPath,
                    color = Color.White.copy(alpha = 0.95f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.4.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}
