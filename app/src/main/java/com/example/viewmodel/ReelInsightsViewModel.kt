package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HealthyGraphGenerator
import com.example.data.ProfileRepository
import com.example.data.model.MetricQualifier
import com.example.data.model.ReelInsightsData
import com.example.data.model.ReelItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InsightsTab(val label: String) {
    OVERVIEW("Overview"),
    ENGAGEMENT("Engagement"),
    AUDIENCE("Audience")
}

enum class AppScreen {
    PROFILE,
    REEL_FEED,
    REEL_INSIGHTS
}

class ReelInsightsViewModel : ViewModel() {

    private val initialProfile = ProfileRepository.getProfileForUsername("alishaasassy")

    private val _userProfile = MutableStateFlow(initialProfile)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _activeReel = MutableStateFlow(initialProfile.reels.getOrElse(1) { initialProfile.reels.first() })
    val activeReel: StateFlow<ReelItem> = _activeReel.asStateFlow()

    private val _data = MutableStateFlow(initialProfile.reels.getOrElse(1) { initialProfile.reels.first() }.insightsData)
    val data: StateFlow<ReelInsightsData> = _data.asStateFlow()

    // Start on Profile Screen as requested
    private val _currentScreen = MutableStateFlow(AppScreen.PROFILE)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(InsightsTab.OVERVIEW)
    val currentTab: StateFlow<InsightsTab> = _currentTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _viewsFilter = MutableStateFlow("All")
    val viewsFilter: StateFlow<String> = _viewsFilter.asStateFlow()

    private val _audienceSubTab = MutableStateFlow("Age")
    val audienceSubTab: StateFlow<String> = _audienceSubTab.asStateFlow()

    private val _isEditorOpen = MutableStateFlow(false)
    val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

    private val _showUsernamePrompt = MutableStateFlow(false)
    val showUsernamePrompt: StateFlow<Boolean> = _showUsernamePrompt.asStateFlow()

    init {
        // Auto-fetch real Instagram Graph API data on launch
        loadUsername("alishaasassy")
    }

    fun loadUsername(username: String) {
        val base = ProfileRepository.getProfileForUsername(username)
        _userProfile.value = base
        val defaultReel = base.reels.getOrElse(1) { base.reels.first() }
        _activeReel.value = defaultReel
        _data.value = defaultReel.insightsData
        _showUsernamePrompt.value = false

        viewModelScope.launch {
            _isLoading.value = true
            val realProfile = ProfileRepository.fetchProfileWithRealApi(username)
            _userProfile.value = realProfile
            val active = realProfile.reels.getOrElse(1) { realProfile.reels.firstOrNull() ?: defaultReel }
            _activeReel.value = active
            _data.value = active.insightsData
            _isLoading.value = false
        }
    }

    fun selectReel(reel: ReelItem) {
        _activeReel.value = reel
        _data.value = reel.insightsData
        _currentScreen.value = AppScreen.REEL_FEED
    }

