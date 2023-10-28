package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.text.SimpleDateFormat
import java.util.Calendar

private val empty = Post(
    id = 0,
    author = "Me",
    content = "",
    likeByMe = false,
    countRepost = 0,
    countViews = 0,
    published = "now"
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(context = application).postDao()
    )
    val data = repository.getAll()
    private val edited = MutableLiveData(empty)

    fun edit(post: Post) {
        edited.value = post
    }

    fun savePost(content: String) {
        edited.value?.let { post ->
            val text = content.trim()
            if (post.id != 0) {
                if (text != post.content) repository.updateContent(post.id, text, getTime())
            } else repository.save(post.copy(content = text, published = getTime()))
            edited.value = empty
        }
    }

    fun like(id: Int) = repository.like(id)

    fun remove(id: Int) = repository.removeById(id)

    fun share(id: Int) = repository.share(id)

    fun cancelEdit() {
        edited.value = empty
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String =
        SimpleDateFormat("dd MMMM yyyy, HH:mm").format(Calendar.getInstance().time).toString()
}