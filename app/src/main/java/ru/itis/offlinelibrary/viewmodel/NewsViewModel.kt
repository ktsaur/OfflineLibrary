package ru.itis.offlinelibrary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.itis.offlinecache.OfflineCacheBuilder
import ru.itis.offlinecache.monitor.CacheMonitor
import ru.itis.offlinelibrary.remote.RetrofitClient
import ru.itis.offlinelibrary.api.Article
import java.io.File

// Все возможные состояния экрана с новостями
sealed class NewsUiState {
    object Loading : NewsUiState()                    // загружается
    data class Success(val articles: List<Article>,
                       val fromCache: Boolean = false) : NewsUiState() // загружено
    data class Error(val message: String) : NewsUiState() // ошибка
}

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Состояние экрана новостей
    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState

    // Статистика кэша для дашборда
    private val _cacheStats = MutableStateFlow<CacheMonitor.CacheStats?>(null)
    val cacheStats: StateFlow<CacheMonitor.CacheStats?> = _cacheStats

    // Состояние сети для UI
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline

    // Создаём монитор кэша
    private val cacheMonitor: CacheMonitor by lazy {
        val cacheDir = File(context.cacheDir, "offline_cache")
        val cache = okhttp3.Cache(cacheDir, 10 * 1024L * 1024L)
        CacheMonitor(cache)
    }

    // Создаём проверку сети
    private val networkChecker by lazy {
        ru.itis.offlinecache.NetworkChecker(context)
    }

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            _isOnline.value = networkChecker.isConnected()

            try {
                val response = RetrofitClient
                    .getInstance(context)
                    .getTopHeadlines()

                _uiState.value = NewsUiState.Success(
                    articles = response.articles,
                    fromCache = !networkChecker.isConnected()
                )

            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error(
                    message = if (!networkChecker.isConnected()) {
                        "Нет интернета и кэш пуст. Загрузите новости хотя бы раз при наличии сети."
                    } else {
                        "Ошибка загрузки: ${e.message}"
                    }
                )
            }

            // Обновляем статистику кэша
            refreshCacheStats()
        }
    }

    fun refreshCacheStats() {
        _cacheStats.value = cacheMonitor.getStats()
    }

    fun clearCache() {
        cacheMonitor.clearCache()
        refreshCacheStats()
    }
}