package ru.itis.offlinecache.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Сетевой интерсептор — работает только когда есть реальный запрос к серверу.
 *
 * Его задача — добавить заголовки к ответу сервера,
 * чтобы OkHttp знал, как долго хранить ответ в кэше.
 *
 * Разница от OfflineCacheInterceptor:
 * - OfflineCacheInterceptor — решает ОТКУДА взять данные (сервер или кэш)
 * - NetworkInterceptor      — говорит КАК ДОЛГО хранить ответ сервера
 */
class NetworkInterceptor(
    private val maxAgeSeconds: Int = 300  // 5 минут по умолчанию
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        // Добавляем заголовок Cache-Control к ответу сервера
        // Это говорит OkHttp: "храни этот ответ maxAgeSeconds секунд"
        return response.newBuilder()
            .header("Cache-Control", "public, max-age=$maxAgeSeconds")
            .removeHeader("Pragma") // Pragma: no-cache мешает кэшированию, убираем
            .build()
    }
}