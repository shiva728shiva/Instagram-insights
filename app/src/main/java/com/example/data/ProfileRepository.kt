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
                            topOverlayText = if (index == 1) "Bellamy and Camilla talked for almost\n2 HOURS?! 😳" else (media.caption?.take(40) ?: "New Reel 🔥"),
                            watermarks = listOf("TIKTOK @$realUsername", "INSTAGRAM @$realUsername"),
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

        val defaultInsights = ReelInsightsData(
            caption = "IG model @piperrockelle recently sparked attentio ...",
            handle = username,
            thumbnailUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            likes = 3,
            comments = 0,
            reshares = 1,
            sends = 0,
            saves = 0,
            views = 1379,
            viewers = 362,
            avgWatchTime = "16s",
            follows = 0,
            skipRate = 11.9f,
            skipRateQualifier = MetricQualifier.LOWER,
            shareRate = 0.0f,
            shareRateQualifier = MetricQualifier.TYPICAL,
            likeRate = 0.8f,
            likeRateQualifier = MetricQualifier.HIGHER,
            saveRate = 0.0f,
            saveRateQualifier = MetricQualifier.TYPICAL,
            repostRate = 0.0f,
            repostRateQualifier = MetricQualifier.TYPICAL,
            commentRate = 0.0f,
            commentRateQualifier = MetricQualifier.TYPICAL,
            reelsTabPct = 22.1f,
            explorePct = 3.4f,
            profilePct = 0.8f,
            feedPct = 0.0f
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
                topOverlayText = "STREAM HIGHLIGHTS! 🔥",
                watermarks = listOf("TIKTOK @$username", "INSTAGRAM @$username"),
                insightsData = defaultInsights.copy(
                    views = 336,
                    viewers = 120,
                    likes = 14,
                    comments = 2,
                    avgWatchTime = "9s"
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
                topOverlayText = "Bellamy and Camilla talked for almost\n2 HOURS?! 😳",
                watermarks = listOf("TIKTOK @PIPERROCKELLE", "INSTAGRAM @PIPERROCKELLE"),
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
                topOverlayText = "Cozy vibes only ✨",
                watermarks = listOf("TIKTOK @$username", "INSTAGRAM @$username"),
                insightsData = defaultInsights.copy(
                    views = 160,
                    viewers = 85,
                    likes = 8,
                    avgWatchTime = "6s"
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
                topOverlayText = "Character design reveal ✨",
                watermarks = listOf("TIKTOK @$username", "INSTAGRAM @$username"),
                insightsData = defaultInsights.copy(
                    views = 2140,
                    viewers = 920,
                    likes = 84,
                    comments = 6,
                    avgWatchTime = "14s"
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
                topOverlayText = "Game changer for creators! ⚡",
                watermarks = listOf("TIKTOK @$username", "INSTAGRAM @$username"),
                insightsData = defaultInsights.copy(
                    views = 4510,
                    viewers = 1840,
                    likes = 192,
                    comments = 15,
                    avgWatchTime = "18s"
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
                topOverlayText = "Weekend dump! 📸",
                watermarks = listOf("TIKTOK @$username", "INSTAGRAM @$username"),
                insightsData = defaultInsights.copy(
                    views = 890,
                    viewers = 410,
                    likes = 45,
                    comments = 3,
                    avgWatchTime = "11s"
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
