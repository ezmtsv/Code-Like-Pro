package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.concurrent.thread

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
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        thread {
            // Начинаем загрузку
            _data.postValue(FeedModel(loading = true))
            try {
                // Данные успешно получены
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                // Получена ошибка
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun savePost(content: String) {
        edited.value?.let { post ->
            val text = content.trim()
            if (post.id != 0L) {
                if (text != post.content) {
                    thread {
                        repository.updateContent(post.id, text, getTime())
                        _postCreated.postValue(Unit)
                    }
                }
            } else {
                thread {
                    repository.save(post.copy(content = text, published = getTime()))
                    _postCreated.postValue(Unit)
                }
            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun like(id: Long) {
        thread {
            repository.like(id)
            loadPosts()
        }
    }

    fun remove(id: Long) {
        thread {
            // Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
                loadPosts()
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }

    fun share(id: Long) {
        thread {
            repository.share(id)
            loadPosts()
        }
    }

    fun cancelEdit() {
        edited.value = empty
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String =
        SimpleDateFormat("dd MMMM yyyy, HH:mm").format(Calendar.getInstance().time).toString()
}