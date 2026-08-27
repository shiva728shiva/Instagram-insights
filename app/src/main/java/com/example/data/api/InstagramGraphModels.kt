package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IgUserResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "biography") val biography: String? = null,
    @Json(name = "profile_picture_url") val profilePictureUrl: String? = null,
    @Json(name = "media_count") val mediaCount: Int? = null,
    @Json(name = "followers_count") val followersCount: Int? = null,
    @Json(name = "follows_count") val followsCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class IgMediaListResponse(
    @Json(name = "data") val data: List<IgMediaItem>? = null
)

@JsonClass(generateAdapter = true)
data class IgMediaItem(
    @Json(name = "id") val id: String,
    @Json(name = "caption") val caption: String? = null,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "permalink") val permalink: String? = null,
    @Json(name = "like_count") val likeCount: Int? = null,
    @Json(name = "comments_count") val commentsCount: Int? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "insights") val insights: IgInsightsListResponse? = null
)

@JsonClass(generateAdapter = true)
data class IgInsightsListResponse(
    @Json(name = "data") val data: List<IgInsightMetric>? = null
)

@JsonClass(generateAdapter = true)
data class IgInsightMetric(
    @Json(name = "name") val name: String? = null,
    @Json(name = "period") val period: String? = null,
    @Json(name = "values") val values: List<IgInsightValue>? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class IgInsightValue(
    @Json(name = "value") val value: Long? = null
)
