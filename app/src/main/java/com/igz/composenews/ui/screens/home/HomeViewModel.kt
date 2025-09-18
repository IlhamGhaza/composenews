package com.igz.composenews.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igz.composenews.data.model.Article
import com.igz.composenews.data.repository.NewsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: NewsRepository = NewsRepository()
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val query: String = "",
        val articles: List<Article> = emptyList(),
        val error: String? = null,
        val favorites: Set<String> = emptySet() // store by article url
    )

    private val _state = MutableStateFlow(UiState(isLoading = true))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.getTopHeadlines(pageSize = 50)
            _state.value = result.fold(
                onSuccess = { list -> _state.value.copy(isLoading = false, articles = list) },
                onFailure = { e -> _state.value.copy(isLoading = false, error = e.message ?: "Unknown error") }
            )
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            if (newQuery.isBlank()) {
                refresh()
            } else {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val result = repository.search(newQuery.trim(), pageSize = 50)
                _state.value = result.fold(
                    onSuccess = { list ->
                        // Also ensure title contains keyword as requirement
                        val filtered = list.filter { it.title?.contains(newQuery, ignoreCase = true) == true }
                        _state.value.copy(isLoading = false, articles = filtered)
                    },
                    onFailure = { e -> _state.value.copy(isLoading = false, error = e.message ?: "Unknown error") }
                )
            }
        }
    }

    fun toggleFavorite(article: Article) {
        val url = article.url ?: return
        val current = _state.value.favorites.toMutableSet()
        if (!current.add(url)) {
            current.remove(url)
        }
        _state.value = _state.value.copy(favorites = current)
    }

    fun isFavorite(article: Article): Boolean {
        val url = article.url ?: return false
        return _state.value.favorites.contains(url)
    }

    fun getArticleByUrl(url: String?): Article? = _state.value.articles.find { it.url == url }
}
