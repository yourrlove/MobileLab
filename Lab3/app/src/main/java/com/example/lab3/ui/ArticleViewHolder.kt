/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lab3.ui

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import coil.load

//import com.example.lab3.data.createdText
import com.example.lab3.databinding.ArticleViewholderBinding
import com.example.lab3.model.Article
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

/**
 * View Holder for a [Article] RecyclerView list item.
 */
class ArticleViewHolder(
    private val binding: ArticleViewholderBinding,
    private val cardView: MaterialCardView = binding.root

) : RecyclerView.ViewHolder(binding.root) {

    fun bind(article: Article) {
        binding.apply {
            binding.title.text = article.title
            binding.description.text = article.description
            binding.source.text = article.source.name
            binding.created.text = formatDate(article.publishedAt)
            binding.ivImage.load(article.urlToImage) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }
        }
    }

    fun formatDate(isoDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")  // because 'Z' means UTC

        val outputFormat = SimpleDateFormat("HH:mm 'on' MMM dd, yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(isoDate)
            if (date != null) {
                outputFormat.format(date)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun setCardColor(sentiment: String) {
        val color = when (sentiment) {
            "positive" -> Color.argb(50, 76, 175, 80) // Light green
            "negative" -> Color.argb(50, 244, 67, 54) // Light red
            "neutral" -> Color.argb(50, 158, 158, 158) // Light gray
            else -> Color.argb(50, 158, 158, 158)
        }
        cardView.setCardBackgroundColor(color)
    }
}
