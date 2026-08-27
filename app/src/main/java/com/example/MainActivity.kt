package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EditorBottomSheet
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.ReelPlayerScreen
import com.example.ui.theme.IgBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ReelInsightsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ReelInsightsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelInsightsApp(
    viewModel: ReelInsightsViewModel = viewModel()
) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val viewsFilter by viewModel.viewsFilter.collectAsStateWithLifecycle()
    val audienceSubTab by viewModel.audienceSubTab.collectAsStateWithLifecycle()
    val isEditorOpen by viewModel.isEditorOpen.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = IgBackground
    ) { _ ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.REEL_INSIGHTS -> {
                    InsightsScreen(
                        data = data,
                        currentTab = currentTab,
                        loading = isLoading,
                        selectedViewsFilter = viewsFilter,
                        selectedAudienceSubTab = audienceSubTab,
                        onTabSelect = { viewModel.selectTab(it) },
                        onViewsFilterSelect = { viewModel.setViewsFilter(it) },
                        onAudienceSubTabSelect = { viewModel.setAudienceSubTab(it) },
                        onBackClick = { viewModel.navigateTo(AppScreen.REEL_FEED) },
                        onOpenEditor = { viewModel.setEditorOpen(true) }
                    )
                }

                AppScreen.REEL_FEED -> {
                    ReelPlayerScreen(
                        data = data,
                        onOpenInsights = { viewModel.navigateTo(AppScreen.REEL_INSIGHTS) },
                        onOpenEditor = { viewModel.setEditorOpen(true) }
                    )
                }
            }
        }

        // Live metrics customizer
        if (isEditorOpen) {
            EditorBottomSheet(
                data = data,
                sheetState = sheetState,
                onDismiss = { viewModel.setEditorOpen(false) },
                onSave = { caption, handle, views, viewers, avgWatch, follows, likes, comments, reshares, sends, saves, skipRate, skipQualifier, likeRate, likeQualifier, shareRate, saveRate, repostRate, commentRate ->
                    viewModel.updateMetrics(
                        caption = caption,
                        handle = handle,
                        views = views,
                        viewers = viewers,
                        avgWatchTime = avgWatch,
                        follows = follows,
                        likes = likes,
                        comments = comments,
                        reshares = reshares,
                        sends = sends,
                        saves = saves,
                        skipRate = skipRate,
                        skipRateQualifier = skipQualifier,
                        likeRate = likeRate,
                        likeRateQualifier = likeQualifier,
                        shareRate = shareRate,
                        saveRate = saveRate,
                        repostRate = repostRate,
                        commentRate = commentRate
                    )
                },
                onReset = {
                    viewModel.resetToDefaults()
                }
            )
        }
    }
}
