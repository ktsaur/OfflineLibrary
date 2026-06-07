package ru.itis.offlinelibrary.remote

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.itis.offlinecache.OfflineCacheBuilder
import ru.itis.offlinelibrary.api.NewsApiService

object RetrofitClient {

    private var instance: NewsApiService? = null

    fun getInstance(context: Context): NewsApiService {
        if (instance == null) {
            instance = create(context)
        }
        return instance!!
    }

    private fun create(context: Context): NewsApiService {

        // Вот здесь подключаем твою библиотеку!
        // Всего 4 строчки вместо 50 строк ручного кода
        val cacheResult = OfflineCacheBuilder(context)
            .cacheSize(10)              // кэш 10 МБ
            .defaultDuration("5m")      // данные свежие 5 минут
            .staleDays(7)               // офлайн данные хранить 7 дней
            .enableLogging(true)        // включаем логи для разработки
            .build()

        // Передаём готовый OkHttpClient в Retrofit
        return Retrofit.Builder()
            .baseUrl(NewsApiService.BASE_URL)
            .client(cacheResult.okHttpClient)   // ← вот тут подключается библиотека
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}