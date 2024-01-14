package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    val dataInvisible: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAllAsync()
    suspend fun saveAsync(post: Post)
    suspend fun removeByIdAsync(post: Post)
    suspend fun likeAsync(post: Post)
    suspend fun updateContentAsync(post: Post, content: String)
    suspend fun savePosts(posts: List<Post>)
    suspend fun getPosts()
}