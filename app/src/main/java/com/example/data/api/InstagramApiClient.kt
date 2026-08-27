package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object InstagramApiClient {

    const val DEFAULT_ACCOUNT_NAME = "Alisha"
    const val DEFAULT_ACCOUNT_ID = "17841442557421472"
    const val DEFAULT_ACCESS_TOKEN = "IGAAVtqaqL2e9BZAFlNc2ltTkZAtWmZAKOG1wY19hTHVIc21xODBJbWdwQUJZAdExLY0lvc3l1dEtoU3BLVU1QQU9Ic2dhR0tCMjJGNmhjdlZAUZAE5nUHV1RjRCQ3dtRHJhTlhjRkdwTVhKMmp1QzJtLU84NzVuNGkzN3hNR0ZA3bnZAYZAwZDZD"

    private const val BASE_URL = "https://graph.facebook.com/v21.0/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: InstagramGraphApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(InstagramGraphApi::class.java)
    }
}
