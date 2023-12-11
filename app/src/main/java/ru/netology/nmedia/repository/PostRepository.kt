package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    //fun getAll(): LiveData<List<Post>>
//    fun getAll(): List<Post>
//    fun like(post: Post): Post

    //    fun share(id: Long): Post
//    fun removeById(id: Long)
//    fun save(post: Post): Post
//    fun updateContent(id: Long, content: String)

    fun getAllAsync(callback: Callback<List<Post>>)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun likeAsync(post: Post, callback: Callback<Post>)
    fun updateContentAsync(post: Post, content: String, callback: Callback<Post>)

    interface Callback<T> {
        fun onSuccess(posts: T) {}
        fun onError(e: Exception, txt: String = "") {}
    }
}