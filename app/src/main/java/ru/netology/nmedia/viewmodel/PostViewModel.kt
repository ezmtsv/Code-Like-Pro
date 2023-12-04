package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    likedByMe = false,
//    countRepost = 0,
//    countViews = 0,
    published = 0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun savePost(content: String) {
        edited.value?.let { post ->
            val text = content.trim()
            if (post.id != 0L) {
                if (text != post.content) {
                    repository.updateContentAsync(
                        post,
                        text,
                        object : PostRepository.GetPostCallBack {
                            override fun onSuccess(post: Post) {
                                _postCreated.postValue(Unit)
                            }

                            override fun onError(e: Exception) {
                                println(e.stackTraceToString())
                            }
                        })

                }
            } else {
                repository.saveAsync(
                    post.copy(content = text),
                    object : PostRepository.GetPostCallBack {
                        override fun onSuccess(post: Post) {
                            _postCreated.postValue(Unit)
                        }

                        override fun onError(e: Exception) {
                            println(e.stackTraceToString())
                        }
                    })

            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun like(post: Post) {
        repository.likeAsync(post, object : PostRepository.GetPostCallBack {
            override fun onSuccess(post: Post) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .map {
                            if (it.id == post.id) post else it
                        }
                    )
                )
            }

            override fun onError(e: Exception) {
                println(e.stackTraceToString())
            }
        })
    }

    fun remove(id: Long) {
        val old = _data.value?.posts.orEmpty()
        try {
            repository.removeByIdAsync(id, object : PostRepository.DelPostCallBack {
                override fun onSuccess(answer: String) {
                    _data.postValue(
                        _data.value?.copy(posts = _data.value?.posts.orEmpty()
                            .filter { it.id != id }
                        )
                    )
                }

                override fun onError(e: Exception) {
                    _data.postValue(_data.value?.copy(posts = old))
                    println(e.stackTraceToString())
                }
            })
        } catch (e: IOException) {
            _data.postValue(_data.value?.copy(posts = old))
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

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String =
        SimpleDateFormat("dd MMMM yyyy, HH:mm").format(Calendar.getInstance().time).toString()
}