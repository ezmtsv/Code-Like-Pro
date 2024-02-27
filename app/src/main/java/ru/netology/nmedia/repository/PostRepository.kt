package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
//    val dataInvisible: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAllAsync()
    suspend fun saveAsync(post: Post)
    suspend fun removeByIdAsync(post: Post)
    suspend fun likeAsync(post: Post)
    suspend fun updateContentAsync(post: Post, content: String)
    suspend fun savePosts(posts: List<Post>)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun upload(upload: MediaUpload): Media
//    suspend fun getPosts()
    suspend fun userAuth(login: String, pass: String): AuthState
}