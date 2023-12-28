package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.text.SimpleDateFormat
import java.util.Calendar

private val empty = Post(
    id = 0,
    author = "Me",
    content = "",
    likedByMe = false,
//    countRepost = 0,
//    countViews = 0,
    published = 0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    var countPost: Int = 0
    val postCreated: LiveData<Unit>
        get() = _postCreated
    val data: LiveData<FeedModel> = repository.data.map {
        FeedModel(it, it.isEmpty())
    }
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    init {
        loadPosts()
    }

    fun loadPosts() {
        _dataState.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repository.getAllAsync()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun savePost(content: String) {
        _postCreated.postValue(Unit)
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    repository.saveAsync(post.copy(content = content))
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun like(post: Post) {
        viewModelScope.launch {
            try {
                repository.likeAsync(post)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeByIdAsync(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

//    fun share(id: Long) {
//        thread {
//            repository.share(id)
//            loadPosts()
//        }
//    }

    fun cancelEdit() {
        edited.value = empty
    }

    fun refreshPosts() {
        _dataState.value = FeedModelState(refreshing = true)
        viewModelScope.launch {
            try {
                repository.getAllAsync()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String =
        SimpleDateFormat("dd MMMM yyyy, HH:mm").format(Calendar.getInstance().time).toString()
}