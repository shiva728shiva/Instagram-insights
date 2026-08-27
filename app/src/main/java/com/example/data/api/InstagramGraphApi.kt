package com.example.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InstagramGraphApi {

    @GET("{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: String,
        @Query("fields") fields: String = "id,username,name,biography,profile_picture_url,media_count,followers_count,follows_count",
        @Query("access_token") accessToken: String
    ): Response<IgUserResponse>

    @GET("{userId}/media")
    suspend fun getUserMedia(
        @Path("userId") userId: String,
        @Query("fields") fields: String = "id,caption,media_type,media_url,thumbnail_url,permalink,like_count,comments_count,timestamp,insights.metric(plays,reach,saved,shares,total_interactions)",
        @Query("access_token") accessToken: String,
        @Query("limit") limit: Int = 25
    ): Response<IgMediaListResponse>

    @GET("me")
    suspend fun getMe(
        @Query("fields") fields: String = "id,username,account_type,media_count",
        @Query("access_token") accessToken: String
    ): Response<IgUserResponse>
}
