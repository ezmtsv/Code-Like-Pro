package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    //fun getAll(): LiveData<List<Post>>
    fun getAll(): List<Post>
    fun like(id: Long): Post
    fun share(id: Long): Post
    fun removeById(id: Long)
    fun save(post: Post): Post
    fun updateContent(id: Long, content: String, published: String)
//    fun getPost(id: Int): Post
}