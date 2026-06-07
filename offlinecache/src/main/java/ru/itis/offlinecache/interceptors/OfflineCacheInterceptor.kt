package ru.itis.offlinecache.interceptors

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import ru.itis.offlinecache.NetworkChecker
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Главный интерсептор библиотеки.
 *
 * Что он делает:
 * 1. Перехватывает каждый запрос
 * 2. Проверяет интернет
 * 3. Если интернет есть  → идёт на сервер, сохраняет копию в кэш
 * 4. Если интернета нет  → берёт данные из кэша
 * 5. Если сервер упал    → тоже берёт из кэша (fallback)
 */
class OfflineCacheInterceptor(
    private val networkChecker: NetworkChecker,
    private val defaultMaxAgeSeconds: Int = 300,      // 5 минут — свежие данные
    private val defaultStaleDays: Int = 7             // 7 дней — устаревшие, но лучше чем ничего
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (networkChecker.isConnected()) {
            // Интернет есть — идём на сервер
            // Говорим: данные актуальны defaultMaxAgeSeconds секунд
            request = request.newBuilder()
                .cacheControl(
                    CacheControl.Builder()
                        .maxAge(defaultMaxAgeSeconds, TimeUnit.SECONDS)
                        .build()
                )
                .build()

            return try {
                val response = chain.proceed(request)
                response
            } catch (e: IOException) {
                // Сервер не ответил — используем кэш как запасной вариант (fallback)
                proceedWithCache(chain, request, defaultStaleDays)
            }

        } else {
            // Интернета нет — сразу берём из кэша
            return proceedWithCache(chain, request, defaultStaleDays)
        }
    }

    /**
     * Выполняет запрос используя только кэш.
     * maxStaleDays — сколько дней устаревшие данные ещё считаются допустимыми
     */
    private fun proceedWithCache(
        chain: Interceptor.Chain,
        originalRequest: okhttp3.Request,
        maxStaleDays: Int
    ): Response {
        val cacheRequest = originalRequest.newBuilder()
            .cacheControl(
                CacheControl.Builder()
                    .onlyIfCached()                          // брать ТОЛЬКО из кэша
                    .maxStale(maxStaleDays, TimeUnit.DAYS)  // принять данные до maxStaleDays старости
                    .build()
            )
            .build()

        return try {
            chain.proceed(cacheRequest)
        } catch (e: IOException) {
            // Кэш тоже пуст — возвращаем пустой ответ с кодом 504
            // 504 = Gateway Timeout, стандартный код для "нет данных"
            okhttp3.Response.Builder()
                .request(originalRequest)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(504)
                .message("No internet and no cache available")
                .body(okhttp3.ResponseBody.create(null, ""))
                .build()
        }
    }
}