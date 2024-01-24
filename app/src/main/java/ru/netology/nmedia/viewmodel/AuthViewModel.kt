package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

class AuthViewModel(application: Application) : AndroidViewModel(application) {
companion object {
    @Volatile
    var userAuth: Boolean = false
    const val DIALOG_OUT = 1
    const val DIALOG_IN = 2
    const val DIALOG_REG = 3
}
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    val data = AppAuth.getInstance().authState
    val authenticated: Boolean
        get() = data.value.id != 0L

    val authState: LiveData<AuthState>
        get() = AppAuth.getInstance().authState.asLiveData()

    private val _dataState = MutableLiveData<FeedModelState>()

    val dataState:LiveData<FeedModelState>
        get() = _dataState

    fun getAuthFromServer(login: String, pass: String) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                val auth = repository.userAuth(login, pass)
                if (auth.id != 0L && auth.token != null) {
                    userAuth = true
                    AppAuth.run { getInstance().setAuth(auth.id, auth.token) }
                    _dataState.value = FeedModelState()
                }
            } catch (e: Exception) {
                userAuth = false
                _dataState.value = FeedModelState(error = true)
                println(e.printStackTrace())
            }

        }
    }

    fun deleteAuth() {
        AppAuth.getInstance().removeAuth()
        userAuth = false
    }
}