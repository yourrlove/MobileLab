package com.example.mobilelab

import com.google.gson.annotations.SerializedName


data class SentimentRequest(
    @SerializedName("contents") val contents: List<Content>
)

data class Content(
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("text") val text: String
)


data class SentimentResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata,
    val modelVersion: String
)

data class Candidate(
    val content: ContentRes,
    val finishReason: String,
    val avgLogprobs: Double
)

data class ContentRes(
    val parts: List<Part>,
    val role: String
)

data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)