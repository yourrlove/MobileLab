package com.example.lab3.api


import com.example.lab3.model.Article
import com.google.gson.annotations.SerializedName

data class RepoArticlesSearchResponse(
    @SerializedName("status") val status: String,
    @SerializedName("totalResults") val total: Int = 0,
    @SerializedName("articles") val items: List<Article> = emptyList(),
)
