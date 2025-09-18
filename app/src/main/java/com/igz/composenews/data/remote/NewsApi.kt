package com.igz.composenews.data.remote

import com.igz.composenews.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    // Top headlines for default country (e.g., us)
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("pageSize") pageSize: Int = 30
    ): NewsResponse

    // Search all articles
    @GET("v2/everything")
    suspend fun searchEverything(
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int = 30,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("language") language: String = "en"
    ): NewsResponse
}
