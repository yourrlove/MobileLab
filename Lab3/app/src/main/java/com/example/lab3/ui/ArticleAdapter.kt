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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.lab3.databinding.ArticleViewholderBinding
import com.example.lab3.model.Article
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import android.util.Log
import java.util.Locale

/**
 * Adapter for an [Article] [List].
 */
class ArticleAdapter(private val context: Context)  : PagingDataAdapter<Article, ArticleViewHolder>(ARTICLE_DIFF_CALLBACK) {

    private var classifier: NLClassifier? = null
    private val TAG = "ArticleAdapter"

    init {
        try {
            classifier = NLClassifier.createFromFile(context, "text_classification_v2.tflite")
            Log.d(TAG, "NLClassifier initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing NLClassifier: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ArticleViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)
        if (article != null) {
            holder.bind(article)
            try {
                if (classifier != null) {
                    Log.d(TAG, "Analyzing sentiment for title: ${article.title}")
                    val sentiment = getSentiment(article.title)
                    Log.d(TAG, "Sentiment result: $sentiment")
                    holder.setCardColor(sentiment)
                } else {
                    Log.e(TAG, "Classifier is null, using neutral sentiment")
                    holder.setCardColor("neutral")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing sentiment: ${e.message}")
                e.printStackTrace()
                holder.setCardColor("neutral")
            }
        }
    }

    override fun onViewRecycled(holder: ArticleViewHolder) {
        super.onViewRecycled(holder)
        holder.setCardColor("neutral") // Reset card color when recycled
    }

    private fun getSentiment(title: String): String {
        return try {
            Log.d(TAG, "Starting sentiment analysis for: $title")
            val results = classifier?.classify(title) ?: return "neutral"
            var positiveScore = 0.0f
            var negativeScore = 0.0f

            for (category in results) {
                Log.d(TAG, "Category: ${category.label}, Score: ${category.score}")
                when (category.label.lowercase(Locale.getDefault())) {
                    "positive" -> positiveScore = category.score
                    "negative" -> negativeScore = category.score
                }
            }

            val sentiment = when {
                positiveScore > 0.6 -> "positive"
                negativeScore > 0.6 -> "negative"
                else -> "neutral"
            }
            Log.d(TAG, "Final sentiment: $sentiment (positive: $positiveScore, negative: $negativeScore)")
            sentiment
        } catch (e: Exception) {
            Log.e(TAG, "Error in getSentiment: ${e.message}")
            e.printStackTrace()
            "neutral"
        }
    }

    fun close() {
        try {
            classifier?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing classifier: ${e.message}")
        }
    }

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem == newItem
        }
    }
}
