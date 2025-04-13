package com.example.myapplication

import com.google.gson.annotations.SerializedName


data class SentimentRequest(
    @SerializedName("sentence") val sentence: String
)

data class Content(
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("sentence") val sentence: String
)

data class SentimentResponse(
    val result: Result,
    val success: Boolean
)

data class Result(
    val label: String,
    val scores: Score,
)

data class Score(
    val NEG: Float,
    val NEU: Float,
    val POS: Float
)