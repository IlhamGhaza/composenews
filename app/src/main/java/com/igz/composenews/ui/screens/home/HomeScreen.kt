package com.igz.composenews.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.igz.composenews.data.model.Article
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDetail: (encodedUrl: String) -> Unit,
    onOpenAbout: () -> Unit,
    vm: HomeViewModel = ViewModels.home
) {
    val state = vm.state.collectAsStateWithLifecycleCompat().value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Compose News") },
                actions = {
                    IconButton(onClick = onOpenAbout) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "about_page"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            var query by remember { mutableStateOf(state.query) }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    vm.onQueryChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari berita...") }
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.error ?: "Terjadi kesalahan")
                }

                state.articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Data kosong")
                }

                else -> ArticleList(
                    list = state.articles,
                    onClick = { article ->
                        val encoded = URLEncoder.encode(article.url ?: "", StandardCharsets.UTF_8.toString())
                        onOpenDetail(encoded)
                    },
                    onToggleFavorite = { vm.toggleFavorite(it) },
                    isFavorite = { vm.isFavorite(it) }
                )
            }
        }
    }
}

@Composable
private fun ArticleList(
    list: List<Article>,
    onClick: (Article) -> Unit,
    onToggleFavorite: (Article) -> Unit,
    isFavorite: (Article) -> Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(list) { item ->
            ArticleRow(
                article = item,
                onClick = { onClick(item) },
                onToggleFavorite = { onToggleFavorite(item) },
                favorite = isFavorite(item)
            )
        }
    }
}

@Composable
private fun ArticleRow(
    article: Article,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    favorite: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = article.urlToImage,
            contentDescription = article.title,
            modifier = Modifier
                .height(72.dp)
                .padding(end = 12.dp)
                .align(Alignment.CenterVertically)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title ?: "(Tanpa judul)",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = article.source?.name ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1
            )
        }
        IconButton(onClick = onToggleFavorite, modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(
                imageVector = if (favorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (favorite) "Hapus dari favorit" else "Tambah ke favorit",
                tint = if (favorite) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

// Simple helper to collect StateFlow in Compose without adding extra dependencies
@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateWithLifecycleCompat() =
    this.collectAsState(initial = this.value)

object ViewModels {
    // single instance ViewModel for simplicity in this sample
    val home: HomeViewModel by lazy { HomeViewModel() }
}
