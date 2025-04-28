package com.example.lab3.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface NewsAPIService {
    @GET("v2/everything?sortBy=popularity&apiKey=a514fa79f45f419fbf2e46049608a2f0")
    suspend fun searchArticles(
            @Query("q") query: String,
            @Query("page") page: Int,
            @Query("pageSize") itemsPerPage: Int
    ): RepoArticlesSearchResponse

    companion object {
        private const val BASE_URL = "https://newsapi.org/"

        fun create(): NewsAPIService {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(NewsAPIService::class.java)
        }
    }
}
