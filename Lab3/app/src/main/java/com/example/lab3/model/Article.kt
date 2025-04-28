package com.example.lab3.model

import com.google.gson.annotations.SerializedName

data class Article (

    val id : Int,

    @SerializedName("source")
    val source: Source, // ✅ object, not String

    @SerializedName("author")
    val author: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("url")
    val url: String,

    @SerializedName("urlToImage")
    val urlToImage: String?,

    @SerializedName("publishedAt")
    val publishedAt: String,

    @SerializedName("content")
    val content: String?
)

data class Source(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String
)
