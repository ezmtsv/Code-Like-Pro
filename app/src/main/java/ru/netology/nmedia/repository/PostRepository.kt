package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun like(id: Int)
    fun share(id: Int)
    fun removeById(id: Int)
    fun save(post: Post)
}