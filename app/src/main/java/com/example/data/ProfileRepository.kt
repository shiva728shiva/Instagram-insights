package com.example.data

import android.util.Log
import com.example.data.api.InstagramApiClient
import com.example.data.model.MetricQualifier
import com.example.data.model.ReelInsightsData
import com.example.data.model.ReelItem
import com.example.data.model.RetentionPoint
import com.example.data.model.UserProfile
import com.example.data.model.ViewDataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProfileRepository {

    private const val TAG = "ProfileRepository"

    suspend fun fetchProfileWithRealApi(
        rawUsername: String,
        accountId: String = InstagramApiClient.DEFAULT_ACCOUNT_ID,
        accessToken: String = InstagramApiClient.DEFAULT_ACCESS_TOKEN
    ): UserProfile = withContext(Dispatchers.IO) {
        val baseProfile = getProfileForUsername(rawUsername)
        try {
            val userResponse = InstagramApiClient.api.getUserProfile(
                userId = accountId,
                accessToken = accessToken
            )

            if (userResponse.isSuccessful && userResponse.body() != null) {
                val user = userResponse.body()!!
                val realUsername = user.username ?: baseProfile.username
                val realName = user.name ?: baseProfile.fullName
                val realBio = user.biography ?: baseProfile.bio
                val realAvatar = user.profilePictureUrl ?: baseProfile.avatarUrl
                val realPosts = user.mediaCount ?: baseProfile.postsCount
                val realFollowers = user.followersCount ?: baseProfile.followersCount
                val realFollowing = user.followsCount ?: baseProfile.followingCount

                // Try fetching media
                val mediaResponse = InstagramApiClient.api.getUserMedia(
                    userId = accountId,
                    accessToken = accessToken
                )

                val reelsList = if (mediaResponse.isSuccessful && !mediaResponse.body()?.data.isNullOrEmpty()) {
                    val mediaList = mediaResponse.body()!!.data!!
                    mediaList.mapIndexed { index, media ->
                        val thumb = media.thumbnailUrl ?: media.mediaUrl ?: baseProfile.reels.getOrNull(index % baseProfile.reels.size)?.thumbnailUrl ?: baseProfile.avatarUrl
                        val playsMetric = media.insights?.data?.firstOrNull { it.name == "plays" }?.values?.firstOrNull()?.value?.toInt() ?: (media.likeCount ?: 0) * 12 + 150
                        val reachMetric = media.insights?.data?.firstOrNull { it.name == "reach" }?.values?.firstOrNull()?.value?.toInt() ?: (playsMetric * 0.4).toInt()
                        val likes = media.likeCount ?: 28
                        val comments = media.commentsCount ?: 0

                        val reelInsights = baseProfile.reels.getOrNull(index)?.insightsData?.copy(
                            caption = media.caption ?: "Reel video update",
                            handle = realUsername,
                            thumbnailUrl = thumb,
                            views = playsMetric,
                            viewers = reachMetric,
                            likes = likes,
                            comments = comments
                        ) ?: baseProfile.reels.first().insightsData.copy(
                            caption = media.caption ?: "Reel video update",
                            handle = realUsername,
                            thumbnailUrl = thumb,
                            views = playsMetric,
                            viewers = reachMetric,
                            likes = likes,
                            comments = comments
                        )

                        ReelItem(
                            id = media.id,
                            thumbnailUrl = thumb,
                            videoUrl = media.mediaUrl ?: "",
                            viewsCount = playsMetric,
                            likesCount = likes,
                            commentsCount = comments,
                            caption = media.caption ?: "Instagram Reel #${index + 1}",
                            topOverlayText = "",
                            watermarks = emptyList(),
                            insightsData = reelInsights
                        )
                    }
                } else {
                    baseProfile.reels
                }

                return@withContext UserProfile(
                    username = realUsername,
                    fullName = realName,
                    avatarUrl = realAvatar,
                    category = baseProfile.category,
                    bio = realBio,
                    postsCount = realPosts,
                    followersCount = realFollowers,
                    followingCount = realFollowing,
                    monthlyViews = "${formatCount(reelsList.sumOf { it.viewsCount })} views in the last 30 days.",
                    reels = reelsList
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load real Instagram Graph API data, using enriched local data: ${e.message}", e)
        }

        return@withContext baseProfile
    }

    fun getProfileForUsername(rawUsername: String): UserProfile {
        val username = rawUsername.trim().lowercase().removePrefix("@").ifBlank { "alishaasassy" }
        
        // Base profile setup matching Screenshot 2
        val isDefault = username == "alishaasassy" || username == "alisha"
        val displayName = if (isDefault) "Alisha" else username.replaceFirstChar { it.uppercase() }
        val posts = if (isDefault) 98 else 42
        val followers = if (isDefault) 0 else 1250
        val following = if (isDefault) 5 else 340
        val bio = if (isDefault) "Fan page\n20\nCapricorn\n`c5fe`" else "Digital Creator ✨\nDaily edits & reels 🎬\nDM for collabs 📩"
        val category = if (isDefault) "Fan page" else "Creator"
        val avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80"

        fun createDynamicInsights(
            views: Int,
            likes: Int,
            viewers: Int,
            caption: String,
            thumb: String,
            avgWatchTime: String = "16s",
            comments: Int = 0,
            reshares: Int = 1,
            sends: Int = 0,
            saves: Int = 0,
            durationSec: Int = 8
        ): ReelInsightsData {
            val healthyViewsOverTime = HealthyGraphGenerator.generateViewsOverTime(totalViews = views, viewersCount = viewers)
            val avgWatchSec = avgWatchTime.replace("s", "").replace("sec", "").trim().toFloatOrNull() ?: (durationSec * 0.85f)
            val healthyRetention = HealthyGraphGenerator.generateRetentionCurve(
                durationSeconds = durationSec,
                totalViews = views,
                viewersCount = viewers,
                avgWatchTimeSec = avgWatchSec
            )
            val healthyWhenLiked = HealthyGraphGenerator.generateWhenLiked(
                durationSeconds = durationSec,
                likesCount = likes,
                totalViews = views
            )

            return ReelInsightsData(
                caption = caption,
                handle = username,
                thumbnailUrl = thumb,
                likes = likes,
                comments = comments,
                reshares = reshares,
                sends = sends,
                saves = saves,
                views = views,
                viewers = viewers,
                avgWatchTime = avgWatchTime,
                follows = (views * 0.002).toInt(),
                skipRate = 11.9f,
                skipRateQualifier = MetricQualifier.LOWER,
                shareRate = if (reshares > 0) ((reshares.toFloat() / views) * 100f) else 0.0f,
                shareRateQualifier = MetricQualifier.TYPICAL,
                likeRate = ((likes.toFloat() / views) * 100f),
                likeRateQualifier = MetricQualifier.HIGHER,
                saveRate = if (saves > 0) ((saves.toFloat() / views) * 100f) else 0.0f,
                saveRateQualifier = MetricQualifier.TYPICAL,
                repostRate = 0.0f,
                repostRateQualifier = MetricQualifier.TYPICAL,
                commentRate = if (comments > 0) ((comments.toFloat() / views) * 100f) else 0.0f,
                commentRateQualifier = MetricQualifier.TYPICAL,
                viewsOverTime = healthyViewsOverTime,
                retention = healthyRetention,
                whenLiked = healthyWhenLiked,
                reelsTabPct = 22.1f,
                explorePct = 3.4f,
                profilePct = 0.8f,
                feedPct = 0.0f
            )
        }

        val defaultInsights = createDynamicInsights(
            views = 1379,
            likes = 3,
            viewers = 362,
            caption = "IG model @piperrockelle recently sparked attentio ...",
            thumb = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            avgWatchTime = "16s",
            reshares = 1
        )

        val reelsList = listOf(
            ReelItem(
                id = "reel_1",
                thumbnailUrl = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=500&auto=format&fit=crop&q=80",
                viewsCount = 336,
                likesCount = 14,
                commentsCount = 2,
                resharesCount = 0,
                sendsCount = 1,
                savesCount = 0,
                caption = "Live reaction to the craziest moment on stream today 🔴 #live #stream",
                topOverlayText = "",
                watermarks = emptyList(),
                insightsData = createDynamicInsights(
                    views = 336,
                    likes = 14,
                    viewers = 120,
                    caption = "Live reaction to the craziest moment on stream today 🔴 #live #stream",
                    thumb = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=500&auto=format&fit=crop&q=80",
                    avgWatchTime = "9s",
                    comments = 2,
                    sends = 1
                )
            ),
            ReelItem(
                id = "reel_2",
                thumbnailUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
                viewsCount = 1379,
                likesCount = 3,
                commentsCount = 0,
                resharesCount = 1,
                sendsCount = 0,
                savesCount = 0,
                caption = "IG model @piperrockelle recently sparked attentio ...",
                topOverlayText = "",
                watermarks = emptyList(),
                insightsData = defaultInsights
            ),
            ReelItem(
                id = "reel_3",
                thumbnailUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
                viewsCount = 160,
                likesCount = 8,
                commentsCount = 0,
                resharesCount = 0,
                sendsCount = 0,
                savesCount = 1,
                caption = "Outfit of the day in pink sweater 🌸💫 #ootd #aesthetic",
                topOverlayText = "",
                watermarks = emptyList(),
                insightsData = createDynamicInsights(
                    views = 160,
                    likes = 8,
                    viewers = 85,
                    caption = "Outfit of the day in pink sweater 🌸💫 #ootd #aesthetic",
                    thumb = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
                    avgWatchTime = "6s",
                    saves = 1
                )
            ),
            ReelItem(
                id = "reel_4",
                thumbnailUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=500&auto=format&fit=crop&q=80",
                viewsCount = 2140,
                likesCount = 84,
                commentsCount = 6,
                resharesCount = 4,
                sendsCount = 3,
                savesCount = 12,
                caption = "Anime art portrait transformation process 🎨✨ #illustration #art",
                topOverlayText = "",
                watermarks = emptyList(),
                insightsData = createDynamicInsights(
                    views = 2140,
                    likes = 84,
                    viewers = 920,
                    caption = "Anime art portrait transformation process 🎨✨ #illustration #art",
                    thumb = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=500&auto=format&fit=crop&q=80",
                    avgWatchTime = "14s",
                    comments = 6,
                    reshares = 4,
                    sends = 3,
                    saves = 12
                )
            ),
            ReelItem(
                id = "reel_5",
                thumbnailUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
                viewsCount = 4510,
                likesCount = 192,
                commentsCount = 15,
                resharesCount = 8,
                sendsCount = 7,
                savesCount = 24,
                caption = "Studio lighting secret you need to know 💡 #creator #bts",
                topOverlayText = "",
                watermarks = emptyList(),
                insightsData = createDynamicInsights(
                    views = 4510,
                    likes = 192,
                    viewers = 1840,
                    caption = "Studio lighting secret you need to know 💡 #creator #bts",
                    thumb = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
                    avgWatchTime = "18s",
                    comments = 15,
                    reshares = 8,
                    sends = 7,
                    saves = 24
                )
            ),
            ReelItem(
                id = "reel_6",
                thumbnailUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
                viewsCount = 890,
                likesCount = 45,
                commentsCount = 3,
                resharesCount = 1,
                sendsCount = 2,
                savesCount = 5,
                caption = "Quick recap of the weekend event 🌇✨ #vlog #weekend",
                topOverlayText = "",
                watermarks = emptyList(),
                insightsData = createDynamicInsights(
                    views = 890,
                    likes = 45,
                    viewers = 410,
                    caption = "Quick recap of the weekend event 🌇✨ #vlog #weekend",
                    thumb = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
                    avgWatchTime = "11s",
                    comments = 3,
                    reshares = 1,
                    sends = 2,
                    saves = 5
                )
            )
        )

        return UserProfile(
            username = username,
            fullName = displayName,
            avatarUrl = avatarUrl,
            category = category,
            bio = bio,
            postsCount = posts,
            followersCount = followers,
            followingCount = following,
            monthlyViews = "10.4K views in the last 30 days.",
            reels = reelsList
        )
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
