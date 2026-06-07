package ru.itis.offlinecache.monitor

import okhttp3.Cache

/**
 * Собирает статистику о кэше.
 * Нужен для дашборда — показывает что внутри кэша.
 */
class CacheMonitor(private val cache: Cache) {

    /**
     * Размер кэша в килобайтах
     */
    fun getCacheSizeKb(): Long {
        return cache.size() / 1024
    }

    /**
     * Максимальный размер кэша в килобайтах
     */
    fun getMaxCacheSizeKb(): Long {
        return cache.maxSize() / 1024
    }

    /**
     * Процент заполненности кэша (0-100)
     */
    fun getCacheUsagePercent(): Int {
        val max = cache.maxSize()
        if (max == 0L) return 0
        return ((cache.size().toDouble() / max.toDouble()) * 100).toInt()
    }

    /**
     * Список всех закэшированных URL
     */
    fun getCachedUrls(): List<String> {
        return try {
            cache.urls().asSequence().toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Количество закэшированных запросов
     */
    fun getCachedRequestsCount(): Int {
        return getCachedUrls().size
    }

    /**
     * Полная статистика одним объектом — удобно для дашборда
     */
    fun getStats(): CacheStats {
        return CacheStats(
            sizeKb          = getCacheSizeKb(),
            maxSizeKb       = getMaxCacheSizeKb(),
            usagePercent    = getCacheUsagePercent(),
            cachedUrls      = getCachedUrls(),
            requestsCount   = getCachedRequestsCount()
        )
    }

    /**
     * Очистить весь кэш
     */
    fun clearCache() {
        try {
            cache.evictAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Модель данных для дашборда
     */
    data class CacheStats(
        val sizeKb: Long,           // текущий размер в КБ
        val maxSizeKb: Long,        // максимальный размер в КБ
        val usagePercent: Int,      // заполненность в процентах
        val cachedUrls: List<String>, // список URL
        val requestsCount: Int      // количество запросов
    )
}