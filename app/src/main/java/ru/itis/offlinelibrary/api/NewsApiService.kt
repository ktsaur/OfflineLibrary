package ru.itis.offlinelibrary.api

import retrofit2.http.GET
import retrofit2.http.Query

// Модель одной новости — то что приходит с сервера
data class Article(
    val title: String,          // заголовок
    val description: String?,   // описание (может быть null)
    val url: String,            // ссылка на полную статью
    val publishedAt: String     // дата публикации
)

// Обёртка — сервер возвращает не список сразу, а объект с полем articles
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

// Интерфейс запросов
interface NewsApiService {

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = API_KEY
    ): NewsResponse

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String = API_KEY
    ): NewsResponse

    companion object {
        const val API_KEY = "5e1853c636ce44d0a86fd9caa010ea48"
        const val BASE_URL = "https://newsapi.org/"
    }
}