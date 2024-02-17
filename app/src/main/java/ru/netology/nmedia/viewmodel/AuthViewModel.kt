package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.PushMessage
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
    private val apiService: PostsApiService
) : ViewModel() {
    companion object {
        @Volatile
        var userAuth: Boolean = false
        const val DIALOG_OUT = 1
        const val DIALOG_IN = 2
        const val DIALOG_REG = 3
    }

//    private val repository: PostRepository =
//        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    val data = appAuth.authState
//    val authenticated: Boolean
//        get() = data.value.id != 0L

    val authState: LiveData<AuthState>
        //get() = AppAuth.getInstance().authState.asLiveData()
        get() = appAuth.authState.asLiveData()

    private val _dataState = MutableLiveData<FeedModelState>()

    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun getAuthFromServer(login: String, pass: String) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                val auth = repository.userAuth(login, pass)
                if (auth.id != 0L && auth.token != null) {
                    userAuth = true
                    appAuth.run { appAuth.setAuth(auth.id, auth.token) }
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
        appAuth.removeAuth()
        userAuth = false
    }

    fun testPushhes(text: String) {
        try {
            viewModelScope.launch {
                val token: String? = null
                val stringToken = PushToken(token ?: Firebase.messaging.token.await())
                apiService.sendTestPush(stringToken.token, PushMessage(5, content = text))

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}