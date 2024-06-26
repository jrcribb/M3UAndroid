package com.m3u.data.repository.stream

import androidx.paging.PagingSource
import com.m3u.data.database.model.Stream
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface StreamRepository {
    fun observe(id: Int): Flow<Stream?>

    fun observeAllByPlaylistUrl(playlistUrl: String): Flow<List<Stream>>
    fun pagingAllByPlaylistUrl(
        url: String,
        category: String,
        query: String,
        sort: Sort
    ): PagingSource<Int, Stream>

    suspend fun get(id: Int): Stream?

    suspend fun getRandomIgnoreSeriesAndHidden(): Stream?

    @Deprecated("stream url is not unique")
    suspend fun getByUrl(url: String): Stream?
    suspend fun getByPlaylistUrl(playlistUrl: String): List<Stream>
    suspend fun favouriteOrUnfavourite(id: Int)
    suspend fun hide(id: Int, target: Boolean)
    suspend fun reportPlayed(id: Int)
    suspend fun getPlayedRecently(): Stream?
    fun observeAllUnseenFavourites(limit: Duration): Flow<List<Stream>>
    fun observeAllFavourite(): Flow<List<Stream>>
    fun observeAllHidden(): Flow<List<Stream>>

    enum class Sort {
        UNSPECIFIED,
        ASC,
        DESC,
        RECENTLY
    }
}
