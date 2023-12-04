package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    //fun getAll(): LiveData<List<Post>>
    fun getAll(): List<Post>
    fun like(post: Post): Post

    //    fun share(id: Long): Post
    fun removeById(id: Long)
    fun save(post: Post): Post
    fun updateContent(id: Long, content: String)
    fun getAllAsync(callback: GetAllCallback)
    fun likeAsync(post: Post, callback: GetPostCallBack)
    fun saveAsync(post: Post, callback: GetPostCallBack)
    fun updateContentAsync(post: Post, content: String, callback: GetPostCallBack)
    fun removeByIdAsync(id: Long, callback: DelPostCallBack)
    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Exception)
    }

    interface GetPostCallBack {
        fun onSuccess(post: Post)
        fun onError(e: Exception)
    }

    interface DelPostCallBack {
        fun onSuccess(answer: String)
        fun onError(e: Exception)
    }
}