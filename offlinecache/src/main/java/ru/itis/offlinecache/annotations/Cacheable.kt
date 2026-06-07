package ru.itis.offlinecache.annotations

/**
 * Аннотация для пометки Retrofit-методов, которые нужно кэшировать.
 *
 * Пример использования:
 * @Cacheable(duration = "30m")
 * @GET("/news")
 * suspend fun getNews(): List<News>
 *
 * Форматы duration:
 * "30s" — 30 секунд
 * "10m" — 10 минут
 * "2h"  — 2 часа
 * "1d"  — 1 день
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cacheable(
    val duration: String = "5m",        // как долго данные считаются свежими
    val forceCache: Boolean = false     // если true — всегда брать из кэша, даже если есть интернет
)