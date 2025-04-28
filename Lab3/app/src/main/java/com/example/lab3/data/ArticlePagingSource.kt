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

package com.example.lab3.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.lab3.api.NewsAPIService
import com.example.lab3.model.Article
import com.google.gson.Gson
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import kotlin.math.max

// The initial key used for loading.
// This is the article id of the first article that will be loaded
private const val STARTING_KEY = 0
private const val LOAD_DELAY_MILLIS = 3_000L

private val firstArticleCreatedTime = LocalDateTime.now()

/**
 * A [PagingSource] that loads articles for paging. The [Int] is the paging key or query that is used to fetch more
 * data, and the [Article] specifies that the [PagingSource] fetches an [Article] [List].
 */
class ArticlePagingSource(
    private val service: NewsAPIService,
    private val query: String,
    private val apiKey: String,
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        return try {
            val response = service.searchArticles(
                query = query,
                page = page,
                pageSize = params.loadSize,
                apiKey = apiKey
            )

            LoadResult.Page(
                data = response.articles.mapIndexed { index, apiArticle ->
                    Article(
                        id = index,
                        source = apiArticle.source,
                        author = apiArticle.author,
                        title = apiArticle.title,
                        description = apiArticle.description,
                        url = apiArticle.url,
                        urlToImage = apiArticle.urlToImage,
                        publishedAt = apiArticle.publishedAt,
                        content = apiArticle.content
                    )
                },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page * params.loadSize < response.totalResults) page + 1 else null
            )

        } catch (e: Exception) {
            Log.d("error", e.message.toString())
            LoadResult.Error(e)
        }
    }

    // The refresh key is used for the initial load of the next PagingSource, after invalidation
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        // In our case we grab the item closest to the anchor position
        // then return its id - (state.config.pageSize / 2) as a buffer
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null

        return ensureValidKey(key = article.id - (state.config.pageSize / 2))
    }

    /**
     * Makes sure the paging key is never less than [STARTING_KEY]
     */
    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)
}
