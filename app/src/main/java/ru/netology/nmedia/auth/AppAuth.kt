package ru.netology.nmedia.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.viewmodel.AuthViewModel

class AppAuth private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(
        AuthState(
            prefs.getLong(KEY_ID, 0),
            prefs.getString(KEY_TOKEN, null)
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
            commit()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState(0, null)
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TOKEN = "token"

        @Volatile
        private var instance: AppAuth? = null

        fun getInstance() = synchronized(this) {
            instance
                ?: throw IllegalStateException("getInstance should be called only after initApp")
        }

        fun initAuth(context: Context): AppAuth = instance ?: synchronized(this) {
            instance ?: AppAuth(context).also {
                AuthViewModel.userAuth = it.authState.value.token != null
                instance = it
            }

        }
    }

}

data class AuthState(val id: Long = 0, val token: String? = null)