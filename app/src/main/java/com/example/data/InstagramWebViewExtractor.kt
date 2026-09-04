package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class RealInstagramReelResult(
    val shortcode: String,
    val username: String?,
    val caption: String?,
    val likesCount: Int?,
    val commentsCount: Int?,
    val viewsCount: Int?,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val localVideoUri: Uri?,
    val isRealDataFetched: Boolean
)

object InstagramWebViewExtractor {

    private const val TAG = "InstaWebViewExtractor"

    private val httpClient = OkHttpClient()

    private fun parseFormattedCount(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.trim().uppercase().replace(",", "")
        return try {
            when {
                clean.endsWith("K") -> (clean.dropLast(1).toFloat() * 1000).toInt()
                clean.endsWith("M") -> (clean.dropLast(1).toFloat() * 1000000).toInt()
                clean.endsWith("B") -> (clean.dropLast(1).toFloat() * 1000000000).toInt()
                else -> clean.replace(".", "").toInt()
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts real numbers from OpenGraph description or page text:
     * e.g. "12.4K likes, 120 comments - username on date: \"caption\""
     */
    fun parseDescriptionText(text: String): Triple<Int?, Int?, Int?> {
        var likes: Int? = null
        var comments: Int? = null
        var views: Int? = null

        val likesMatcher = Pattern.compile("([0-9,.]+[KkMmBb]?)\\s*likes", Pattern.CASE_INSENSITIVE).matcher(text)
        if (likesMatcher.find()) {
            likes = parseFormattedCount(likesMatcher.group(1))
        }

        val commentsMatcher = Pattern.compile("([0-9,.]+[KkMmBb]?)\\s*comments", Pattern.CASE_INSENSITIVE).matcher(text)
        if (commentsMatcher.find()) {
            comments = parseFormattedCount(commentsMatcher.group(1))
        }

        val viewsMatcher = Pattern.compile("([0-9,.]+[KkMmBb]?)\\s*(?:views|plays)", Pattern.CASE_INSENSITIVE).matcher(text)
        if (viewsMatcher.find()) {
            views = parseFormattedCount(viewsMatcher.group(1))
        }

        return Triple(likes, comments, views)
    }

    /**
     * Uses an in-app WebView on the device to load the reel link,
     * intercept direct MP4 streams, and execute JS to extract real metadata.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun extractRealReel(
        context: Context,
        reelUrl: String,
        onResult: (RealInstagramReelResult) -> Unit
    ) {
        val shortcode = InstagramLinkFetcher.extractShortcode(reelUrl) ?: "reel_${System.currentTimeMillis() % 10000}"
        val targetUrl = "https://www.instagram.com/reel/$shortcode/"

        Handler(Looper.getMainLooper()).post {
            var webView: WebView? = null
            var capturedVideoUrl: String? = null
            var hasFinished = false

            fun finishWithData(
                extractedUsername: String?,
                extractedCaption: String?,
                likes: Int?,
                comments: Int?,
                views: Int?,
                vidUrl: String?,
                thumbUrl: String?
            ) {
                if (hasFinished) return
                hasFinished = true

                Handler(Looper.getMainLooper()).post {
                    try {
                        webView?.stopLoading()
                        webView?.destroy()
                        webView = null
                    } catch (e: Exception) {
                        Log.e(TAG, "Error destroying webview", e)
                    }
                }

                val hasRealStats = (likes != null && likes > 0) || (views != null && views > 0)

                onResult(
                    RealInstagramReelResult(
                        shortcode = shortcode,
                        username = extractedUsername,
                        caption = extractedCaption,
                        likesCount = likes,
                        commentsCount = comments,
                        viewsCount = views,
                        videoUrl = vidUrl ?: capturedVideoUrl,
                        thumbnailUrl = thumbUrl,
                        localVideoUri = null,
                        isRealDataFetched = hasRealStats || (vidUrl != null)
                    )
                )
            }

            try {
                webView = WebView(context.applicationContext)
                val settings: WebSettings = webView!!.settings
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 13; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                webView!!.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url?.toString() ?: ""
                        if (url.contains(".mp4") || url.contains("video_dashinit") || (url.contains("cdninstagram.com") && url.contains("/v/"))) {
                            if (capturedVideoUrl == null) {
                                capturedVideoUrl = url
                                Log.d(TAG, "Captured direct MP4 URL from network: $url")
                            }
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        // Run JavaScript to extract real data from OpenGraph meta and HTML elements
                        val jsCode = """
                            (function() {
                                var ogDesc = document.querySelector('meta[property="og:description"]')?.content || '';
                                var ogTitle = document.querySelector('meta[property="og:title"]')?.content || '';
                                var ogImage = document.querySelector('meta[property="og:image"]')?.content || '';
                                var ogVideo = document.querySelector('meta[property="og:video"]')?.content || '';
                                var videoEl = document.querySelector('video');
                                var videoSrc = videoEl ? (videoEl.src || videoEl.currentSrc || '') : '';
                                var bodyText = document.body ? document.body.innerText.substring(0, 3000) : '';

                                return JSON.stringify({
                                    ogDesc: ogDesc,
                                    ogTitle: ogTitle,
                                    ogImage: ogImage,
                                    ogVideo: ogVideo,
                                    videoSrc: videoSrc,
                                    bodyText: bodyText
                                });
                            })();
                        """.trimIndent()

                        view?.evaluateJavascript(jsCode) { resultJson ->
                            try {
                                if (!resultJson.isNullOrBlank() && resultJson != "null") {
                                    val cleaned = if (resultJson.startsWith("\"") && resultJson.endsWith("\"")) {
                                        // Unescape JSON string returned by evaluateJavascript
                                        JSONObject("{ \"res\": $resultJson }").getString("res")
                                    } else {
                                        resultJson
                                    }

                                    val parsed = JSONObject(cleaned)
                                    val ogDesc = parsed.optString("ogDesc", "")
                                    val ogTitle = parsed.optString("ogTitle", "")
                                    val ogImage = parsed.optString("ogImage", "")
                                    val ogVideo = parsed.optString("ogVideo", "")
                                    val videoSrc = parsed.optString("videoSrc", "")
                                    val bodyText = parsed.optString("bodyText", "")

                                    val finalVideo = when {
                                        videoSrc.isNotBlank() -> videoSrc
                                        ogVideo.isNotBlank() -> ogVideo
                                        else -> capturedVideoUrl
                                    }

                                    // Parse stats from description or body text
                                    val descStats = parseDescriptionText(ogDesc)
                                    val bodyStats = if (descStats.first == null) parseDescriptionText(bodyText) else descStats

                                    var username: String? = null
                                    val userMatcher = Pattern.compile("-\\s*@?([a-zA-Z0-9._]+)\\s+on", Pattern.CASE_INSENSITIVE).matcher(ogDesc)
                                    if (userMatcher.find()) {
                                        username = userMatcher.group(1)
                                    }

                                    var caption: String? = null
                                    val captionMatcher = Pattern.compile(":\\s*\"(.*)\"", Pattern.CASE_INSENSITIVE).matcher(ogDesc)
                                    if (captionMatcher.find()) {
                                        caption = captionMatcher.group(1)
                                    } else if (ogTitle.isNotBlank() && !ogTitle.equals("Instagram", ignoreCase = true)) {
                                        caption = ogTitle
                                    }

                                    finishWithData(
                                        extractedUsername = username,
                                        extractedCaption = caption,
                                        likes = bodyStats.first,
                                        comments = bodyStats.second,
                                        views = bodyStats.third,
                                        vidUrl = finalVideo,
                                        thumbUrl = ogImage.ifBlank { null }
                                    )
                                    return@evaluateJavascript
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing JS evaluation result", e)
                            }

                            // Fallback if JS returned null or threw
                            finishWithData(
                                extractedUsername = null,
                                extractedCaption = null,
                                likes = null,
                                comments = null,
                                views = null,
                                vidUrl = capturedVideoUrl,
                                thumbUrl = null
                            )
                        }
                    }
                }

                webView!!.loadUrl(targetUrl)

                // Timeout fallback after 9 seconds so the UI never hangs
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!hasFinished) {
                        finishWithData(
                            extractedUsername = null,
                            extractedCaption = null,
                            likes = null,
                            comments = null,
                            views = null,
                            vidUrl = capturedVideoUrl,
                            thumbUrl = null
                        )
                    }
                }, 9000)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize WebView for extraction", e)
                finishWithData(null, null, null, null, null, null, null)
            }
        }
    }

    /**
     * Downloads direct MP4 stream into application cache file for frame scrubbing.
     */
    suspend fun downloadVideoToCache(
        context: Context,
        videoUrl: String,
        shortcode: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "ig_reel_${shortcode}.mp4")
            val req = Request.Builder()
                .url(videoUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            val resp = httpClient.newCall(req).execute()
            if (resp.isSuccessful && resp.body != null) {
                val bytes = resp.body!!.bytes()
                if (bytes.isNotEmpty()) {
                    FileOutputStream(file).use { it.write(bytes) }
                    return@withContext Uri.fromFile(file)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed downloading video stream: ${e.message}")
        }
        null
    }
}
