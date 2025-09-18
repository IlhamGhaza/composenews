package com.igz.composenews.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.igz.composenews.ui.screens.home.ViewModels
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    encodedUrl: String,
    onBack: () -> Unit
) {
    val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
    val article = ViewModels.home.getArticleByUrl(url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = article?.source?.name ?: "Detail Berita", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            AsyncImage(
                model = article?.urlToImage,
                contentDescription = article?.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(text = article?.title ?: "(Tanpa judul)", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(text = "Sumber: ${article?.source?.name ?: "-"}")
            Spacer(Modifier.height(4.dp))
            Text(text = "Penulis: ${article?.author ?: "-"}")
            Spacer(Modifier.height(4.dp))
            Text(text = "Tanggal: ${article?.publishedAt ?: "-"}")
            Spacer(Modifier.height(12.dp))
            Text(text = article?.description ?: "")
            Spacer(Modifier.height(8.dp))
            Text(text = article?.content ?: "")
        }
    }
}
