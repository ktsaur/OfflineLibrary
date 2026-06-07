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
import ru.itis.offlinelibrary.viewmodel.NewsViewModel

@Composable
fun DashboardScreen(viewModel: NewsViewModel, modifier: Modifier = Modifier) {

    val cacheStats by viewModel.cacheStats.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Статус сети
        item {
            Text(
                text = "Монитор кэша",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline)
                        Color(0xFFD4EDDA) else Color(0xFFF8D7DA)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOnline) "🟢" else "🔴",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOnline) "Подключено к сети" else "Нет подключения",
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) Color(0xFF155724) else Color(0xFF721C24)
                        )
                        Text(
                            text = if (isOnline) "Данные загружаются с сервера"
                            else "Данные берутся из кэша",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Статистика кэша
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Использование кэша",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    cacheStats?.let { stats ->
                        // Прогресс-бар заполненности
                        LinearProgressIndicator(
                            progress = { stats.usagePercent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stats.sizeKb} КБ занято",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "из ${stats.maxSizeKb} КБ",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Закэшировано запросов: ${stats.requestsCount}",
                            fontSize = 13.sp
                        )
                    } ?: Text(
                        text = "Загрузка статистики...",
                        color = Color.Gray
                    )
                }
            }
        }

        // Кнопки управления
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Обновить статистику
                OutlinedButton(
                    onClick = { viewModel.refreshCacheStats() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔄 Обновить")
                }

                // Очистить кэш
                Button(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC3545)
                    )
                ) {
                    Text("🗑 Очистить кэш")
                }
            }
        }

        // Список закэшированных URL
        item {
            Text(
                text = "Закэшированные запросы",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        val urls = cacheStats?.cachedUrls ?: emptyList()
        if (urls.isEmpty()) {
            item {
                Text(
                    text = "Кэш пуст",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(urls) { url ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F9FA)
                    )
                ) {
                    Text(
                        text = url,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}