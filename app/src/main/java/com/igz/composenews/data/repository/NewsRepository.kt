package com.igz.composenews.data.repository

import com.igz.composenews.data.model.Article
import com.igz.composenews.data.remote.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsRepository {
    private val api = NetworkModule.newsApi

    suspend fun getTopHeadlines(country: String = "us", pageSize: Int = 50): Result<List<Article>> =
        safeCall { api.getTopHeadlines(country = country, pageSize = pageSize).articles.orEmpty() }

    suspend fun search(query: String, pageSize: Int = 50): Result<List<Article>> =
        safeCall { api.searchEverything(query = query, pageSize = pageSize).articles.orEmpty() }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
