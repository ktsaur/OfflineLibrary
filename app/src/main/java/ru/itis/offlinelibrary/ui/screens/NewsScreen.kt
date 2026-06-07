package ru.itis.offlinelibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.itis.offlinelibrary.api.Article
import ru.itis.offlinelibrary.viewmodel.NewsUiState
import ru.itis.offlinelibrary.viewmodel.NewsViewModel

@Composable
fun NewsScreen(viewModel: NewsViewModel, modifier: Modifier = Modifier) {

    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    Column(modifier = modifier) {

        // Плашка "нет интернета" вверху экрана
        if (!isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD)
                    )
                ) {
                    Text(
                        text = "📡 Офлайн режим — показываем кэшированные данные",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF856404),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Содержимое в зависимости от состояния
        when (val state = uiState) {

            is NewsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Загружаем новости...")
                    }
                }
            }

            is NewsUiState.Success -> {
                // Плашка "данные из кэша"
                if (state.fromCache) {
                    Text(
                        text = "⚡ Данные из кэша",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.articles) { article ->
                        NewsCard(article = article)
                    }
                }
            }

            is NewsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(text = "😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.loadNews() }) {
                            Text("Попробовать снова")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
            article.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.publishedAt.take(10), // показываем только дату
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}