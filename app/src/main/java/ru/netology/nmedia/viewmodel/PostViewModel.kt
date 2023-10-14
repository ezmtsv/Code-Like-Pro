package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    likeByMe = false,
    published = ""
)

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
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

    /*    fun cancelEdit() {
            edited.value = empty
        }*/
}