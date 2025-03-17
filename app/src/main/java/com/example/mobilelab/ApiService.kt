package com.example.mobilelab

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    fun analyzeSentiment(
        @retrofit2.http.Query("key") apiKey: String,
        @retrofit2.http.Header("Content-Type") contentType: String,
        @Body requestBody: SentimentRequest
    ): Call<SentimentResponse>
}