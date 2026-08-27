package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.IgGreen
import com.example.ui.theme.IgRed
import com.example.ui.theme.IgTextMuted

enum class MetricQualifier(val label: String, val color: Color) {
    HIGHER("Higher", IgGreen),
    LOWER("Lower", IgGreen),
    LOWER_RED("Lower", IgRed),
    TYPICAL("Typical", IgTextMuted)
}

data class ViewDataPoint(
    val dateLabel: String,
    val viewsThisReel: Float,
    val viewsTypical: Float
)

data class RetentionPoint(
    val timeLabel: String,
    val percent: Float
)

data class ReelInsightsData(
    val caption: String = "studio session, take 12 🎬",
    val handle: String = "your.page",
    val thumbnailUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
    val likes: Int = 28,
    val comments: Int = 0,
    val reshares: Int = 1,
    val sends: Int = 0,
    val saves: Int = 0,
    
    // Summary
    val views: Int = 1379,
    val viewers: Int = 362,
    val avgWatchTime: String = "16s",
    val follows: Int = 0,

    // What impacts your views
    val skipRate: Float = 11.9f,
    val skipRateQualifier: MetricQualifier = MetricQualifier.LOWER,
    val shareRate: Float = 0.0f,
    val shareRateQualifier: MetricQualifier = MetricQualifier.TYPICAL,
    val likeRate: Float = 0.8f,
    val likeRateQualifier: MetricQualifier = MetricQualifier.HIGHER,
    val saveRate: Float = 0.0f,
    val saveRateQualifier: MetricQualifier = MetricQualifier.TYPICAL,
    val repostRate: Float = 0.0f,
    val repostRateQualifier: MetricQualifier = MetricQualifier.TYPICAL,
    val commentRate: Float = 0.0f,
    val commentRateQualifier: MetricQualifier = MetricQualifier.TYPICAL,

    // Views Over Time chart points (Aug 17 to Aug 28)
    val viewsOverTime: List<ViewDataPoint> = listOf(
        ViewDataPoint("Aug 17", 0f, 0f),
        ViewDataPoint("Aug 18", 359f, 140f),
        ViewDataPoint("Aug 19", 359f, 140f),
        ViewDataPoint("Aug 20", 359f, 140f),
        ViewDataPoint("Aug 21", 359f, 140f),
        ViewDataPoint("Aug 22", 359f, 140f),
        ViewDataPoint("Aug 23", 359f, 140f),
        ViewDataPoint("Aug 24", 359f, 140f),
        ViewDataPoint("Aug 25", 359f, 140f),
        ViewDataPoint("Aug 26", 359f, 140f),
        ViewDataPoint("Aug 27", -1f, 140f),
        ViewDataPoint("Aug 28", -1f, 0f)
    ),

    // Retention curve (How long people watched)
    val retention: List<RetentionPoint> = listOf(
        RetentionPoint("0:00", 100f),
        RetentionPoint("0:01", 97f),
        RetentionPoint("0:02", 92f),
        RetentionPoint("0:03", 88f),
        RetentionPoint("0:04", 24f),
        RetentionPoint("0:05", 20f),
        RetentionPoint("0:06", 18f),
        RetentionPoint("0:07", 16f),
        RetentionPoint("0:08", 15f),
        RetentionPoint("0:09", 13f),
        RetentionPoint("0:10", 11f),
        RetentionPoint("0:11", 8f)
    ),

    // When people liked your reel
    val whenLiked: List<Float> = listOf(
        15f, 85f, 95f, 100f, 100f, 70f, 100f, 90f, 60f, 40f, 20f, 10f
    ),

    // Top sources of views
    val reelsTabPct: Float = 27.2f,
    val explorePct: Float = 4.4f,
    val profilePct: Float = 0.4f,
    val feedPct: Float = 0.1f,

    // Audience
    val followersAudiencePct: Float = 0.0f,
    val nonFollowersAudiencePct: Float = 100.0f,
    val ageDemographics: Map<String, Float> = mapOf(
        "13-17" to 6.6f,
        "18-24" to 40.3f,
        "25-34" to 34.4f,
        "35-44" to 11.2f,
        "45-54" to 4.5f,
        "55-64" to 1.9f,
        "65+" to 1.1f
    ),
    val countryDemographics: Map<String, Float> = mapOf(
        "India" to 44.0f,
        "United States" to 4.0f,
        "Brazil" to 4.0f,
        "Indonesia" to 3.7f,
        "Uzbekistan" to 3.4f
    ),
    val genderDemographics: Map<String, Float> = mapOf(
        "Men" to 67.6f,
        "Women" to 32.4f
    )
)
