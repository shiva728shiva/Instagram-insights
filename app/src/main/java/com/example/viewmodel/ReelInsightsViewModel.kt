package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MetricQualifier
import com.example.data.model.ReelInsightsData
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
    REEL_FEED,
    REEL_INSIGHTS
}

class ReelInsightsViewModel : ViewModel() {

    private val _data = MutableStateFlow(ReelInsightsData())
    val data: StateFlow<ReelInsightsData> = _data.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.REEL_INSIGHTS)
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

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.REEL_INSIGHTS) {
            triggerLoading(800)
        }
    }

    fun selectTab(tab: InsightsTab) {
        _currentTab.value = tab
        triggerLoading(500)
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

    fun triggerLoading(durationMillis: Long = 700) {
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
        commentRate: Float? = null
    ) {
        _data.update { current ->
            current.copy(
                caption = caption ?: current.caption,
                handle = handle ?: current.handle,
                thumbnailUrl = thumbnailUrl ?: current.thumbnailUrl,
                views = views ?: current.views,
                viewers = viewers ?: current.viewers,
                avgWatchTime = avgWatchTime ?: current.avgWatchTime,
                follows = follows ?: current.follows,
                likes = likes ?: current.likes,
                comments = comments ?: current.comments,
                reshares = reshares ?: current.reshares,
                sends = sends ?: current.sends,
                saves = saves ?: current.saves,
                skipRate = skipRate ?: current.skipRate,
                skipRateQualifier = skipRateQualifier ?: current.skipRateQualifier,
                likeRate = likeRate ?: current.likeRate,
                likeRateQualifier = likeRateQualifier ?: current.likeRateQualifier,
                shareRate = shareRate ?: current.shareRate,
                saveRate = saveRate ?: current.saveRate,
                repostRate = repostRate ?: current.repostRate,
                commentRate = commentRate ?: current.commentRate
            )
        }
    }

    fun resetToDefaults() {
        _data.value = ReelInsightsData()
    }
}
