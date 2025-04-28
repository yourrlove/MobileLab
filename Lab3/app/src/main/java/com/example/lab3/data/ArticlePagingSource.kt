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
    private val query: String
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
//        // If params.key is null, it is the first load, so we start loading with STARTING_KEY
//        val startKey = params.key ?: STARTING_KEY
//
//        // We fetch as many articles as hinted to by params.loadSize
//        val range = startKey.until(startKey + params.loadSize)

//        // Simulate a delay for loads adter the initial load
//        if (startKey != STARTING_KEY) delay(LOAD_DELAY_MILLIS)
//        return LoadResult.Page(
//            data = range.map { number ->
//                Article(
//                    id = number,
//                    title = "Article $number",
//                    description = "This describes article $number",
//                    created = firstArticleCreatedTime.minusDays(number.toLong())
//                )
//            },
//            prevKey = when (startKey) {
//                STARTING_KEY -> null
//                else -> when (val prevKey = ensureValidKey(key = range.first - params.loadSize)) {
//                    // We're at the start, there's nothing more to load
//                    STARTING_KEY -> null
//                    else -> prevKey
//                }
//            },
//            nextKey = range.last + 1
//        )

        val page = params.key ?: 1
        return try {
            val response = service.searchArticles(
                query = query,
                page = page,
                itemsPerPage = 100
            )
            Log.d("result", response.items.isEmpty().toString())
            val gson = Gson()

            LoadResult.Page(
                data = response.items.mapIndexed { index, apiArticle ->
                    Article(
                        source = apiArticle.source, // serialize Source
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
                nextKey = if (response.items.isEmpty()) null else page + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // The refresh key is used for the initial load of the next PagingSource, after invalidation
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        // In our case we grab the item closest to the anchor position
        // then return its id - (state.config.pageSize / 2) as a buffer
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null

        return ensureValidKey(key = 10 - (state.config.pageSize / 2))
    }

    /**
     * Makes sure the paging key is never less than [STARTING_KEY]
     */
    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)
}
