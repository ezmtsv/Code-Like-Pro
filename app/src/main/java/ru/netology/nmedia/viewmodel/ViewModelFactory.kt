package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.repository.PostRepository

class ViewModelFactory(
    private val postRepository: PostRepository,
    private val appAuth: AppAuth,
    private val apiService: PostsApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
                PostViewModel(postRepository, appAuth) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(postRepository, appAuth, apiService) as T
            }
            else -> error("Unknown class $modelClass")
        }

}