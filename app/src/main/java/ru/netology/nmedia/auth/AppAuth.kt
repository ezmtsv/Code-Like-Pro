package ru.netology.nmedia.auth

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.PushToken

private const val KEY_ID = "id"
private const val KEY_TOKEN = "token"

class AppAuth (context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(
        AuthState(
            prefs.getLong(KEY_ID, 0),
            prefs.getString(KEY_TOKEN, null)
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
            commit()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState(0, null)
        with(prefs.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                DependencyContainer.getInstance().apiService.sendPushToken(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    companion object {
//        @Volatile
//        private var instance: AppAuth? = null
//
//        fun getInstance() = synchronized(this) {
//            instance
//                ?: throw IllegalStateException("getInstance should be called only after initApp")
//        }
//
//        fun initAuth(context: Context): AppAuth = instance ?: synchronized(this) {
//            instance ?: AppAuth(context).also {
//                AuthViewModel.userAuth = it.authState.value.token != null
//                instance = it
//            }
//
//        }
//    }

}

data class AuthState(val id: Long = 0, val token: String? = null)