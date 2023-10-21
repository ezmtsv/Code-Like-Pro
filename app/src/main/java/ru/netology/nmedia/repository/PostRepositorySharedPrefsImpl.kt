package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositorySharedPrefsImpl(context: Context) : PostRepository {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val key = "posts"

    private var nextId: Int = 1
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)

    init {
        prefs.getString(key, null)?.let {
            posts = gson.fromJson(it, type)
            if (posts.toString() != "[]") nextId = posts.maxOf { post -> post.id } + 1
//            println("posts = $posts")
            data.value = posts
        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun like(id: Int) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likeByMe = !it.likeByMe,
                countLike = if (it.likeByMe) it.countLike - 1 else it.countLike + 1
            )
        }
        data.value = posts
        sync()
    }

    override fun share(id: Int) {
        posts = posts.map {
            if (it.id != id) it else it.copy(countRepost = it.countRepost + 1)
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Int) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun save(post: Post) {
        posts = if (post.id == 0) {
            listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    published = "Now"
                )
            ) + posts
        } else {
            posts.map { if (post.id != it.id) it else it.copy(content = post.content) }
        }
        data.value = posts
        sync()
    }

    override fun getPost(id: Int): Post =
        posts.find {
            it.id == id
        }!!

    private fun sync() {
        with(prefs.edit()) {
            putString(key, gson.toJson(posts))
            apply()
        }
    }
}