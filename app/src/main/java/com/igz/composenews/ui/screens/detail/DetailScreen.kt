package com.igz.composenews.ui.screens.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.igz.composenews.ui.screens.home.ViewModels
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    encodedUrl: String,
    onBack: () -> Unit
) {
    val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
    val article = ViewModels.home.getArticleByUrl(url)
    val context = LocalContext.current

    // remove truncation marker like "[+3507 chars]" which NewsAPI sometimes appends
    val cleanedContent = article?.content
        ?.replace(Regex("""\[\+\d+ chars]"""), "")
        ?: ""

    // State to hold fetched full article text (if we attempt to load it)
    val fullTextState = remember { mutableStateOf<String?>(null) }
    val isFetching = remember { mutableStateOf(false) }

    // Decide if the API content looks truncated and we should try to fetch full page
    val looksTruncated = remember(article?.content) {
        val c = article?.content
        if (c == null || c.isBlank()) return@remember true
        // marker like [+1234 chars]
        if (Regex("\\[\\+\\d+ chars]" ).containsMatchIn(c)) return@remember true
        // trailing ellipsis
        if (c.trim().endsWith("...") || c.trim().endsWith("…")) return@remember true
        false
    }

    // Try to fetch article page and extract text when it looks truncated
    LaunchedEffect(key1 = article?.url) {
        val articleUrl = article?.url
        if (articleUrl.isNullOrBlank()) return@LaunchedEffect
        if (!looksTruncated) return@LaunchedEffect

        try {
            isFetching.value = true
            val doc = withContext(Dispatchers.IO) {
                // set a user agent and timeout
                Jsoup.connect(articleUrl).userAgent("Mozilla/5.0 (Android)").timeout(10_000).get()
            }
            // Try common containers
            var extracted = doc.select("article").text()
            if (extracted.isBlank()) extracted = doc.select("main").text()
            if (extracted.isBlank()) {
                // fallback: join paragraph texts but keep some structure
                val paragraphs = doc.select("p").map { it.text() }.filter { it.isNotBlank() }
                extracted = paragraphs.joinToString("\n\n")
            }
            if (extracted.isNotBlank()) {
                fullTextState.value = extracted
            }
        } catch (e: Exception) {
            // network/parse failed — just leave fullTextState null
        } finally {
            isFetching.value = false
        }
    }

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
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
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

                // Prefer full fetched text if available; otherwise show cleaned content
                when {
                    fullTextState.value != null -> {
                        Text(text = fullTextState.value ?: "")
                    }
                    cleanedContent.isNotBlank() -> {
                        Text(text = cleanedContent)
                    }
                    isFetching.value -> {
                        Text(text = "Memuat isi lengkap...")
                    }
                    else -> {
                        Text(text = "(Tidak ada isi lengkap tersedia)")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // If the API returned a URL to full article, provide a button to open it in browser
                val articleUrl = article?.url
                if (!articleUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, articleUrl.toUri())
                        context.startActivity(intent)
                    }) {
                        Text(text = "Buka sumber asli")
                    }
                }
            }
        }
    }
}
