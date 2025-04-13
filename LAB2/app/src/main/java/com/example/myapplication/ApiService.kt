package com.example.myapplication

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/predict")
    fun analyzeSentiment(
        @Body requestBody: SentimentRequest
    ): Call<SentimentResponse>
}