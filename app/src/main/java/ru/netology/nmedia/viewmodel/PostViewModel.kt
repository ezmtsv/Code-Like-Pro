package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

private val empty = Post(
    id = 0,
    author = "Me",
    content = "",
    likeByMe = false,
    countRepost = 0,
    countViews = 0,
    published = "now"
)

/*
class PostViewModel(application: Application) : AndroidViewModel(application) {
//    private val repository: PostRepository = PostRepositoryFilesImpl(application)

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(context = application).postDao()
    )

    val edited = MutableLiveData(empty)
    val data = repository.getAll()
    fun like(id: Int) = repository.like(id)
    fun share(id: Int) = repository.share(id)
    fun remove(id: Int) = repository.removeById(id)

    fun savePost(content: String) {
        edited.value?.let { post ->
            val text = content.trim()
            if (text != post.content) repository.save(post.copy(content = text))
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = empty
    }

}*/
class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(context = application).postDao()
    )
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun edit(post: Post) {
        edited.value = post
    }

    fun savePost(content: String) {
        edited.value?.let { post ->
            val text = content.trim()
            if (post.id != 0) {
                if (text != post.content) repository.updateContent(post.id, text)
            } else repository.save(post.copy(content = text))
            edited.value = empty
        }
    }

    fun like(id: Int) = repository.like(id)
    fun remove(id: Int) = repository.removeById(id)

    fun share(id: Int) = repository.share(id)

    fun cancelEdit() {
        edited.value = empty
    }
}