    fun setShowUsernamePrompt(show: Boolean) {
        _showUsernamePrompt.value = show
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.REEL_INSIGHTS) {
            triggerLoading(600)
        }
    }

    fun selectTab(tab: InsightsTab) {
        _currentTab.value = tab
        triggerLoading(400)
    }

    fun setViewsFilter(filter: String) {
        _viewsFilter.value = filter
    }

    fun setAudienceSubTab(subTab: String) {
        _audienceSubTab.value = subTab
    }

    fun setEditorOpen(open: Boolean) {
        _isEditorOpen.value = open
    }

    fun triggerLoading(durationMillis: Long = 600) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(durationMillis)
            _isLoading.value = false
        }
    }

    fun updateMetrics(
        caption: String? = null,
        handle: String? = null,
        thumbnailUrl: String? = null,
        views: Int? = null,
        viewers: Int? = null,
        avgWatchTime: String? = null,
        follows: Int? = null,
        likes: Int? = null,
        comments: Int? = null,
        reshares: Int? = null,
        sends: Int? = null,
        saves: Int? = null,
        skipRate: Float? = null,
        skipRateQualifier: MetricQualifier? = null,
        likeRate: Float? = null,
        likeRateQualifier: MetricQualifier? = null,
        shareRate: Float? = null,
        saveRate: Float? = null,
        repostRate: Float? = null,
        commentRate: Float? = null,
        reelsTabPct: Float? = null,
        explorePct: Float? = null,
        profilePct: Float? = null,
        feedPct: Float? = null,
        followersAudiencePct: Float? = null,
        nonFollowersAudiencePct: Float? = null,
        countryDemographics: Map<String, Float>? = null,
        ageDemographics: Map<String, Float>? = null,
        genderDemographics: Map<String, Float>? = null
    ) {
        _data.update { current ->
            val newViews = views ?: current.views
            val newLikes = likes ?: current.likes
            val viewsChanged = views != null && views != current.views

            // Compute healthy graphs and metrics if views changed or if graphs need refresh
            val healthyViewsOverTime = if (viewsChanged) {
                HealthyGraphGenerator.generateViewsOverTime(totalViews = newViews)
            } else current.viewsOverTime

            val durationSec = ((current.videoDurationMs / 1000L).toInt()).coerceIn(4, 60)
            val healthyRetention = if (viewsChanged) {
                HealthyGraphGenerator.generateRetentionCurve(durationSeconds = durationSec, totalViews = newViews)
            } else current.retention

            val healthyWhenLiked = if (viewsChanged || (likes != null && likes != current.likes)) {
                HealthyGraphGenerator.generateWhenLiked(durationSeconds = durationSec, likesCount = newLikes)
            } else current.whenLiked

            val updated = current.copy(
                caption = caption ?: current.caption,
                handle = handle ?: current.handle,
                thumbnailUrl = thumbnailUrl ?: current.thumbnailUrl,
                views = newViews,
                viewers = viewers ?: if (viewsChanged) (newViews * 0.74).toInt().coerceAtLeast(1) else current.viewers,
                avgWatchTime = avgWatchTime ?: current.avgWatchTime,
                follows = follows ?: current.follows,
                likes = newLikes,
                comments = comments ?: current.comments,
                reshares = reshares ?: current.reshares,
                sends = sends ?: current.sends,
                saves = saves ?: current.saves,
                skipRate = skipRate ?: current.skipRate,
                skipRateQualifier = skipRateQualifier ?: current.skipRateQualifier,
                likeRate = likeRate ?: if (viewsChanged) ((newLikes.toFloat() / newViews.coerceAtLeast(1)) * 100f) else current.likeRate,
                likeRateQualifier = likeRateQualifier ?: current.likeRateQualifier,
                shareRate = shareRate ?: current.shareRate,
                saveRate = saveRate ?: current.saveRate,
                repostRate = repostRate ?: current.repostRate,
                commentRate = commentRate ?: current.commentRate,
                reelsTabPct = reelsTabPct ?: current.reelsTabPct,
                explorePct = explorePct ?: current.explorePct,
                profilePct = profilePct ?: current.profilePct,
                feedPct = feedPct ?: current.feedPct,
                followersAudiencePct = followersAudiencePct ?: current.followersAudiencePct,
                nonFollowersAudiencePct = nonFollowersAudiencePct ?: current.nonFollowersAudiencePct,
                countryDemographics = countryDemographics ?: current.countryDemographics,
                ageDemographics = ageDemographics ?: current.ageDemographics,
                genderDemographics = genderDemographics ?: current.genderDemographics,
                viewsOverTime = healthyViewsOverTime,
                retention = healthyRetention,
                whenLiked = healthyWhenLiked
            )
            // Sync active reel
            _activeReel.update { it.copy(
                caption = updated.caption,
                insightsData = updated,
                viewsCount = updated.views,
                likesCount = updated.likes,
                commentsCount = updated.comments,
                resharesCount = updated.reshares,
                sendsCount = updated.sends,
                savesCount = updated.saves
            ) }
            updated
        }
    }

    fun updateProfile(
        username: String? = null,
        fullName: String? = null,
        avatarUrl: String? = null,
        category: String? = null,
        bio: String? = null,
        postsCount: Int? = null,
        followersCount: Int? = null,
        followingCount: Int? = null
    ) {
        _userProfile.update { current ->
            val updated = current.copy(
                username = username ?: current.username,
                fullName = fullName ?: current.fullName,
                avatarUrl = avatarUrl ?: current.avatarUrl,
                category = category ?: current.category,
                bio = bio ?: current.bio,
                postsCount = postsCount ?: current.postsCount,
                followersCount = followersCount ?: current.followersCount,
                followingCount = followingCount ?: current.followingCount
            )
            if (username != null) {
                _data.update { it.copy(handle = username) }
            }
            if (avatarUrl != null) {
                _data.update { it.copy(thumbnailUrl = avatarUrl) }
            }
            updated
        }
    }

    fun updateActiveReelCaption(newCaption: String) {
        _activeReel.update { it.copy(caption = newCaption) }
        _data.update { it.copy(caption = newCaption) }
    }

    fun updateActiveReelAvatar(newAvatarUrl: String) {
        _userProfile.update { it.copy(avatarUrl = newAvatarUrl) }
        _activeReel.update { it.copy(thumbnailUrl = newAvatarUrl) }
        _data.update { it.copy(thumbnailUrl = newAvatarUrl) }
    }

    fun resetToDefaults() {
        val defaultReel = _userProfile.value.reels.getOrElse(1) { _userProfile.value.reels.first() }
        _data.value = defaultReel.insightsData
    }

    fun loadSelectedVideo(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videoInfo = com.example.data.VideoMediaManager.extractVideoInfo(context, uri)
                val newReel = com.example.data.VideoMediaManager.buildReelItemFromVideo(
                    videoInfo = videoInfo,
                    username = _userProfile.value.username
                )

                // Add to user profile reels at top
                _userProfile.update { current ->
                    val updatedReels = listOf(newReel) + current.reels
                    current.copy(
                        reels = updatedReels,
                        postsCount = current.postsCount + 1
                    )
                }

                _activeReel.value = newReel
                _data.value = newReel.insightsData
                _currentScreen.value = AppScreen.REEL_INSIGHTS
            } catch (e: Exception) {
                android.util.Log.e("ReelInsightsViewModel", "Error loading selected video: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
