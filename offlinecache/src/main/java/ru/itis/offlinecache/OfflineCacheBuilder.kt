package ru.itis.offlinecache

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.itis.offlinecache.interceptors.NetworkInterceptor
import ru.itis.offlinecache.interceptors.OfflineCacheInterceptor
import ru.itis.offlinecache.monitor.CacheMonitor
import java.io.File

/**
 * Главная точка входа в библиотеку.
 *
 * Использование в приложении:
 *
 * val builder = OfflineCacheBuilder(context)
 *     .cacheSize(10)
 *     .defaultDuration("5m")
 *     .enableLogging(true)
 *     .build()
 *
 * val okHttpClient = builder.okHttpClient
 * val monitor = builder.cacheMonitor
 */
class OfflineCacheBuilder(private val context: Context) {

    private var cacheSizeMb: Int = 10           // размер кэша по умолчанию 10 МБ
    private var defaultDurationSeconds: Int = 300  // 5 минут
    private var staleDays: Int = 7              // устаревшие данные хранить 7 дней
    private var loggingEnabled: Boolean = false // логи выключены по умолчанию

    /** Размер кэша в мегабайтах */
    fun cacheSize(mb: Int): OfflineCacheBuilder {
        this.cacheSizeMb = mb
        return this
    }

    /** Как долго данные считаются свежими. Форматы: "30s", "10m", "2h", "1d" */
    fun defaultDuration(duration: String): OfflineCacheBuilder {
        this.defaultDurationSeconds = parseDuration(duration)
        return this
    }

    /** Сколько дней хранить устаревшие данные (используются офлайн) */
    fun staleDays(days: Int): OfflineCacheBuilder {
        this.staleDays = days
        return this
    }

    /** Включить логи — удобно при разработке */
    fun enableLogging(enabled: Boolean): OfflineCacheBuilder {
        this.loggingEnabled = enabled
        return this
    }

    /** Собрать всё вместе и вернуть готовый результат */
    fun build(): OfflineCacheResult {
        // Создаём кэш на диске
        val cacheDir = File(context.cacheDir, "offline_cache")
        val cacheSizeBytes = cacheSizeMb * 1024L * 1024L
        val cache = Cache(cacheDir, cacheSizeBytes)

        // Создаём проверку сети
        val networkChecker = NetworkChecker(context)

        // Создаём интерсепторы
        val offlineInterceptor = OfflineCacheInterceptor(
            networkChecker = networkChecker,
            defaultMaxAgeSeconds = defaultDurationSeconds,
            defaultStaleDays = staleDays
        )
        val networkInterceptor = NetworkInterceptor(
            maxAgeSeconds = defaultDurationSeconds
        )

        // Собираем OkHttpClient
        val clientBuilder = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineInterceptor)       // application interceptor
            .addNetworkInterceptor(networkInterceptor) // network interceptor

        // Добавляем логи если нужно
        if (loggingEnabled) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            clientBuilder.addInterceptor(logging)
        }

        val okHttpClient = clientBuilder.build()
        val cacheMonitor = CacheMonitor(cache)

        return OfflineCacheResult(
            okHttpClient = okHttpClient,
            cacheMonitor = cacheMonitor,
            networkChecker = networkChecker
        )
    }

    /**
     * Парсит строку длительности в секунды.
     * "30s" → 30, "5m" → 300, "2h" → 7200, "1d" → 86400
     */
    private fun parseDuration(duration: String): Int {
        if (duration.isBlank()) return 300

        val number = duration.dropLast(1).toIntOrNull() ?: return 300

        return when (duration.last().lowercaseChar()) {
            's' -> number                    // секунды
            'm' -> number * 60              // минуты → секунды
            'h' -> number * 60 * 60         // часы → секунды
            'd' -> number * 60 * 60 * 24   // дни → секунды
            else -> 300
        }
    }

    /**
     * Результат сборки — всё что нужно приложению
     */
    data class OfflineCacheResult(
        val okHttpClient: OkHttpClient,     // передаётся в Retrofit
        val cacheMonitor: CacheMonitor,     // передаётся в дашборд
        val networkChecker: NetworkChecker  // можно использовать в UI
    )
}