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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            // Exact Scrubbed Frame or Thumbnail
            if (currentFrameBitmap != null) {
                Image(
                    bitmap = currentFrameBitmap!!.asImageBitmap(),
                    contentDescription = "Video Frame at $scrubSecond s",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "Video Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Circular Play/Pause Overlay indicator matching Real Instagram
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.42f))
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
