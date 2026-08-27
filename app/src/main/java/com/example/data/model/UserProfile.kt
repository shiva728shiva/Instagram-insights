package com.example.data.model

data class ReelItem(
    val id: String,
    val thumbnailUrl: String,
    val videoUrl: String = "",
    val viewsCount: Int,
    val likesCount: Int = 28,
    val commentsCount: Int = 0,
    val resharesCount: Int = 1,
    val sendsCount: Int = 0,
    val savesCount: Int = 0,
    val caption: String,
    val topOverlayText: String = "",
    val watermarks: List<String> = listOf("TIKTOK @PIPERROCKELLE", "INSTAGRAM @PIPERROCKELLE"),
    val insightsData: ReelInsightsData
)

data class UserProfile(
    val username: String = "alishaasassy",
    val fullName: String = "Alisha",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
    val category: String = "Fan page",
    val bio: String = "20\nCapricorn\n`c5fe`",
    val postsCount: Int = 98,
    val followersCount: Int = 0,
    val followingCount: Int = 5,
    val monthlyViews: String = "10.4K views in the last 30 days.",
    val reels: List<ReelItem> = emptyList()
)
